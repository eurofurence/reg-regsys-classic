import { de_DE } from '../localizations/de-DE.js'
import { en_US } from '../localizations/en-US.js'
import { App } from './app.js'

const { createApp } = Vue
const { createI18n } = VueI18n

const params = new URL(document.location.toString()).searchParams;
const locale = params.get('lang') ?? 'en-US'

const i18n = createI18n({
    locale: locale,
    fallbackLocale: 'en-US',
    messages: {
        'en-US': en_US,
        'de-DE': de_DE,
    },
})

createApp(App).use(i18n).mount('#app')
