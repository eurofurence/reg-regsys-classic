import { ListAllGroups } from '../apis/roomsrv.js'
import { StoredErrorList } from '../stores/errorlist.js'
import { debug } from '../shared/debug.js'
import { useTernary } from './useternary.js'

const { ref } = Vue

// useGroupList provides a list of groups that can be filtered and sorted.
//
// This composable integrates with the backend API and has a local cache to avoid unnecessary fetches.
//
// errors are asynchronously added to the errorlist store, but the errorlist is never cleared - you have
// to do that from the outside.
export const useGroupList = () => {
    // the filtered list of groups. Updated by fetch() or filter().
    //
    // Should not be updated from the outside!
    const groups = ref([])

    // the currently selected group, or undefined if no group is selected.
    //
    // Should not be updated directly from the outside!
    //
    // use select() to select or de-select a group.
    const selected = ref(undefined)

    // the filter criteria.
    //
    // Maintained from outside this composable, but kept here to keep everything together.
    const filter = {
        name: ref(''),
        public: useTernary(undefined),
        wheelchair: useTernary(undefined),
    }

    // the raw list of groups caches the response from the backend API.
    //
    // Exposed but should not normally be interacted with in any way.
    const rawGroups = ref([])

    // reload performs an async fetch of the raw group list from the backend API.
    //
    // if any errors occur, they are added to the errorlist store.
    //
    // after successfully retrieving the raw list, it is also refiltered, and the selected group is cleared.
    const reload = () => {
        debug('useGroupList.reload')
        selected.value = undefined
        ListAllGroups((gps) => {
            debug('useGroupList.reload.success', gps)
            rawGroups.value = gps
            apply()
        }, (status, apiError) => {
            debug('useGroupList.reload.error', status, apiError)
            StoredErrorList.errors.addError(apiError)
        })
    }

    const matchesFilter = (gp) => {
        const isPublic = (gp.flags ?? []).includes('public')
        const isWheelchair = (gp.flags ?? []).includes('wheelchair')

        const matchesNameFilter = filter.name.value ? gp.name.toLowerCase().includes(filter.name.value.toLowerCase()) : true
        const matchesANickFilter = filter.name.value ? (gp.members && gp.members.filter(m => m.nickname.toLowerCase().includes(filter.name.value.toLowerCase())).length > 0) : true
        const matchesCommentFilter = filter.name.value ? gp.comments && gp.comments.toLowerCase().includes(filter.name.value.toLowerCase()) : true
        const matchesPublicFilter = filter.public.matches(isPublic)
        const matchesWheelchairFilter = filter.wheelchair.matches(isWheelchair)
        return (matchesNameFilter || matchesANickFilter || matchesCommentFilter) && matchesPublicFilter && matchesWheelchairFilter
    }

    // apply uses the internal cache to apply new filter settings.
    //
    // no API access occurs, so this is a synchronous operation.
    //
    // clears the selected group because it may no longer be in the list.
    const apply = () => {
        debug('useGroupList.refilter')
        selected.value = undefined
        // TODO sort not implemented
        groups.value = rawGroups.value.filter(matchesFilter)
    }

    // select selects a group (by id), but only if it is in the current filter.
    //
    // If the group id is not found in the list, the selection is cleared.
    //
    // If the same group is already selected, it is instead unselected.
    //
    // Pass undefined to clear the selection.
    const select = (id) => {
        debug('useGroupList.select', id)
        if (id === undefined) {
            debug('useGroupList.select cleared')
            selected.value = undefined
        } else {
            const matchingGroupsInList = groups.value.filter((gp) => gp.id === id)
            if (matchingGroupsInList.length === 0) {
                debug('useGroupList.select id not in list - clearing selection', id)
                selected.value = undefined
            } else if (selected.value && selected.value.id === id) {
                debug('useGroupList.select id already selected - unselecting it', id)
                selected.value = undefined
            } else {
                debug('useGroupList.select ok', id)
                selected.value = matchingGroupsInList[0]
            }
        }
    }

    return { groups, selected, filter, rawGroups, reload, apply, select }
}