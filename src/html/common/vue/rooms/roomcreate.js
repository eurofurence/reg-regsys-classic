import { CreateRoom } from '../apis/roomsrv.js'
import { StoredErrorList } from '../stores/errorlist.js'

const { ref } = Vue

export const RoomCreateForm = {
    setup() {
        const name = ref('')
        const roomSize = ref(2)
        const errorList = StoredErrorList // get a local reference to the store ref
        const submitHandler = () => {
            errorList.resetErrors()
            CreateRoom({
                // TODO field validation functions
                name: name.value,
                flags: [],
                size: roomSize.value,
            }, (status, apiError) => {
                errorList.addError(apiError)
            })
        }

        return {
            name,
            roomSize,
            submitHandler,
            errorList,
        }
    },
    template: `
      <table width="100%" cellpadding="0" cellspacing="4" border="0">
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">{{ $t('rooms.create.name') }}</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><input type="text" size="40" maxlength="80" v-model="name" placeholder="todo i18n"/></td>
        </tr>
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">{{ $t('rooms.create.size') }}</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><input type="text" size="5" maxlength="5" v-model="roomSize"/></td>
        </tr>
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">&nbsp;</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><button @click="submitHandler">{{ $t('rooms.create.save') }}</button></td>
        </tr>
      </table>
    `
}