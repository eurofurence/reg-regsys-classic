// main entry point of the roomshare group application, for use in a script tag of type module
//
// see templates groups_js.vm and groups.vm

import { de_DE } from '../localizations/de-DE.js'
import { en_US } from '../localizations/en-US.js'
import { App } from './app.js'
import { StoredErrorList } from '../stores/errorlist.js';
import { debug } from '../shared/debug.js'

const { createApp } = Vue
const { createI18n } = VueI18n

const params = new URL(document.location.toString()).searchParams;
const locale = params.get('lang') ?? 'en-US'
debug('index.js locale=', locale)

const i18n = createI18n({
    legacy: false,
    locale: locale,
    fallbackLocale: 'en-US',
    messages: {
        'en-US': en_US,
        'de-DE': de_DE,
    },
})

const app = createApp(App).use(i18n)
debug('index.js app created')

app.config.errorHandler = (err) => {
    StoredErrorList.addError({
        message: "script",
        details: {
            message: [ err.message ],
        },
    })
}
debug('index.js error handler added')

app.mount('#app')
debug('index.js app mounted')
