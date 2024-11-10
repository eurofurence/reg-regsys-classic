// main entry point of the room application, for use in a script tag of type module
//
// see templates rooms_js.vm and rooms.vm

import { de_DE } from '../localizations/de-DE.js'
import { en_US } from '../localizations/en-US.js'
import { App } from './app.js'
import { StoredErrorList } from '../stores/errorlist.js';

const { createApp } = Vue
const { createI18n } = VueI18n

const params = new URL(document.location.toString()).searchParams;
const locale = params.get('lang') ?? 'en-US'

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

app.config.errorHandler = (err) => {
    StoredErrorList.addError({
        message: "script",
        details: {
            message: [ err.message ],
        },
    })
}

app.mount('#app')
