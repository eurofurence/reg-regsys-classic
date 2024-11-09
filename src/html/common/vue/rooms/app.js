import { ErrorList } from './errorlist.js'
import { RoomList } from './roomlist.js'
import { RoomCreateForm } from './roomcreate.js'

export const App = {
    components: {
        ErrorList,
        RoomList,
        RoomCreateForm,
    },
    template: `
        <ErrorList />
   <div class="headline"><br/>{{ $t('rooms.list.title') }}</div>
   <hr class="contentbox"/>
        <RoomList />
   <div class="headline"><br/>{{ $t('rooms.create.title') }}</div>
   <hr class="contentbox"/>
        <RoomCreateForm />
   `
}
