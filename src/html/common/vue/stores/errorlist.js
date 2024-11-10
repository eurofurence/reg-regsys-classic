const { reactive } = Vue

export const StoredErrorList = reactive({
    errors: [],
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
