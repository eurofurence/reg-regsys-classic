import { ErrorList } from '../shared/errorlist.js'
import { RoomList } from './roomlist.js'
import { RoomCreateForm } from './roomcreate.js'

const { useI18n } = VueI18n

export const App = {
    setup() {
        const { t } = useI18n()

        return {
            t
        }
    },
    components: {
        ErrorList,
        RoomList,
        RoomCreateForm,
    },
    template: `
        <ErrorList />
   <div class="headline"><br/>{{ t('rooms.list.title') }}</div>
   <hr class="contentbox"/>
        <RoomList />
   <div class="headline"><br/>{{ t('rooms.create.title') }}</div>
   <hr class="contentbox"/>
        <RoomCreateForm />
   `
}
