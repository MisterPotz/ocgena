import {
    PositionablesIndex as PositionablesPositionIndex,
    Positionable,
    leftBorder,
    rightBorder,
    topBorder,
    bottomBorder,
} from "./SpaceModel"

type SearchSpace = {
    leftIndex: number
    rightIndex: number
    array: Positionable[]
}
// maybe later rewrite into quadtree like algorithm
export class PositionablesPositionIndexImpl implements PositionablesPositionIndex {
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

    private getInRangeSearchSpace(
        leftBorder: number,
        rightBorder: number,
        array: Positionable[],
        selector: (positionable: Positionable) => number,
    ): SearchSpace | null {
        const leftIndex = this.binarySearchEqualOrBiggerThan(leftBorder, array, selector)
        const rightIndex = this.binarySearchEqualOrSmallerThan(rightBorder, array, selector)
        if (leftIndex === null || rightIndex === null || rightIndex - leftIndex < 0) return null

        return {
            leftIndex,
            rightIndex,
            array,
        }
    }

    private determineInRangeSearchSpace(
        left: number,
        top: number,
        right: number,
        bottom: number,
    ): SearchSpace | null {
        const lBorderSearchSpace = this.getInRangeSearchSpace(
            left,
            right,
            this.sortedByLeftBorder,
            pos => leftBorder(pos),
        )
        if (!lBorderSearchSpace) return null
        const rBorderSearchSpace = this.getInRangeSearchSpace(
            left,
            right,
            this.sortedByRightBorder,
            pos => rightBorder(pos),
        )
        if (!rBorderSearchSpace) return null
        const tBorderSearchSpace = this.getInRangeSearchSpace(
            top,
            bottom,
            this.sortedByTopBorder,
            pos => topBorder(pos),
        )
        if (!tBorderSearchSpace) return null
        const bBorderSearchSpace = this.getInRangeSearchSpace(
            top,
            bottom,
            this.sortedByBottomBorder,
            pos => bottomBorder(pos),
        )
        if (!bBorderSearchSpace) return null

        var smallestSearchSpace: SearchSpace = lBorderSearchSpace

        for (const searchSpace of [rBorderSearchSpace, tBorderSearchSpace, bBorderSearchSpace]) {
            if (
                smallestSearchSpace.rightIndex - smallestSearchSpace.leftIndex >=
                searchSpace.rightIndex - searchSpace.leftIndex
            ) {
                smallestSearchSpace = searchSpace
            }
        }
        return smallestSearchSpace
    }

    getPositionablesInRange(
        left: number,
        top: number,
        right: number,
        bottom: number,
    ): Positionable[] {
        const answer: Positionable[] = []
        const searchSpace = this.determineInRangeSearchSpace(left, top, right, bottom)

        if (!searchSpace) return answer

        for (var i = searchSpace.leftIndex; i <= searchSpace.rightIndex; i++) {
            const pos = searchSpace.array[i]
            if (
                left <= leftBorder(pos) &&
                top <= topBorder(pos) &&
                rightBorder(pos) <= right &&
                bottomBorder(pos) <= bottom
            ) {
                answer.push(searchSpace.array[i])
            }
        }

        return answer
    }

