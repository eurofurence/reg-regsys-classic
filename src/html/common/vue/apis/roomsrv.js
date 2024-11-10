const apiCall = ( method, path, params, body, success2xxHandler, apiErrorHandler ) => {
    axios({
        url: path,
        method: method,
        baseURL: 'http://localhost:10000/roomsrv/api/rest/v1',
        params: params,
        data: body,
        timeout: 30000,
        withCredentials: true,
    }).then(function (response) {
        // console.log(response);
        // only called if 2xx response status
        success2xxHandler(response)
    }).catch(function (error) {
        // console.log(error);
        if (error.response) {
            // request was made with response status non-2xx
            apiErrorHandler(error.response.status, error.response.data)
        } else if (error.request) {
            // request was made but no response was received
            apiErrorHandler(0, {
                message: 'request.unanswered',
                details: {
                    details: ['failed to receive a response', error.message],
                }
            })
        } else {
            apiErrorHandler(0, {
                message: 'request.setup',
                details: {
                    details: ['failed to set up request', error.message],
                }
            })
        }
    })
}

export const ListAllRooms = ( setRooms, apiErrorHandler ) => {
    // console.log('ListAllRooms')
    apiCall('get', '/rooms', null, null, (response) => {
        // TODO move to a full success handler that gets the response
        if (response?.data?.rooms) {
            setRooms(response.data.rooms)
        }
    }, apiErrorHandler )
}

export const CreateRoom = ( room, successHandler, apiErrorHandler ) => {
    apiCall('post', '/rooms', null, room, (response) => {
        // TODO get id of new room from location and place into room, so handler knows which room to update
        room.id = ''
        successHandler(room)
    }, apiErrorHandler )
}

export const UpdateRoom = ( room, successHandler, apiErrorHandler ) => {
    apiCall('put', '/rooms/' + room.id, null, room, (response) => {
        successHandler(room)
    }, apiErrorHandler )
}

export const GetRoomByID = ( id, successHandler, apiErrorHandler ) => {
    apiCall('get', '/rooms/' + id, null, null,(response) => {
        successHandler(response.data)
    }, apiErrorHandler )
}
