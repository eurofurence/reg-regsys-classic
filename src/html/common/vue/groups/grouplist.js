import { debug } from '../shared/debug.js'
import { useGroupList } from '../use/usegrouplist.js'

const { watch } = Vue
const { useI18n } = VueI18n

export const GroupList = {
    props: ['reload'], // increment to trigger list reload
    emits: ['groupClicked','filterChanged'],
    setup(props, { emit }) {
        debug('GroupList.setup', props)

        const list = useGroupList()

        // handlers

        const groupClicked = (id) => {
            debug('GroupList.groupClicked', id)
            list.select(id)
            emit('groupClicked', list.selected.value)
        }
        const publicColumnClicked = () => {
            debug('GroupList.publicColumnClicked')
            list.filter.public.cycle()
            list.apply()
            emit('filterChanged')
        }
        const wheelchairColumnClicked = () => {
            debug('GroupList.wheelchairColumnClicked')
            list.filter.wheelchair.cycle()
            list.apply()
            emit('filterChanged')
        }

        watch(list.filter.name, (newValue, oldValue) => {
            if (newValue !== oldValue) {
                debug('GroupList.filter.name changed', oldValue, newValue)
                list.apply()
                emit('filterChanged')
            }
        })

        watch(() => props.reload, (newValue, oldValue) => {
            debug('GroupList.watch props.reload', oldValue, newValue)
            list.reload()
        })

        // formatting, style and i18n

        const { t } = useI18n()
        const listCellClass = (gp, extra = undefined) => {
            const isSelected = list.selected.value && gp.id === list.selected.value.id
            return 'searchlist nobreak' + (isSelected ? ' selected' : '') + (extra ? ' ' + extra : '')
        }
        const maximalIndexes = (gp) => {
            const mKeys =  (gp.members ?? []).keys()
            const iKeys = (gp.invited ?? []).keys()
            return mKeys.length > iKeys.length ? mKeys : iKeys
        }
        const memberDisplay = (gp, idx, alt) => {
            if (gp.members && gp.members.length > idx) {
                const m = gp.members[idx]
                return m.nickname + ' (' + m.id + ')'
            } else {
                return alt
            }
        }
        const invitedDisplay = (gp, idx, alt) => {
            if (gp.invited && gp.invited.length > idx) {
                const m = gp.invited[idx]
                return m.nickname + ' (' + m.id + ')'
            } else {
                return alt
            }
        }

        return {
            list,
            groupClicked,
            publicColumnClicked,
            wheelchairColumnClicked,
            t,
            listCellClass,
            maximalIndexes,
            memberDisplay,
            invitedDisplay,
        }
    },
    template: `
<div class="headline"><br/>{{ t('groups.list.title') }}</div>
<hr class="contentbox"/>
<p>{{ t('groups.list.filter') }}: <input type="text" size="40" maxlength="80" v-model.trim="list.filter.name.value"/></p>
<p>{{ t('groups.list.info') }}</p>
<table class="searchlist">
        <tr>
            <th class="searchlist">{{ t('groups.list.header.no') }}</th>
            <th class="searchlist">{{ t('groups.list.header.name') }}</th>
            <th class="searchlist">{{ t('groups.list.header.size') }}</th>
            <th class="searchlist" @click="publicColumnClicked">{{ t('groups.list.header.public') }}&nbsp;{{ list.filter.public.display('✔','❌','·') }}</th>
            <th class="searchlist" @click="wheelchairColumnClicked">{{ t('groups.list.header.wheelchair') }}&nbsp;{{ list.filter.wheelchair.display('✔','❌','·') }}</th>
            <th class="searchlist">{{ t('groups.list.header.comments') }}</th>
            <th class="searchlist">{{ t('groups.list.header.members') }}</th>
            <th class="searchlist">{{ t('groups.list.header.invited') }}</th>
        </tr>
        <template v-for="(g, i) in list.groups.value">
            <tr class="searchlist_sep" @click="groupClicked(g.id)">
                <td :class="listCellClass(g,'right')">{{ i+1 }}</td>
                <td :class="listCellClass(g)">{{ g.name }}</td>
                <td :class="listCellClass(g,'right')">{{ g.size }}</td>
                <td :class="listCellClass(g,'center')">{{ (g.flags ?? []).includes('public') ? '✔' : '' }}</td>
                <td :class="listCellClass(g,'center')">{{ (g.flags ?? []).includes('wheelchair') ? '✔' : '' }}</td>
                <td :class="listCellClass(g)">{{ g.comments }}</td>
                <td :class="listCellClass(g)">{{ memberDisplay(g,0,'-') }}</td>
                <td :class="listCellClass(g)">{{ invitedDisplay(g,0,'-') }}</td>
            </tr>
            <template v-for="(ignored, j) in maximalIndexes(g)">
                <tr v-if="j > 0" class="searchlist" @click="groupClicked(g.id)">
                    <td :class="listCellClass(g)">&nbsp;</td>
                    <td :class="listCellClass(g)">&nbsp;</td>
                    <td :class="listCellClass(g)">&nbsp;</td>
                    <td :class="listCellClass(g)">&nbsp;</td>
                    <td :class="listCellClass(g)">&nbsp;</td>
                    <td :class="listCellClass(g)">&nbsp;</td>
                    <td :class="listCellClass(g)">{{ memberDisplay(g,j,'') }}</td>
                    <td :class="listCellClass(g)">{{ invitedDisplay(g,j,'') }}</td>
                </tr>
            </template>
        </template>
</table>
`
}
