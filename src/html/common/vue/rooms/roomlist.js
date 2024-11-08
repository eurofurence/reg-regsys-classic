import { ListAllRooms } from '../apis/roomsrv.js'
import { StoredErrorList } from '../stores/errorlist.js'

const { ref } = Vue

export const RoomList = {
    setup() {
        // console.log('RoomList setup')
        const roomList = ref([])
        const setRoomList = (rooms) => {
            roomList.value = rooms
        }

        ListAllRooms((rooms) => {
            // console.log("got " + rooms.length + " rooms to store")
            // TODO sort by name
            setRoomList(rooms)
        }, StoredErrorList.errors.addError)

        return {
            roomList,
            setRoomList,
        }
    },
    // TODO parse final flag
    template: `
<table v-if="roomList" class="searchlist">
        <tr>
            <th class="searchlist" align="right">{{ $t('rooms.list.header.no') }}</th>
            <th class="searchlist">{{ $t('rooms.list.header.name') }}</th>
            <th class="searchlist" align="right">{{ $t('rooms.list.header.size') }}</th>
            <th class="searchlist">{{ $t('rooms.list.header.final') }}</th>
            <th class="searchlist">{{ $t('rooms.list.header.comments') }}</th>
        </tr>
        <tr v-for="(r, i) in roomList" class="searchlist_sep">
            <td class="searchlist" align="right">{{ i+1 }}</td>
            <td class="searchlist">{{ r.name }}</td>
            <td class="searchlist" align="right">{{ r.size }}</td>
            <td class="searchlist" align="center">{{ r.flags }}</td>
            <td class="searchlist">{{ r.comments }}</td>
        </tr>
</table>
<p v-else>{{ $t('rooms.list.empty') }}</p>
`
}
