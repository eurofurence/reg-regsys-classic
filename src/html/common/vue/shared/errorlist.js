import { StoredErrorList } from "../stores/errorlist.js";

const { computed } = Vue
const { useI18n } = VueI18n

export const ErrorList = {
    setup() {
        const { t, te } = useI18n()

        const errorList = StoredErrorList
        // computed function without parameters
        const headingMessage = computed(() => {
            return t(
                StoredErrorList.errors.length === 1 ? 'errors.heading.single' : 'errors.heading.multiple',
                { count: StoredErrorList.errors.length }
            )
        })
        // computed function with a parameter
        const translatedMessageCode = (code) => computed(() => {
            const fullCode = 'errors.message.'+code
            return te(fullCode) ? t(fullCode) : '??'
        })

        return {
            t,
            errorList,
            headingMessage,
            translatedMessageCode,
        }
    },
    template: `
<table v-if="errorList.errors.length > 0" border="0" class="error">
  <tr>
    <th class="error" colspan="4">{{ headingMessage }}</th>            
  </tr>
  <tr v-for="e in errorList.errors">
    <td class="error">{{ translatedMessageCode(e.message) }}</td>
    <td class="error">{{ e.message }}</td>
    <td class="error">{{ e.details }}</td>
    <td class="error">{{ e.requestid }}</td>
  </tr>
</table>
`
}
