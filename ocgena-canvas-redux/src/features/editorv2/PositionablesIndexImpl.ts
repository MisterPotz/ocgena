import { PositionablesIndex, Positionable } from "./SpaceModel"

type SearchSpace = {   
        leftIndex: number
        rightIndex: number
        array: Positionable[],
}
// maybe later rewrite into quadtree like algorithm
export class PositionablesIndexImpl implements PositionablesIndex {
    private sortedByXY: Positionable[] = []
    private sortedByYX: Positionable[] = []
    private sortedByLeftBorder: Positionable[] = []
    private sortedByRightBorder: Positionable[] = []
    private sortedByTopBorder: Positionable[] = []
    private sortedByBottomBorder: Positionable[] = []

    constructor() {}

    private binarySearchEqualOrBiggerThan(
        value: number,
        positionables: Positionable[],
        selector: (positionable: Positionable) => number,
    ): number | null {
        if (positionables.length == 0) return null
        if (selector(positionables[0]) >= value) return 0
        if (selector(positionables[positionables.length - 1]) < value) return null

        var left = 0
        var middle = 0
        var right = positionables.length - 1

        while (left <= right) {
            middle = ((right - left) >> 1) + left
            if (selector(positionables[middle]) < value) {
                left = middle + 1
            } else if (selector(positionables[middle]) > value) {
                right = middle - 1
            } else {
                for (var i = middle - 1; i >= 0; i--) {
                    if (selector(positionables[i]) !== value) {
                        return i + 1
                    }
                }
                return 0
            }
        }
        if (selector(positionables[middle]) >= value) {
            return middle
        }
        return middle + 1
    }

    private binarySearchEqualOrSmallerThan(
        value: number,
        positionables: Positionable[],
        selector: (positionable: Positionable) => number,
    ): number | null {
        if (positionables.length == 0) return null
        if (selector(positionables[positionables.length - 1]) <= value)
            return positionables.length - 1
        if (selector(positionables[0]) > value) return null

        var left = 0
        var middle = 0
        var right = positionables.length - 1

        while (left <= right) {
            middle = ((right - left) >> 1) + left
            if (selector(positionables[middle]) < value) {
                left = middle + 1
            } else if (selector(positionables[middle]) > value) {
                right = middle - 1
            } else {
                for (var i = middle + 1; i < positionables.length; i++) {
                    if (selector(positionables[i]) !== value) {
                        return i - 1
                    }
                }
                return positionables.length - 1
            }
        }

        if (selector(positionables[middle]) <= value) {
            return middle
        }
        return middle - 1
    }

    private getSearchSpace(
        leftBorder: number,
        rightBorder: number,
        array: Positionable[],
        selector: (positionable: Positionable) => number,
    ): SearchSpace | null {
        const leftIndex = this.binarySearchEqualOrBiggerThan(leftBorder, array, selector)
        const rightIndex = this.binarySearchEqualOrSmallerThan(rightBorder, array, selector)
        if (!leftIndex || !rightIndex || rightIndex - leftIndex < 0) return null

        return {
            leftIndex,
            rightIndex,
            array
        }
    }

    getPositionablesInRange(
        left: number,
        top: number,
        right: number,
        bottom: number,
    ): Positionable[] {
        const answer: Positionable[] = []

        const lBorderSearchSpace = this.getSearchSpace(left, right, this.sortedByLeftBorder, pos => pos.footprintFromStart().left + pos.x)
        if (!lBorderSearchSpace) return answer
        const rBorderSearchSpace = this.getSearchSpace(left, right, this.sortedByRightBorder, pos => pos.footprintFromStart().right + pos.x)
        if (!rBorderSearchSpace) return answer
        const tBorderSearchSpace = this.getSearchSpace(top, bottom, this.sortedByTopBorder, pos => pos.footprintFromStart().top + pos.y)
        if (!tBorderSearchSpace) return answer
        const bBorderSearchSpace = this.getSearchSpace(top, bottom, this.sortedByBottomBorder, pos => pos.footprintFromStart().bottom + pos.y)
        if (!bBorderSearchSpace) return answer

        var smallestSearchSpace : SearchSpace = lBorderSearchSpace

        for (const searchSpace of [rBorderSearchSpace, tBorderSearchSpace, bBorderSearchSpace]) {
            if (smallestSearchSpace.rightIndex - smallestSearchSpace.leftIndex >= searchSpace.rightIndex - searchSpace.leftIndex) {
                smallestSearchSpace = searchSpace
            }
        }

        for (var i = smallestSearchSpace.leftIndex; i <= smallestSearchSpace.rightIndex; i++) {
            const pos = smallestSearchSpace.array[i]
            if (
                left <= pos.footprintFromStart().left + pos.x &&
                top <= pos.footprintFromStart().top + pos.y &&
                pos.footprintFromStart().right + pos.x <= right &&
                pos.footprintFromStart().bottom + pos.y <= bottom
            ) {
                answer.push(smallestSearchSpace.array[i])
            }
        }

        return answer
    }

