class Enum {
    constructor(...enums) {
        for (let i = 0; i < enums.length; i++) {
            this[enums[i]] = enums[i];
        }
    }
}
