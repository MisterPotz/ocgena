import { Keys } from "./SpaceModel"

export class CombinedPressedKeyChecker {
    private pressedKeysSet: Set<Keys> = new Set<Keys>()
    private plusKeys: Set<Keys> = new Set<Keys>()
    private minusKeys: Set<Keys> = new Set<Keys>()
    private updatedKeys: Set<Keys> = new Set<Keys>()
    private arrToSet(keys: Keys[]): Set<Keys> {
        const checkKeysSet = new Set<Keys>()
        for (const key of keys) {
            checkKeysSet.add(key)
        }
        return checkKeysSet
    }

    private setToArr(keys: Set<Keys>): Keys[] {
        const arr: Keys[] = []
        for (const key of keys) {
            arr.push(key)
        }
        return arr
    }

    checkBecameOnlyCombination(keys: Keys[]) {
        const checkKeysSet = this.arrToSet(keys)
        if (
            this.updatedKeys.size === checkKeysSet.size &&
            this.updatedKeys.symmetricDifference(checkKeysSet).size === 0 &&
            this.pressedKeysSet.symmetricDifference(checkKeysSet).size > 0
        ) {
            console.log(
                "checkBecameOnlyCombination, symmetric diff",
                this.pressedKeysSet.symmetricDifference(checkKeysSet).size,
            )
            return true
        }
        return false
    }

    checkStoppedBeingCombination(keys: Keys[]) {
        const checkKeysSet = this.arrToSet(keys)

        if (
            this.pressedKeysSet.size === checkKeysSet.size &&
            this.pressedKeysSet.symmetricDifference(checkKeysSet).size === 0 &&
            (this.updatedKeys.size !== checkKeysSet.size ||
                this.updatedKeys.symmetricDifference(checkKeysSet).size > 0)
        ) {
            return true
        }
        return false
    }
    updateKeys(pressedKeys: Keys[], newDownButtons: Keys[], newReleaseButtons: Keys[]) {
        this.clear()

        this.updatePressedKeys(pressedKeys)
        this.updatePlusKeys(newDownButtons)
        this.updateMinusKeys(newReleaseButtons)
        this.updateCompiledKeys()

        return this
    }

    private clear() {
        this.plusKeys.clear()
        this.updatedKeys.clear()
        this.minusKeys.clear()
        this.pressedKeysSet.clear()
        return this
    }

    private updatePressedKeys(keysSet: Keys[]) {
        this.clear()
        for (const key of keysSet) {
            this.pressedKeysSet.add(key)
        }
        return this
    }

    private updatePlusKeys(plusKeys: Keys[]) {
        this.plusKeys.clear()
        for (const key of plusKeys) {
            this.plusKeys.add(key)
        }
        return this
    }

    private updateMinusKeys(minusKeys: Keys[]) {
        this.minusKeys.clear()
        for (const key of minusKeys) {
            this.minusKeys.add(key)
        }
        return this
    }

    private updateCompiledKeys() {
        this.updatedKeys.clear()
        this.updatedKeys = this.pressedKeysSet.union(this.plusKeys).difference(this.minusKeys)
        return this
    }

    getNewKeys(): Keys[] {
        return this.setToArr(this.updatedKeys)
    }

    minusKeysArr() {
        return this.setToArr(this.minusKeys)
    }

    plusKeysArr() {
        return this.setToArr(this.plusKeys)
    }
}
