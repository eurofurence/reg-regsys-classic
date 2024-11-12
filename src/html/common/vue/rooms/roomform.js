import { CreateRoom, UpdateRoom, GetRoomByID } from '../apis/roomsrv.js'
import { StoredErrorList } from '../stores/errorlist.js'
import { debug } from '../shared/debug.js'

const { ref, computed, watch } = Vue
const { useI18n } = VueI18n

export const RoomForm = {
    props: ['id'],
    emits: ['roomsPossiblyUpdated'],
    setup(props, { emit }) {
        debug('RoomForm.setup')
        const { t } = useI18n()

        const id = ref(props.id)
        const name = ref('')
        const roomsize = ref('2')
        const comments = ref('')

        const isNew = computed(() => {
            return id.value.length === 0
        })
        const isNameError = computed(() => {
            return name.value.length === 0
        })
        const isSizeError = computed(() => {
            return !roomsize.value.match(/^[1-9][0-9]*$/)
        })

        const errorList = StoredErrorList // get a local reference to the store ref
        const resetHandler = () => {
            debug('RoomForm.resetHandler')
            // even a reset will at least de-select the currently edited room
            emit('roomsPossiblyUpdated')
            errorList.resetErrors()
            id.value = ''
            name.value = ''
            roomsize.value = '2'
            comments.value = ''
        }
        const submitHandler = () => {
            debug('RoomForm.submitHandler')
            errorList.resetErrors()
            if (id.value.length === 0) {
                const room = {
                    name: name.value,
                    flags: [],
                    size: Number(roomsize.value),
                    comments: comments.value,
                }
                debug('RoomForm.submitHandler creating', room)
                CreateRoom(room,  (rm) => {
                    debug('RoomForm.submitHandler.success', rm)
                    resetHandler()
                },(status, apiError) => {
                    debug('RoomForm.submitHandler.error', status, apiError)
                    errorList.addError(apiError)
                })
            } else {
                const room = {
                    id: id.value,
                    name: name.value,
                    flags: [], // TODO
                    size: Number(roomsize.value),
                    comments: comments.value,
                }
                debug('RoomForm.submitHandler updating', room)
                UpdateRoom(room, (rm) => {
                    debug('RoomForm.submitHandler.success', rm)
                    resetHandler()
                }, (status, apiError) => {
                    debug('RoomForm.submitHandler.error', status, apiError)
                    errorList.addError(apiError)
                })
            }
        }

        watch(() => props.id, (newIdValue, oldIdValue) => {
            debug('RoomForm.watch props.id changed', oldIdValue, newIdValue)
            id.value = newIdValue
        })

        watch(id, (newIdValue, oldIdValue) => {
            debug('RoomForm.watch id changed', oldIdValue, newIdValue)
            if (newIdValue !== oldIdValue) {
                if (newIdValue.length === 0) {
                    resetHandler()
                } else {
                    GetRoomByID(newIdValue, (room) => {
                        debug('RoomForm.get.success', newIdValue, room)
                        id.value = newIdValue
                        name.value = room.name
                        roomsize.value = '' + room.size
                        comments.value = room.comments ?? ""
                    }, (status, apiError) => {
                        debug('RoomForm.get.error', status, apiError)
                        errorList.addError(apiError)
                    })
                }
            }
        }, { immediate: true })

        return {
            t,
            name,
            roomsize,
            comments,
            isNew,
            isNameError,
            isSizeError,
            submitHandler,
            resetHandler,
            errorList,
        }
    },
    template: `
      <div class="headline"><br/>{{ isNew ? t('rooms.create.title') : t('rooms.edit.title') }}</div>
      <hr class="contentbox"/>
      <table width="100%" cellpadding="0" cellspacing="4" border="0">
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">{{ isNew ? t('rooms.create.name') : t('rooms.edit.name') }}</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><input type="text" size="40" maxlength="80" v-model.trim="name" :class="{ error: isNameError }" :placeholder="isNew ? t('rooms.create.namehint') : t('rooms.edit.namehint')"/></td>
        </tr>
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">{{ isNew ? t('rooms.create.size') : t('rooms.edit.size') }}</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><input type="text" size="5" maxlength="5" v-model.trim="roomsize" :class="{ error: isSizeError }"/></td>
        </tr>
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">{{ isNew ? t('rooms.create.comments') : t('rooms.edit.comments') }}</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><input type="text" size="40" maxlength="80" v-model.trim="comments"/></td>
        </tr>
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">&nbsp;</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><button :disabled="isNameError || isSizeError" @click="submitHandler">{{ isNew ? t('rooms.create.save') : t('rooms.edit.save') }}</button>&nbsp;<button @click="resetHandler">{{ isNew ? t('rooms.create.cancel') : t('rooms.edit.cancel') }}</button></td>
        </tr>
      </table>
    `
}