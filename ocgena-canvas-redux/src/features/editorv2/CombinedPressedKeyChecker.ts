import { Keys } from "./SpaceModel";

export class CombinedPressedKeyChecker {
    private pressedKeysSet: Set<Keys> = new Set<Keys>();
    private plusKeys: Set<Keys> = new Set<Keys>();
    private minusKeys: Set<Keys> = new Set<Keys>();

    checkArePressed(keys: Keys[]) {
        for (const key of keys) {
            if (!this.pressedKeysSet.has(key)) {
                return false;
            }
        }
        return true;
    }

    checkBecamePressed(keys: Keys[]) {
        var atLeastOneInPlus = false;

        for (const key of keys) {
            const isAlreadyPressed = this.pressedKeysSet.has(key);
            const inPlus = this.plusKeys.has(key);
            const inMinus = this.minusKeys.has(key);
            if (inPlus && !isAlreadyPressed && !inMinus) {
                atLeastOneInPlus = true;
            }
        }
        return atLeastOneInPlus;
    }

    checkBecameUnpressed(keys: Keys[]) {
        var atLeastOneInMinus = false;
        for (const key of keys) {
            const isAlreadyPressed = this.pressedKeysSet.has(key);
            const inPlus = this.plusKeys.has(key);
            const inMinus = this.minusKeys.has(key);
            if (inMinus && isAlreadyPressed && !inPlus) {
                atLeastOneInMinus = true;
            }
        }
        return atLeastOneInMinus;
    }

    updatePressedKeys(keysSet: Keys[]) {
        this.plusKeys.clear();
        this.minusKeys.clear();
        this.pressedKeysSet.clear();
        for (const key of keysSet) {
            this.pressedKeysSet.add(key)
        }
        return this;
    }

    updatePlusKeys(plusKeys: Keys[]) {
        this.plusKeys.clear();
        for (const key of plusKeys) {
            this.plusKeys.add(key);
        }
        return this;
    }

    updateMinusKeys(minusKeys: Keys[]) {
        this.minusKeys.clear();
        for (const key of minusKeys) {
            this.minusKeys.add(key);
        }
        return this;
    }

    compileNewKeys() : Keys[] {
        const ans : Keys[] = []
        for (const key of this.pressedKeysSet.union(this.plusKeys).difference(this.minusKeys)) {
            ans.push(key)
        }
        return ans
    }
}
