const apiCall = ( method, path, params, body, successParser, apiErrorHandler ) => {
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
        successParser(response.data)
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

export const ListAllRooms = ( addRooms, apiErrorHandler ) => {
    // console.log('ListAllRooms')
    apiCall('get', '/rooms', null, null, (data) => {
        if (data.rooms) {
            addRooms(data.rooms)
        }
    }, apiErrorHandler )
}

export const CreateRoom = ( room, apiErrorHandler ) => {
    apiCall('post', '/rooms', null, room, (data) => {}, apiErrorHandler )
}
