import { CreateGroup, UpdateGroup, GetGroupByID } from '../apis/roomsrv.js'
import { StoredErrorList } from '../stores/errorlist.js'
import { debug } from '../shared/debug.js'

const { ref, computed } = Vue

// useGroupEditor provides a group model made up of separate refs for a group edit/create form.
//
// This composable integrates with the backend API.
//
// errors are asynchronously added to the errorlist store, and the error list is cleared upon successful
// API operations.
export const useGroupEditor = () => {
    const id = ref('') // the group id currently being edited (or created if undefined). Ensure string. Should not assign directly.
    const name = ref('')
    const maxSize = ref('6') // ensure string
    const owner = ref('0') // ensure string - TODO dropdown with model?
    const isPublic = ref(false) // cannot call this public
    const wheelchair = ref(false)
    const comments = ref('')
    const members = ref([]) // ensure never undefined
    const invites = ref([]) // ensure never undefined

    // maxSize converts the size to a Number (or -1 if invalid)
    const maxSizeNumber = computed(() => {
        if (maxSize.value.match(/^[1-9][0-9]{0,2}$/)) {
            return Number(maxSize.value)
        } else {
            return -1
        }
    })
    const ownerBadgeNumber = computed(() => {
        if (owner.value.match(/^[1-9][0-9]{0,5}$/)) {
            // 1 million badge numbers should be enough for any convention
            return Number(owner.value)
        } else {
            return 0
        }
    })

    const isNew = computed(() => {
        return id.value.length === 0
    })
    const isNameError = computed(() => {
        return name.value.length === 0 || name.value.length > 80
    })
    const isSizeError = computed(() => {
        return maxSize.value < members.value.length + invites.value.length
    })
    const isOwnerError = computed(() => {
        return ownerBadgeNumber.value === 0
    })
    const isError = computed(() => {
        return isNameError.value || isSizeError.value
    })

    // setupForId either resets the editor to blank (create group) state or obtains
    // the group from the backend API and then sets up the editor for it.
    //
    // newId should be an uuid, or '' or undefined
    //
    // onSuccess is called after the operation completes, which may or may not be asynchronous.
    const setupForId = (newId = undefined, onSuccess = undefined) => {
        debug('useGroupEditor.setupForId', newId)
        if (newId) {
            GetGroupByID(newId, (group) => {
                debug('useGroupEditor.setupForId.get.success', newId, group)
                StoredErrorList.resetErrors()
                id.value = newId
                name.value = group.name
                maxSize.value = '' + group.maximum_size
                owner.value = '' + group.owner
                isPublic.value = (group.flags ?? []).includes('public')
                wheelchair.value = (group.flags ?? []).includes('wheelchair')
                comments.value = group.comments ?? ''
                members.value = group.members ?? []
                invites.value = group.invites ?? []
                if (onSuccess) {
                    onSuccess()
                }
            }, (status, apiError) => {
                debug('useGroupEditor.setupForId.get.error', status, apiError)
                StoredErrorList.addError(apiError)
            })
        } else {
            debug('useGroupEditor.reset.success')
            StoredErrorList.resetErrors()
            id.value = ''
            name.value = ''
            maxSize.value = '6'
            owner.value = '0'
            isPublic.value = false
            wheelchair.value = false
            comments.value = ''
            members.value = []
            invites.value = []
            if (onSuccess) {
                onSuccess()
            }
        }
    }

    const flagsList = () => {
        const result = []
        if (isPublic.value) {
            result.push('public')
        }
        if (wheelchair.value) {
            result.push('wheelchair')
        }
        return result
    }

    // save creates or updates the editable group fields using the backend API.
    //
    // onSuccess is called after the operation completes. It receives the saved group as a parameter.
    // This also passes back its id, should you need it.
    //
    // error list is cleared before starting the save operation. You are expected not to allow
    // it to trigger if any field is in error state (see isError) or if previous errors occurred
    // during load.
    const save = (onSuccess) => {
        debug('useGroupEditor.save')
        StoredErrorList.resetErrors()
        if (id.value) {
            const group = {
                id: id.value,
                name: name.value,
                flags: flagsList(),
                owner: ownerBadgeNumber.value,
                maximum_size: maxSizeNumber.value,
                comments: comments.value,
            }
            debug('useGroupEditor.save updating', group)
            UpdateGroup(group, (gp) => {
                debug('useGroupEditor.save.success', gp)
                setupForId()
                if (onSuccess) {
                    onSuccess(gp)
                }
            }, (status, apiError) => {
                debug('useGroupEditor.save.error', status, apiError)
                StoredErrorList.addError(apiError)
            })
        } else {
            const group = {
                name: name.value,
                flags: flagsList(),
                owner: ownerBadgeNumber.value,
                maximum_size: maxSizeNumber.value,
                comments: comments.value,
            }
            debug('useGroupEditor.save creating', group)
            CreateGroup(group,  (gp) => {
                debug('useGroupEditor.save.success', gp)
                setupForId()
                if (onSuccess) {
                    onSuccess(gp)
                }
            },(status, apiError) => {
                debug('useGroupEditor.save.error', status, apiError)
                StoredErrorList.addError(apiError)
            })
        }
    }

    return {
        id,
        name,
        maxSize,
        owner,
        isPublic,
        wheelchair,
        comments,
        members,
        invites,
        maxSizeNumber,
        ownerBadgeNumber,
        isNew,
        isNameError,
        isSizeError,
        isOwnerError,
        isError,
        setupForId,
        save,
    }
}