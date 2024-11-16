import { ErrorList } from '../shared/errorlist.js'
import { GroupList } from './grouplist.js'
import { GroupForm } from './groupform.js'
import { debug } from '../shared/debug.js'

const { ref, onMounted } = Vue

export const App = {
    setup() {
        debug('App.setup')
        const groupId = ref('')
        const updateCount = ref(0)

        const setGroupId = (id) => {
            debug('App.setGroupId',id)
            groupId.value = id
        }
        const reloadGroups = () => {
            debug('App.reloadGroups')
            groupId.value = ''
            updateCount.value++
        }

        onMounted(() => reloadGroups())

        return {
            groupId,
            updateCount,
            setGroupId,
            reloadGroups,
        }
    },
    components: {
        ErrorList,
        GroupForm,
        GroupList,
    },
    template: `
        <ErrorList />
        <GroupForm :id="groupId" @groups-possibly-updated="reloadGroups"/>
        <GroupList :reload="updateCount" @group-clicked="(group) => setGroupId(group ? group.id : '')" @filter-changed="reloadGroups"/>
   `
}
