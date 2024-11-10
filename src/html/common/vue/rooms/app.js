import { ErrorList } from '../shared/errorlist.js'
import { RoomList } from './roomlist.js'
import { RoomForm } from './roomform.js'

const { ref } = Vue

export const App = {
    setup() {
        const roomId = ref('')
        const updateCount = ref(0)

        const setRoomId = (id) => {
            console.log('received '+id)
            roomId.value = id
        }
        const reloadRooms = () => {
            console.log('reload rooms')
            roomId.value = ''
            updateCount.value++
        }

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
        <RoomList :reload="updateCount" @room-clicked="(id) => setRoomId(id)"/>
   `
}
