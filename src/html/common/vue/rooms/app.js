import { ErrorList } from './errorlist.js'
import { RoomList } from './roomlist.js'
import { RoomCreateForm } from './roomcreate.js'

export const App = {
    components: {
        ErrorList,
        RoomList,
        RoomCreateForm,
    },
    template: `<td class="contentbox">
   <div class="headline"><br/>{{ $t('rooms.app.title') }}</div>
   <hr class="contentbox"/>
        <ErrorList />
        <RoomList />
   <div class="headline"><br/>{{ $t('rooms.create.title') }}</div>
   <hr class="contentbox"/>
        <RoomCreateForm />
    </td>`
}
