import { CombinedPressedKeyChecker } from "./CombinedPressedKeyChecker"
import { InteractionModeType } from "./EditorV2"
import { Keys } from "./SpaceModel"

export class ActiveModeDeterminer {
    keysChecker = new CombinedPressedKeyChecker()

    sortedConditions: { type: InteractionModeType; activationKeys: Keys[] }[] = []

    constructor(modeActivationConditions: { type: InteractionModeType; activationKeys: Keys[] }[]) {
        this.sortedConditions = modeActivationConditions.toSorted(
            (a, b) => a.activationKeys.length - b.activationKeys.length,
        )
    }

    determineNewMode(
        currentModeType: InteractionModeType | undefined | null,
        pressedKeys: Keys[],
        newDownButtons: Keys[],
        newReleaseButtons: Keys[],
    ) {
        this.keysChecker.updateKeys(pressedKeys, newDownButtons, newReleaseButtons)

        var newModeType: InteractionModeType | null | undefined = currentModeType
        var hasNewModeCandidate = false
        for (const mode of this.sortedConditions) {
            if (
                this.keysChecker.checkBecameOnlyCombination(mode.activationKeys) &&
                !hasNewModeCandidate
            ) {
                newModeType = mode.type
                hasNewModeCandidate = true
                break
            }
        }

        if (!hasNewModeCandidate) {
            for (const mode of this.sortedConditions) {
                if (
                    mode.type === currentModeType &&
                    this.keysChecker.checkStoppedBeingCombination(mode.activationKeys)
                ) {
                    newModeType = null
                    break
                }
            }
        }
        return newModeType
    }
}
