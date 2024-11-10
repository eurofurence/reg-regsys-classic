import { ListAllRooms } from '../apis/roomsrv.js'
import { StoredErrorList } from '../stores/errorlist.js'
import { debug } from '../shared/debug.js'

const { ref, watch } = Vue
const { useI18n } = VueI18n

export const RoomList = {
    props: ['reload'], // increment to trigger list reload
    emits: ['room-clicked'],
    setup(props, { emit }) {
        debug('RoomList.setup', props)
        const { t } = useI18n()

        const roomList = ref([])

        const setRoomList = (rooms) => {
            debug('RoomList.setRoomList', rooms)
            roomList.value = rooms
        }

        const emitRoomClicked = (id) => {
            debug('RoomList.emitRoomClicked', id)
            emit('room-clicked', id)
        }

        const fetchRoomList = () => {
            debug('RoomList.fetchRoomList')
            ListAllRooms((rooms) => {
                debug('RoomList.fetchRoomList.success', rooms)
                setRoomList(rooms)
            }, (status, apiError) => {
                debug('RoomList.fetchRoomList.error', status, apiError)
                StoredErrorList.errors.addError(apiError)
            })
        }

        watch(() => props.reload, (newValue, oldValue) => {
            debug('RoomList.watch props.reload', oldValue, newValue)
            fetchRoomList()
        })

        return {
            t,
            roomList,
            setRoomList,
            emitRoomClicked,
        }
    },
    // TODO parse final flag
    // TODO highlight selected room for update
    template: `
<div class="headline"><br/>{{ t('rooms.list.title') }}</div>
<hr class="contentbox"/>
<p>{{ t('rooms.list.click') }}</p>
<table v-if="roomList" class="searchlist">
        <tr>
            <th class="searchlist" align="right">{{ t('rooms.list.header.no') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.name') }}</th>
            <th class="searchlist" align="right">{{ t('rooms.list.header.size') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.final') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.comments') }}</th>
        </tr>
        <tr v-for="(r, i) in roomList" class="searchlist_sep" @click="emitRoomClicked(r.id)">
            <td class="searchlist" align="right">{{ i+1 }}</td>
            <td class="searchlist">{{ r.name }}</td>
            <td class="searchlist" align="right">{{ r.size }}</td>
            <td class="searchlist" align="center">{{ r.flags }}</td>
            <td class="searchlist">{{ r.comments }}</td>
        </tr>
</table>
<p v-else>{{ t('rooms.list.empty') }}</p>
`
}