    getPositionablesOutOfRange(
        left: number,
        top: number,
        right: number,
        bottom: number,
    ): Positionable[] {
        const answer: Positionable[] = []
        const searchSpace = this.determineInRangeSearchSpace(left, top, right, bottom)

        if (!searchSpace) {
            return [...this.sortedByLeftBorder]
        }
        for (var i = 0; i < searchSpace.array.length; i++) {
            const pos = searchSpace.array[i]
            if (
                !(
                    left <= leftBorder(pos) &&
                    top <= topBorder(pos) &&
                    rightBorder(pos) <= right &&
                    bottomBorder(pos) <= bottom
                )
            ) {
                answer.push(searchSpace.array[i])
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
        this.sortedInsert(positionable, compareXYZ, this.sortedByXY)
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

    insertPack(positionables: Positionable[]) {
        for (const pos of positionables) {
            this.insert(pos)
        }
    }

    removePack(positionables: Positionable[]) {
        for (const pos of positionables) {
            this.remove(pos)
        }
    }


    private compareYXZ(positionable1: Positionable, positionable2: Positionable) {
        return compareBy(
            positionable1,
            positionable2,
            el => el.y,
            el => el.x,
            el => -el.z,
        )
    }

    private compareLeftBorder(positionable1: Positionable, positionable2: Positionable) {
        return compareBy(
            positionable1,
            positionable2,
            el => leftBorder(el),
            el => -el.z,
        )
    }

    private compareRightBorder(positionable1: Positionable, positionable2: Positionable) {
        return compareBy(
            positionable1,
            positionable2,
            el => rightBorder(el),
            el => -el.z,
        )
    }

    private compareTopBorder(positionable1: Positionable, positionable2: Positionable) {
        return compareBy(
            positionable1,
            positionable2,
            el => topBorder(el),
            el => -el.z,
        )
    }

    private compareBottomBorder(positionable1: Positionable, positionable2: Positionable) {
        return compareBy(
            positionable1,
            positionable2,
            el => bottomBorder(el),
            el => -el.z,
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

    getByCoordinate(x: number, y: number): Positionable | null {
        const leftBorderCandidatesEndIndex = this.binarySearchEqualOrSmallerThan(
            x,
            this.sortedByLeftBorder,
            el => leftBorder(el),
        )
        if (leftBorderCandidatesEndIndex === null) return null
        const rightBorderCandidatesStartIndex = this.binarySearchEqualOrBiggerThan(
            x,
            this.sortedByRightBorder,
            el => rightBorder(el),
        )
        if (rightBorderCandidatesStartIndex === null) return null
        const topBorderCandidatesEndIndex = this.binarySearchEqualOrSmallerThan(
            y,
            this.sortedByTopBorder,
            el => topBorder(el),
        )
        if (topBorderCandidatesEndIndex === null) return null
        const bottomBorderCandidatesStartIndex = this.binarySearchEqualOrBiggerThan(
            y,
            this.sortedByBottomBorder,
            el => bottomBorder(el),
        )
        if (bottomBorderCandidatesStartIndex === null) return null

        var smallest = leftBorderCandidatesEndIndex + 1
        for (const spaceSize of [
            this.sortedByRightBorder.length - rightBorderCandidatesStartIndex,
            topBorderCandidatesEndIndex + 1,
            this.sortedByBottomBorder.length - bottomBorderCandidatesStartIndex,
        ]) {
            if (spaceSize < smallest) {
                smallest = spaceSize
            }
        }
        var startIndex: number
        var endIndex: number
        var array: Positionable[]
        switch (smallest) {
            case leftBorderCandidatesEndIndex + 1: {
                startIndex = 0
                endIndex = leftBorderCandidatesEndIndex
                array = this.sortedByLeftBorder
                break
            }
            case this.sortedByRightBorder.length - rightBorderCandidatesStartIndex: {
                startIndex = rightBorderCandidatesStartIndex
                endIndex = this.sortedByRightBorder.length - 1
                array = this.sortedByRightBorder
                break
            }
            case topBorderCandidatesEndIndex + 1: {
                startIndex = 0
                endIndex = topBorderCandidatesEndIndex
                array = this.sortedByTopBorder
                break
            }
            case this.sortedByBottomBorder.length - bottomBorderCandidatesStartIndex: {
                startIndex = bottomBorderCandidatesStartIndex
                endIndex = this.sortedByBottomBorder.length - 1
                array = this.sortedByBottomBorder
                break
            }
            default: {
                return null
            }
        }
        for (var i = startIndex; i <= endIndex; i++) {
            if (array[i].containsXY(x, y)) {
                return array[i]
            }
        }
        return null
    }
}

function compare(any1: any, any2: any) {
    if (any1 < any2) {
        return -1
    }
    if (any1 > any2) {
        return 1
    }
    return 0
}

function compareBy(
    positionable1: Positionable,
    positionable2: Positionable,
    ...criteria: ((pos: Positionable) => any)[]
) {
    for (const crit of criteria) {
        const comparison = compare(crit(positionable1), crit(positionable2))
        if (comparison !== 0) {
            return comparison
        }
    }
    return 0
}

export function compareXYZ(positionable1: Positionable, positionable2: Positionable) {
    return compareBy(
        positionable1,
        positionable2,
        el => el.x,
        el => el.y,
        el => -el.z,
    )
}

export function compareTopLeft(positionable1: Positionable, positionable2: Positionable) {
    return compareBy(
        positionable1,
        positionable2,
        el => leftBorder(el),
        el => topBorder(el)
    )
}

export function compareBottomRight(positionable1: Positionable, positionable2: Positionable) {
    return compareBy(
        positionable1,
        positionable2,
        el => rightBorder(el),
        el => bottomBorder(el)
    )
}