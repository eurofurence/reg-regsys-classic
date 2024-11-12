import { ErrorList } from '../shared/errorlist.js'
import { StoredErrorList } from '../stores/errorlist.js'
import { debug } from '../shared/debug.js'

export const App = {
    setup() {
        debug('App.setup')

        // set up an error so at least something shows up
        StoredErrorList.addError({
            details: {details: ['page not yet implemented']},
        })

        return {}
    },
    components: {
        ErrorList,
    },
    template: `
        <ErrorList />
   `
}
