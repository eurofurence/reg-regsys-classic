import { ListAllRooms } from '../apis/roomsrv.js'
import { StoredErrorList } from '../stores/errorlist.js'
import { debug } from '../shared/debug.js'
import { useTernary } from './useternary.js'

const { ref } = Vue

// useRoomList provides a list of rooms that can be filtered and sorted.
//
// This composable integrates with the backend API and has a local cache to avoid unnecessary fetches.
//
// errors are asynchronously added to the errorlist store, but the errorlist is never cleared - you have to do that
// from the outside.
export const useRoomList = () => {
    // the filtered list of rooms. Updated by fetch() or filter().
    //
    // Should not be updated from the outside!
    const rooms = ref([])

    // the currently selected room, or undefined if no room is selected.
    //
    // Should not be updated directly from the outside!
    //
    // use select() to select or de-select a room.
    const selected = ref(undefined)

    // the filter criteria.
    //
    // Maintained from outside this composable, but kept here to keep everything together.
    const filter = {
        name: ref(''),
        final: useTernary(undefined),
        handicapped: useTernary(undefined),
    }

    // the raw list of rooms caches the response from the backend API.
    //
    // Exposed but should not normally be interacted with in any way.
    const rawRooms = ref([])

    // reload performs an async fetch of the raw rooms list from the backend API.
    //
    // if any errors occur, they are added to the errorlist store.
    //
    // after successfully retrieving the raw list, it is also refiltered, and the selected room is cleared.
    const reload = () => {
        debug('useRoomList.reload')
        selected.value = undefined
        ListAllRooms((rms) => {
            debug('useRoomList.reload.success', rms)
            rawRooms.value = rms
            apply()
        }, (status, apiError) => {
            debug('useRoomList.reload.error', status, apiError)
            StoredErrorList.errors.addError(apiError)
        })
    }

    const matchesFilter = (rm) => {
        const isFinal = (rm.flags ?? []).includes('final')
        const isHandicapped = (rm.flags ?? []).includes('handicapped')

        const matchesNameFilter = filter.name.value ? rm.name.toLowerCase().includes(filter.name.value.toLowerCase()) : true
        const matchesANickFilter = filter.name.value ? (rm.occupants && rm.occupants.filter(o => o.nickname.toLowerCase().includes(filter.name.value.toLowerCase())).length > 0) : true
        const matchesFinalFilter = filter.final.matches(isFinal)
        const matchesHcFilter = filter.handicapped.matches(isHandicapped)
        return (matchesNameFilter || matchesANickFilter) && matchesFinalFilter && matchesHcFilter
    }

    // apply uses the internal cache to apply new filter settings.
    //
    // no API access occurs, so this is a synchronous operation.
    //
    // clears the selected room because it may no longer be in the list.
    const apply = () => {
        debug('useRoomList.refilter')
        selected.value = undefined
        // TODO sort not implemented
        rooms.value = rawRooms.value.filter(matchesFilter)
    }

    // select selects a room (by id), but only if it is in the current filter.
    //
    // If the room id is not found in the list, the selection is cleared.
    //
    // If the same room is already selected, it is instead unselected.
    //
    // Pass undefined to clear the selection.
    const select = (id) => {
        debug('useRoomList.select', id)
        if (id === undefined) {
            debug('useRoomList.select cleared')
            selected.value = undefined
        } else {
            const matchingRoomsInList = rooms.value.filter((rm) => rm.id === id)
            if (matchingRoomsInList.length === 0) {
                debug('useRoomList.select id not in list - clearing selection', id)
                selected.value = undefined
            } else if (selected.value && selected.value.id === id) {
                debug('useRoomList.select id already selected - unselecting it', id)
                selected.value = undefined
            } else {
                debug('useRoomList.select ok', id)
                selected.value = matchingRoomsInList[0]
            }
        }
    }

    return { rooms, selected, filter, rawRooms, reload, apply, select }
}