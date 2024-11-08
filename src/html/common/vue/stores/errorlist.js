const { reactive } = Vue

export const StoredErrorList = reactive({
    errors: [{
        requestid: '12345678',
        message: 'something',
        details: {
            details: ['something has happened'],
        },
    }],
    resetErrors() {
        this.errors = []
    },
    addError(apiError) {
        this.errors.push({
            requestid: apiError.requestid ?? '00000000',
            message: apiError.message ?? 'unknown',
            details: apiError.details ?? {},
        })
    },
})
