import { debug } from '../shared/debug.js'
import { useRoomEditor } from '../use/useroomeditor.js'

const { watch } = Vue
const { useI18n } = VueI18n

export const RoomForm = {
    props: ['id'],
    emits: ['roomsPossiblyUpdated'],
    setup(props, { emit }) {
        debug('RoomForm.setup')

        const editor = useRoomEditor()

        const resetHandler = () => {
            debug('RoomForm.resetHandler')
            editor.setupForId(undefined, () => {
                // even a reset will at least de-select the currently edited room
                emit('roomsPossiblyUpdated')
            })
        }
        const submitHandler = () => {
            debug('RoomForm.submitHandler')
            editor.save((room) => {
                emit('roomsPossiblyUpdated')
            })
        }

        watch(() => props.id, (newIdValue, oldIdValue) => {
            debug('RoomForm.watch props.id changed', oldIdValue, newIdValue)
            // no need to trigger event, the update comes from the outside, also it would reset the form.
            editor.setupForId(newIdValue, undefined)
        })

        // formatting, style and i18n

        const { t } = useI18n()
        const tkey = (extension) => {
            return t(editor.isNew.value ? 'rooms.create.' + extension : 'rooms.edit.' + extension)
        }

        return {
            editor,
            resetHandler,
            submitHandler,
            t,
            tkey,
        }
    },
    template: `
      <div class="headline"><br/>{{ tkey('title') }}</div>
      <hr class="contentbox"/>
      <table width="100%" cellpadding="0" cellspacing="4" border="0">
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">{{ tkey('name') }}</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><input type="text" size="40" maxlength="80" v-model.trim="editor.name.value" :class="{ error: editor.isNameError.value }" :placeholder="tkey('namehint')"/></td>
        </tr>
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">{{ tkey('size') }}</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><input type="text" size="5" maxlength="5" v-model.trim="editor.size.value" :class="{ error: editor.isSizeError.value }"/></td>
        </tr>
        <tr>
          <td class="label" ALIGN="right" VALIGN="top" width="20%"><label for="room-final">{{ tkey('flags') }}</label></TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%">
            <TABLE BORDER="0" CELLPADDING="3" CELLSPACING="0" WIDTH="100%">
              <TR>
                <TD class="input" width="5%"><input id="room-final" type="checkbox" v-model="editor.final.value" class="check"/></TD>
                <TD class="input" width="45%"><label for="room-final">{{ tkey('final') }}</label></TD>
              </TR>
              <TR>
                <TD class="input" width="5%"><input id="room-handicapped" type="checkbox" v-model="editor.handicapped.value" class="check"/></TD>
                <TD class="input" width="45%"><label for="room-handicapped">{{ tkey('handicapped') }}</label></TD>
              </TR>
            </TABLE>
          </td>
        </tr>
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">{{ tkey('comments') }}</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><input type="text" size="40" maxlength="80" v-model.trim="editor.comments.value"/></td>
        </tr>
        <tr>
          <td class="label" ALIGN="right" VALIGN="middle" width="20%">&nbsp;</TD>
          <td class="input" ALIGN="left" VALIGN="middle" width="80%"><button :disabled="editor.isError.value" @click="submitHandler">{{ tkey('save') }}</button>&nbsp;<button @click="resetHandler">{{ tkey('cancel') }}</button></td>
        </tr>
      </table>
    `
}