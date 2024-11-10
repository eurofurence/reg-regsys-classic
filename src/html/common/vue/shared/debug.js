var debugEnable = false

export const debug = (...p) => {
    if (debugEnable) {
        console.log(p)
    }
}
