import { CreateRoom, UpdateRoom, GetRoomByID } from '../apis/roomsrv.js'
import { StoredErrorList } from '../stores/errorlist.js'
import { debug } from '../shared/debug.js'

const { ref, computed } = Vue

// useRoomEditor provides a room model made up of separate refs for a room edit/create form.
//
// This composable integrates with the backend API.
//
// errors are asynchronously added to the errorlist store, and the error list is cleared upon successful
// API operations.
export const useRoomEditor = () => {
    const id = ref('') // the room id currently being edited (or created if undefined). Ensure string. Should not assign directly.
    const name = ref('')
    const size = ref('2') // ensure string
    const final = ref(false)
    const handicapped = ref(false)
    const comments = ref('')
    const occupants = ref([]) // ensure never undefined

    // bedCount converts the size to a Number (or -1 if invalid)
    const bedCount = computed(() => {
        if (size.value.match(/^[1-9][0-9]{0,2}$/)) {
            return Number(size.value)
        } else {
            return -1
        }
    })

    const isNew = computed(() => {
        return id.value.length === 0
    })
    const isNameError = computed(() => {
        return name.value.length === 0 || name.value.length > 80
    })
    const isSizeError = computed(() => {
        return bedCount.value < occupants.value.length
    })
    const isError = computed(() => {
        return isNameError.value || isSizeError.value
    })

    // setupForId either resets the editor to blank (create room) state or obtains
    // the room from the backend API and then sets up the editor for it.
    //
    // newId should be an uuid, or '' or undefined
    //
    // onSuccess is called after the operation completes, which may or may not be asynchronous.
    const setupForId = (newId = undefined, onSuccess = undefined) => {
        debug('useRoomEditor.setupForId', newId)
        if (newId) {
            GetRoomByID(newId, (room) => {
                debug('useRoomEditor.setupForId.get.success', newId, room)
                StoredErrorList.resetErrors()
                id.value = newId
                name.value = room.name
                size.value = '' + room.size
                final.value = (room.flags ?? []).includes('final')
                handicapped.value = (room.flags ?? []).includes('handicapped')
                comments.value = room.comments ?? ''
                occupants.value = room.occupants ?? []
                if (onSuccess) {
                    onSuccess()
                }
            }, (status, apiError) => {
                debug('useRoomEditor.setupForId.get.error', status, apiError)
                StoredErrorList.addError(apiError)
            })
        } else {
            debug('useRoomEditor.reset.success')
            StoredErrorList.resetErrors()
            id.value = ''
            name.value = ''
            size.value = '2'
            final.value = false
            handicapped.value = false
            comments.value = ''
            occupants.value = []
            if (onSuccess) {
                onSuccess()
            }
        }
    }

    const flagsList = () => {
        const result = []
        if (final.value) {
            result.add('final')
        }
        if (handicapped.value) {
            result.add('handicapped')
        }
        return result
    }

    // save creates or updates the editable room fields using the backend API.
    //
    // onSuccess is called after the operation completes. It receives the saved room as a parameter.
    // This also passes back its id, should you need it.
    //
    // error list is cleared before starting the save operation. You are expected not to allow
    // it to trigger if any field is in error state (see isError) or if previous errors occurred
    // during load.
    const save = (onSuccess) => {
        debug('useRoomEditor.save')
        StoredErrorList.resetErrors()
        if (id.value) {
            const room = {
                id: id.value,
                name: name.value,
                flags: flagsList(),
                size: bedCount.value,
                comments: comments.value,
            }
            debug('useRoomEditor.save updating', room)
            UpdateRoom(room, (rm) => {
                debug('useRoomEditor.save.success', rm)
                setupForId()
                if (onSuccess) {
                    onSuccess(rm)
                }
            }, (status, apiError) => {
                debug('useRoomEditor.save.error', status, apiError)
                StoredErrorList.addError(apiError)
            })
        } else {
            const room = {
                name: name.value,
                flags: flagsList(),
                size: bedCount.value,
                comments: comments.value,
            }
            debug('useRoomEditor.save creating', room)
            CreateRoom(room,  (rm) => {
                debug('useRoomEditor.save.success', rm)
                setupForId()
                if (onSuccess) {
                    onSuccess(rm)
                }
            },(status, apiError) => {
                debug('useRoomEditor.save.error', status, apiError)
                StoredErrorList.addError(apiError)
            })
        }
    }

    return {
        id,
        name,
        size,
        final,
        handicapped,
        comments,
        occupants,
        bedCount,
        isNew,
        isNameError,
        isSizeError,
        isError,
        setupForId,
        save,
    }
}