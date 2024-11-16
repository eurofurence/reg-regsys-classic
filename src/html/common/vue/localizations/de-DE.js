export const de_DE = {
    rooms: {
        list: {
            title: 'Zimmerliste',
            header: {
                no: "Nr.",
                name: "Name",
                size: "Größe",
                final: "Final?",
                handicapped: "Beh.ger.?",
                comments: "Kommentar",
                occupants: "Belegung",
            },
            info: 'Zimmer anklicken, um zu editieren. Spalte anklicken zum filtern/sortieren.',
            filter: 'Filter',
        },
        create: {
            title: 'Neues Zimmer',
            name: 'Name',
            namehint: 'bitte Namen eingeben',
            size: 'Bettenzahl',
            comments: 'Kommentar',
            flags: 'Eigenschaften',
            final: 'Final',
            handicapped: 'Behindertengerecht',
            save: 'Neues Zimmer speichern!',
            cancel: 'Abbrechen',
        },
        edit: {
            title: 'Zimmer bearbeiten',
            name: 'Name',
            namehint: 'bitte Namen eingeben',
            size: 'Bettenzahl',
            comments: 'Kommentar',
            flags: 'Eigenschaften',
            final: 'Final',
            handicapped: 'Behindertengerecht',
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
