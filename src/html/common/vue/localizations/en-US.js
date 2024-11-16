export const en_US = {
    groups: {
        list: {
            title: 'Current Groups',
            header: {
                no: "No.",
                name: "Name",
                size: "Size",
                public: "Public?",
                wheelchair: "Wheelchair?",
                comments: "Comment",
                members: "Members",
                invited: "Invitations",
            },
            info: 'Click on a group to edit it. Click on a column to filter/sort by it.',
            filter: 'Filter',
        },
        create: {
            title: 'Create Group',
            name: 'Name',
            namehint: 'please enter a name',
            size: 'Max.Size',
            owner: 'Owner',
            comments: 'Comments',
            flags: 'Flags',
            public: 'Public',
            wheelchair: 'Wheelchair',
            save: 'Save new group!',
            cancel: 'Cancel!',
        },
        edit: {
            title: 'Edit Group',
            name: 'Name',
            namehint: 'please enter a name',
            size: 'Max.Size',
            owner: 'Owner',
            comments: 'Comments',
            flags: 'Flags',
            public: 'Public',
            wheelchair: 'Wheelchair',
            save: 'Save changes!',
            cancel: 'Cancel!',
        },
    },
    rooms: {
        list: {
            title: 'Current Rooms',
            header: {
                no: "No.",
                name: "Name",
                size: "Size",
                final: "Final?",
                handicapped: "HC.ok.?",
                comments: "Comment",
                occupants: "Occupants",
            },
            info: 'Click on a room to edit it. Click on a column to filter/sort by it.',
            filter: 'Filter',
        },
        create: {
            title: 'Create Room',
            name: 'Name',
            namehint: 'please enter a name',
            size: 'Beds',
            comments: 'Comments',
            flags: 'Flags',
            final: 'Final',
            handicapped: 'Handicapped',
            save: 'Save new room!',
            cancel: 'Cancel!',
        },
        edit: {
            title: 'Edit Room',
            name: 'Name',
            namehint: 'please enter a name',
            size: 'Beds',
            comments: 'Comments',
            flags: 'Flags',
            final: 'Final',
            handicapped: 'Handicapped',
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
