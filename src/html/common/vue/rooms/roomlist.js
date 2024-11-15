import { debug } from '../shared/debug.js'
import { useRoomList } from '../use/useroomlist.js'

const { watch } = Vue
const { useI18n } = VueI18n

export const RoomList = {
    props: ['reload'], // increment to trigger list reload
    emits: ['roomClicked','filterChanged'],
    setup(props, { emit }) {
        debug('RoomList.setup', props)
        const { t } = useI18n()

        const list = useRoomList()

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

        const rooms = list.rooms
        const selected = list.selected
        const nameFilter = list.filter.name
        const finalFilter = list.filter.final
        const hcFilter = list.filter.handicapped

        return {
            t,
            rooms,
            selected,
            nameFilter,
            finalFilter,
            hcFilter,
            roomClicked,
            finalColumnClicked,
            hcColumnClicked,
        }
    },
    template: `
<div class="headline"><br/>{{ t('rooms.list.title') }}</div>
<hr class="contentbox"/>
<p>{{ t('rooms.list.filter') }}: <input type="text" size="40" maxlength="80" v-model.trim="nameFilter"/></p>
<p>{{ t('rooms.list.info') }}</p>
<table class="searchlist">
        <tr>
            <th class="searchlist" align="right">{{ t('rooms.list.header.no') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.name') }}</th>
            <th class="searchlist" align="right">{{ t('rooms.list.header.size') }}</th>
            <th class="searchlist" @click="finalColumnClicked">{{ t('rooms.list.header.final') }}&nbsp;{{ finalFilter.display('✔','❌','·') }}</th>
            <th class="searchlist" @click="hcColumnClicked">{{ t('rooms.list.header.handicapped') }}&nbsp;{{ hcFilter.display('✔','❌','·') }}</th>
            <th class="searchlist">{{ t('rooms.list.header.comments') }}</th>
        </tr>
        <tr v-for="(r, i) in rooms" class="searchlist_sep" @click="roomClicked(r.id)">
            <td :class="selected && r.id === selected.id ? 'searchlist selected' : 'searchlist'" align="right">{{ i+1 }}</td>
            <td :class="selected && r.id === selected.id ? 'searchlist selected' : 'searchlist'">{{ r.name }}</td>
            <td :class="selected && r.id === selected.id ? 'searchlist selected' : 'searchlist'" align="right">{{ r.size }}</td>
            <td :class="selected && r.id === selected.id ? 'searchlist selected' : 'searchlist'" align="center">{{ (r.flags ?? []).includes('final') ? '✔' : '' }}</td>
            <td :class="selected && r.id === selected.id ? 'searchlist selected' : 'searchlist'" align="center">{{ (r.flags ?? []).includes('handicapped') ? '✔' : '' }}</td>
            <td :class="selected && r.id === selected.id ? 'searchlist selected' : 'searchlist'">{{ r.comments }}</td>
        </tr>
</table>
`
}
