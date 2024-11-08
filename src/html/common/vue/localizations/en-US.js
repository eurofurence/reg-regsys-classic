export const en_US = {
    rooms: {
        app: {
            title: 'Manage Rooms',
        },
        list: {
            empty: 'There are currently no rooms. You should create one.',
            header: {
                no: "No.",
                name: "Name",
                size: "Size",
                final: "Final?",
                comments: "Comment",
            },
        },
        create: {
            title: 'Create Room',
            name: 'Name',
            size: 'Beds',
            save: 'Save!',
        },
    },
    errors: {
        heading: {
            single: 'The following error has occured processing your last request:',
            multiple: 'The following {count} errors have occured processing your last request:',
        },
        message: {
            auth: {
                forbidden: 'access denied',
                unauthorized: 'not logged in',
            },
            request: {
                unanswered: 'no response from server',
                setup: 'failed to set up request to server',
            },
            room: {
                data: {
                    invalid: 'room data invalid',
                },
            },
            unknown: 'unknown error',
            something: 'default error',
        }
    },
    count: {
        heading: 'Count is',
        errors: 'Error count',
    }
}