    private sortedInsert(
        positionable: Positionable,
        comparison: (pos1: Positionable, pos2: Positionable) => number,
        array: Positionable[],
    ) {
        if (array.length == 0) {
            array.push(positionable)
            return
        }

        for (var i = 0; i < array.length; i++) {
            if (comparison(positionable, array[i]) != 1) {
                array.splice(i, 0, positionable)
                return
            }
        }
        array.push(positionable)
    }

    private removeFromArr(positionable: Positionable, array: Positionable[]) {
        for (var i = 0; i < array.length; i++) {
            if (positionable.id === array[i].id) {
                array.splice(i, 1)
                break
            }
        }
    }

    insert(positionable: Positionable) {
        this.sortedInsert(positionable, this.compareXYZ, this.sortedByXY)
        this.sortedInsert(positionable, this.compareYXZ, this.sortedByYX)
        this.sortedInsert(positionable, this.compareLeftBorder, this.sortedByLeftBorder)
        this.sortedInsert(positionable, this.compareRightBorder, this.sortedByRightBorder)
        this.sortedInsert(positionable, this.compareTopBorder, this.sortedByTopBorder)
        this.sortedInsert(positionable, this.compareBottomBorder, this.sortedByBottomBorder)
    }

    remove(positionable: Positionable) {
        this.removeFromArr(positionable, this.sortedByXY)
        this.removeFromArr(positionable, this.sortedByYX)
        this.removeFromArr(positionable, this.sortedByLeftBorder)
        this.removeFromArr(positionable, this.sortedByRightBorder)
        this.removeFromArr(positionable, this.sortedByTopBorder)
        this.removeFromArr(positionable, this.sortedByBottomBorder)
    }

    private compare(any1: any, any2: any) {
        if (any1 < any2) {
            return -1
        }
        if (any1 > any2) {
            return 1
        }
        return 0
    }

    private compareBy(
        positionable1: Positionable,
        positionable2: Positionable,
        ...criteria: ((pos: Positionable) => any)[]
    ) {
        for (const crit of criteria) {
            const comparison = this.compare(crit(positionable1), crit(positionable2))
            if (comparison !== 0) {
                return comparison
            }
        }
        return 0
    }

    private compareXYZ(positionable1: Positionable, positionable2: Positionable) {
        return this.compareBy(
            positionable1,
            positionable2,
            el => el.x,
            el => el.y,
            el => el.z,
        )
    }

    private compareYXZ(positionable1: Positionable, positionable2: Positionable) {
        return this.compareBy(
            positionable1,
            positionable2,
            el => el.y,
            el => el.x,
            el => el.z,
        )
    }

    private compareLeftBorder(positionable1: Positionable, positionable2: Positionable) {
        return this.compareBy(
            positionable1,
            positionable2,
            el => el.x + el.footprintFromStart().left,
        )
    }

    private compareRightBorder(positionable1: Positionable, positionable2: Positionable) {
        return this.compareBy(
            positionable1,
            positionable2,
            el => el.x + el.footprintFromStart().right,
        )
    }

    private compareTopBorder(positionable1: Positionable, positionable2: Positionable) {
        return this.compareBy(
            positionable1,
            positionable2,
            el => el.y + el.footprintFromStart().top,
        )
    }

    private compareBottomBorder(positionable1: Positionable, positionable2: Positionable) {
        return this.compareBy(
            positionable1,
            positionable2,
            el => el.y + el.footprintFromStart().bottom,
        )
    }

    getById(id?: string): Positionable | null {
        for (const positionable of this.sortedByXY) {
            if (positionable.id === id) {
                return positionable
            }
        }
        return null
    }
}
