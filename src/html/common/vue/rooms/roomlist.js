import { ListAllRooms } from '../apis/roomsrv.js'
import { StoredErrorList } from '../stores/errorlist.js'
import { debug } from '../shared/debug.js'

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
        const finalFilter = ref(undefined)
        const hcFilter = ref(undefined)

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
            const matchesFinalFilter = finalFilter.value === undefined || (room.flags ?? []).includes('final') === finalFilter.value
            const matchesHcFilter = hcFilter.value === undefined || (room.flags ?? []).includes('handicapped') === hcFilter.value
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

        const cycleFilter = (triBoolValue) => {
            if (triBoolValue === true) {
                return false
            } else if (triBoolValue === false) {
                return undefined
            } else {
                return true
            }
        }
        const finalColumnClicked = () => {
            debug('RoomList.finalColumnClicked')
            finalFilter.value = cycleFilter(finalFilter.value)
            fetchRoomList()
            emitFilterChanged()
        }
        const hcColumnClicked = () => {
            debug('RoomList.hcColumnClicked')
            hcFilter.value = cycleFilter(hcFilter.value)
            fetchRoomList()
            emitFilterChanged()
        }
        const displayFilter = (triBoolValue) => {
            if (triBoolValue === true) {
                return '✔'
            } else if (triBoolValue === false) {
                return '❌'
            } else {
                return '·'
            }
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
            displayFilter,
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
            <th class="searchlist" @click="finalColumnClicked">{{ t('rooms.list.header.final') }}&nbsp;{{ displayFilter(finalFilter) }}</th>
            <th class="searchlist" @click="hcColumnClicked">{{ t('rooms.list.header.handicapped') }}&nbsp;{{ displayFilter(hcFilter) }}</th>
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
