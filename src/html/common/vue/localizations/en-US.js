export const en_US = {
    rooms: {
        list: {
            title: 'Current Rooms',
            empty: 'There are currently no rooms. You should create one.',
            header: {
                no: "No.",
                name: "Name",
                size: "Size",
                final: "Final?",
                comments: "Comment",
            },
            click: 'Click on a room to edit it',
        },
        create: {
            title: 'Create Room',
            name: 'Name',
            namehint: 'please enter a name',
            size: 'Beds',
            comments: 'Comments',
            save: 'Save new room!',
            cancel: 'Cancel!',
        },
        edit: {
            title: 'Edit Room',
            name: 'Name',
            namehint: 'please enter a name',
            size: 'Beds',
            comments: 'Comments',
            save: 'Save changes!',
            cancel: 'Cancel!',
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
                    duplicate: 'duplicate room name',
                    invalid: 'room data invalid',
                },
            },
            unknown: 'unknown error',
            script: 'unexpected javascript error',
        }
    },
    count: {
        heading: 'Count is',
        errors: 'Error count',
    }
}
