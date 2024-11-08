import { StoredErrorList } from "../stores/errorlist.js";

export const ErrorList = {
    setup() {
        return {
            errorList: StoredErrorList, // get a local reference to the store ref
        }
    },
    template: `
<table v-if="errorList.errors" border="0" class="error">
  <tr>
    <th class="error" colspan="3">{{ $t( 
       errorList.errors.length === 1 ? 'errors.heading.single' : 'errors.heading.multiple', 
       { count: errorList.errors.length }
    ) }}</th>            
  </tr>
  <tr v-for="e in errorList.errors">
    <td class="error">{{ $t('errors.message.'+e.message) }} ({{ e.message }})</td>
    <td class="error">{{ e.details }}</td>
    <td class="error">{{ e.requestid }}</td>
  </tr>
</table>
`
}
