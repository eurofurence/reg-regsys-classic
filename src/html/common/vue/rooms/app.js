import { ErrorList } from '../shared/errorlist.js'
import { RoomList } from './roomlist.js'
import { RoomForm } from './roomform.js'
import { debug } from '../shared/debug.js'

const { ref, onMounted } = Vue

export const App = {
    setup() {
        debug('App.setup')
        const roomId = ref('')
        const updateCount = ref(0)

        const setRoomId = (id) => {
            debug('App.setRoomId',id)
            roomId.value = id
        }
        const reloadRooms = () => {
            debug('App.reloadRooms')
            roomId.value = ''
            updateCount.value++
        }

        onMounted(() => reloadRooms())

        return {
            roomId,
            updateCount,
            setRoomId,
            reloadRooms,
        }
    },
    components: {
        ErrorList,
        RoomForm,
        RoomList,
    },
    template: `
        <ErrorList />
        <RoomForm :id="roomId" @rooms-possibly-updated="reloadRooms"/>
        <RoomList :reload="updateCount" @room-clicked="(room) => setRoomId(room ? room.id : '')" @filter-changed="reloadRooms"/>
   `
}
