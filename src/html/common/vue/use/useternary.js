const { ref } = Vue

// useTernary provides a boolean that can also be undefined.
//
// we use this in a couple of places as a filter criterion.
// undefined means that no filtering should take place.
export const useTernary = (initial = undefined) => {
    const state = ref(initial)

    // cycle advances the ternary through its 3 possible states.
    //
    // Used in a click handler.
    const cycle = () => {
        if (state.value === true) {
            state.value = false
        } else if (state.value === false) {
            state.value = undefined
        } else {
            state.value = true
        }
    }

    // matches compares boolean b to our internal state.
    //
    // Returns true if b equals state.
    // Also returns true if state is undefined, no matter the value of b.
    const matches = (b) => {
        return state.value === undefined || state.value === b
    }

    // display takes 3 snippets and returns the one matching the state.
    //
    // Used when displaying the state.
    const display = (trueValue, falseValue, undefinedValue) => {
        if (state.value === true) {
            return trueValue
        } else if (state.value === false) {
            return falseValue
        } else {
            return undefinedValue
        }
    }

    return { state, cycle, matches, display }
}
