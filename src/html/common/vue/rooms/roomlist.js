import { ListAllRooms } from '../apis/roomsrv.js'
import { StoredErrorList } from '../stores/errorlist.js'
import { debug } from '../shared/debug.js'
import { useTernary } from '../use/useternary.js'

const { ref, watch } = Vue
const { useI18n } = VueI18n

export const RoomList = {
    props: ['reload'], // increment to trigger list reload
    emits: ['roomClicked','filterChanged'],
    setup(props, { emit }) {
        debug('RoomList.setup', props)
        const { t } = useI18n()

        const roomList = ref([])
        const selectedId = ref('')
        const filter = ref('')
        const finalFilter = useTernary(undefined)
        const hcFilter = useTernary(undefined)

        const setRoomList = (rooms) => {
            debug('RoomList.setRoomList', rooms)
            roomList.value = rooms
            selectedId.value = ''
        }

        const emitRoomClicked = (id) => {
            debug('RoomList.emitRoomClicked', id)
            if (selectedId.value === id) {
                // allow un-select
                selectedId.value = ''
            } else {
                selectedId.value = id
            }
            emit('roomClicked', selectedId.value)
        }
        const emitFilterChanged = () => {
            debug('RoomList.emitFilterChanged')
            selectedId.value = ''
            emit('filterChanged')
        }

        const matchesFilter = (room) => {
            const matchesNameFilter = filter.value ? room.name.toLowerCase().includes(filter.value.toLowerCase()) : true
            const matchesFinalFilter = finalFilter.matches((room.flags ?? []).includes('final'))
            const matchesHcFilter = hcFilter.matches((room.flags ?? []).includes('handicapped'))
            return matchesNameFilter && matchesFinalFilter && matchesHcFilter
        }
        const fetchRoomList = () => {
            debug('RoomList.fetchRoomList')
            ListAllRooms((rooms) => {
                debug('RoomList.fetchRoomList.success', rooms)
                rooms = rooms.filter(matchesFilter)
                setRoomList(rooms)
            }, (status, apiError) => {
                debug('RoomList.fetchRoomList.error', status, apiError)
                StoredErrorList.errors.addError(apiError)
            })
        }

        const finalColumnClicked = () => {
            debug('RoomList.finalColumnClicked')
            finalFilter.cycle()
            fetchRoomList()
            emitFilterChanged()
        }
        const hcColumnClicked = () => {
            debug('RoomList.hcColumnClicked')
            hcFilter.cycle()
            fetchRoomList()
            emitFilterChanged()
        }

        watch(filter, (newValue, oldValue) => {
            if (newValue !== oldValue) {
                fetchRoomList()
                emitFilterChanged()
            }
        })

        watch(() => props.reload, (newValue, oldValue) => {
            debug('RoomList.watch props.reload', oldValue, newValue)
            fetchRoomList()
        })

        return {
            t,
            roomList,
            selectedId,
            filter,
            finalFilter,
            hcFilter,
            finalColumnClicked,
            hcColumnClicked,
            emitRoomClicked,
        }
    },
    template: `
<div class="headline"><br/>{{ t('rooms.list.title') }}</div>
<hr class="contentbox"/>
<p>{{ t('rooms.list.filter') }}: <input type="text" size="40" maxlength="80" v-model.trim="filter"/></p>
<p>{{ t('rooms.list.info') }}</p>
<table class="searchlist">
        <tr>
            <th class="searchlist" align="right">{{ t('rooms.list.header.no') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.name') }}</th>
            <th class="searchlist" align="right">{{ t('rooms.list.header.size') }}</th>
            <th class="searchlist" @click="finalColumnClicked">{{ t('rooms.list.header.final') }}&nbsp;{{ finalFilter.display('✔','❌','·') }}</th>
            <th class="searchlist" @click="hcColumnClicked">{{ t('rooms.list.header.handicapped') }}&nbsp;{{ hcFilter.display('✔','❌','·') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.comments') }}</th>
        </tr>
        <tr v-for="(r, i) in roomList" class="searchlist_sep" @click="emitRoomClicked(r.id)">
            <td :class="r.id === selectedId ? 'searchlist selected' : 'searchlist'" align="right">{{ i+1 }}</td>
            <td :class="r.id === selectedId ? 'searchlist selected' : 'searchlist'">{{ r.name }}</td>
            <td :class="r.id === selectedId ? 'searchlist selected' : 'searchlist'" align="right">{{ r.size }}</td>
            <td :class="r.id === selectedId ? 'searchlist selected' : 'searchlist'" align="center">{{ (r.flags ?? []).includes('final') ? '✔' : '' }}</td>
            <td :class="r.id === selectedId ? 'searchlist selected' : 'searchlist'" align="center">{{ (r.flags ?? []).includes('handicapped') ? '✔' : '' }}</td>
            <td :class="r.id === selectedId ? 'searchlist selected' : 'searchlist'">{{ r.comments }}</td>
        </tr>
</table>
`
}
