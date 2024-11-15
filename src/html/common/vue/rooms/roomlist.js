import { debug } from '../shared/debug.js'
import { useRoomList } from '../use/useroomlist.js'

const { watch } = Vue
const { useI18n } = VueI18n

export const RoomList = {
    props: ['reload'], // increment to trigger list reload
    emits: ['roomClicked','filterChanged'],
    setup(props, { emit }) {
        debug('RoomList.setup', props)

        const list = useRoomList()

        // handlers

        const roomClicked = (id) => {
            debug('RoomList.roomClicked', id)
            list.select(id)
            emit('roomClicked', list.selected.value)
        }
        const finalColumnClicked = () => {
            debug('RoomList.finalColumnClicked')
            list.filter.final.cycle()
            list.apply()
            emit('filterChanged')
        }
        const hcColumnClicked = () => {
            debug('RoomList.hcColumnClicked')
            list.filter.handicapped.cycle()
            list.apply()
            emit('filterChanged')
        }

        watch(list.filter.name, (newValue, oldValue) => {
            if (newValue !== oldValue) {
                debug('RoomList.filter.name changed', oldValue, newValue)
                list.apply()
                emit('filterChanged')
            }
        })

        watch(() => props.reload, (newValue, oldValue) => {
            debug('RoomList.watch props.reload', oldValue, newValue)
            list.reload()
        })

        // formatting, style and i18n

        const { t } = useI18n()
        const listCellClass = (rm, extra = undefined) => {
            const isSelected = list.selected.value && rm.id === list.selected.value.id
            return 'searchlist nobreak' + (isSelected ? ' selected' : '') + (extra ? ' ' + extra : '')
        }
        const occupantDisplay = (rm, idx, alt) => {
            if (rm.occupants && rm.occupants.length > idx) {
                const m = rm.occupants[idx]
                return m.nickname + ' (' + m.id + ')'
            } else {
                return alt
            }
        }

        return {
            list,
            roomClicked,
            finalColumnClicked,
            hcColumnClicked,
            t,
            listCellClass,
            occupantDisplay,
        }
    },
    template: `
<div class="headline"><br/>{{ t('rooms.list.title') }}</div>
<hr class="contentbox"/>
<p>{{ t('rooms.list.filter') }}: <input type="text" size="40" maxlength="80" v-model.trim="list.filter.name.value"/></p>
<p>{{ t('rooms.list.info') }}</p>
<table class="searchlist">
        <tr>
            <th class="searchlist">{{ t('rooms.list.header.no') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.name') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.size') }}</th>
            <th class="searchlist" @click="finalColumnClicked">{{ t('rooms.list.header.final') }}&nbsp;{{ list.filter.final.display('✔','❌','·') }}</th>
            <th class="searchlist" @click="hcColumnClicked">{{ t('rooms.list.header.handicapped') }}&nbsp;{{ list.filter.handicapped.display('✔','❌','·') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.comments') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.occupants') }}</th>
        </tr>
        <template v-for="(r, i) in list.rooms.value">
            <tr class="searchlist_sep" @click="roomClicked(r.id)">
                <td :class="listCellClass(r,'right')">{{ i+1 }}</td>
                <td :class="listCellClass(r)">{{ r.name }}</td>
                <td :class="listCellClass(r,'right')">{{ r.size }}</td>
                <td :class="listCellClass(r,'center')">{{ (r.flags ?? []).includes('final') ? '✔' : '' }}</td>
                <td :class="listCellClass(r,'center')">{{ (r.flags ?? []).includes('handicapped') ? '✔' : '' }}</td>
                <td :class="listCellClass(r)">{{ r.comments }}</td>
                <td :class="listCellClass(r)">{{ occupantDisplay(r,0,'-') }}</td>
            </tr>
            <template v-for="(m, j) in r.occupants">
                <tr v-if="j > 0" class="searchlist" @click="roomClicked(r.id)">
                    <td :class="listCellClass(r)">&nbsp;</td>
                    <td :class="listCellClass(r)">&nbsp;</td>
                    <td :class="listCellClass(r)">&nbsp;</td>
                    <td :class="listCellClass(r)">&nbsp;</td>
                    <td :class="listCellClass(r)">&nbsp;</td>
                    <td :class="listCellClass(r)">&nbsp;</td>
                    <td :class="listCellClass(r)">{{ occupantDisplay(r,j,'') }}</td>
                </tr>
            </template>
        </template>
</table>
`
}
