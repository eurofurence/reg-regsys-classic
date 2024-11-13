export const de_DE = {
    rooms: {
        list: {
            title: 'Zimmerliste',
            empty: 'Aktuell sind keine Zimmer definiert. Du solltest welche anlegen.',
            header: {
                no: "Nr.",
                name: "Name",
                size: "Größe",
                final: "Final?",
                comments: "Kommentar",
            },
            click: 'Zimmer anklicken, um zu editieren',
        },
        create: {
            title: 'Neues Zimmer',
            name: 'Name',
            namehint: 'bitte Namen eingeben',
            size: 'Bettenzahl',
            comments: 'Kommentar',
            save: 'Neues Zimmer speichern!',
            cancel: 'Abbrechen',
        },
        edit: {
            title: 'Zimmer bearbeiten',
            name: 'Name',
            namehint: 'bitte Namen eingeben',
            size: 'Bettenzahl',
            comments: 'Kommentar',
            save: 'Änderungen Speichern!',
            cancel: 'Abbrechen',
        },
    },
    errors: {
        heading: {
            single: 'Der folgende Fehler trat bei der Verarbeitung auf:',
            multiple: 'Die folgenden {count} Fehler traten bei der Verarbeitung auf:',
        },
        message: {
            auth: {
                forbidden: 'kein Zugriff',
                unauthorized: 'nicht eingeloggt',
            },
            request: {
                unanswered: 'no response from server',
                setup: 'failed to set up request to server',
            },
            room: {
                data: {
                    duplicate: 'Doppelter Zimmername',
                    invalid: 'Ungültige Zimmerdaten',
                },
            },
            unknown: 'unbekannter Fehler',
            script: 'unerwarteter Javascript-Fehler',
        }
    },
    count: {
        heading: 'Anzahl ist',
        errors: 'Fehlerzahl',
    }
}
