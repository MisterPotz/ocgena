import { inRangeIncInc } from "./editorv2Slice";
import { PositionablesIndex, Positionable } from "./SpaceModel";

export class PositionablesIndexImpl implements PositionablesIndex {
    private sortedByXY: Positionable[] = [];
    private sortedByYX: Positionable[] = [];

    constructor() {
    }

    binarySearchEqualOrBigger(
        value: number,
        positionables: Positionable[],
        selector: (positionable: Positionable) => number
    ): number | null {
        if (positionables.length == 0) return null;
        if (selector(positionables[0]) >= value) return 0;
        if (selector(positionables[positionables.length - 1]) < value) return null;

        var left = 0;
        var middle = 0;
        var right = positionables.length - 1;

        while (left <= right) {
            middle = ((right - left) << 1) + left;
            if (selector(positionables[middle]) < value) {
                left = middle + 1;
            } else if (selector(positionables[middle]) > value) {
                right = middle - 1;
            } else {
                for (var i = middle - 1; i >= 0; i--) {
                    if (selector(positionables[i]) !== value) {
                        return i + 1;
                    }
                }
                return 0;
            }
        }
        if (selector(positionables[middle]) >= value) {
            return middle;
        }
        return middle + 1;
    }

    binarySearchEqualOrSmaller(
        value: number,
        positionables: Positionable[],
        selector: (positionable: Positionable) => number
    ): number | null {
        if (positionables.length == 0) return null;
        if (selector(positionables[positionables.length - 1]) <= value)
            return positionables.length - 1;
        if (selector(positionables[0]) > value) return null;

        var left = 0;
        var middle = 0;
        var right = positionables.length - 1;

        while (left <= right) {
            middle = ((right - left) << 1) + left;
            if (selector(positionables[middle]) < value) {
                left = middle + 1;
            } else if (selector(positionables[middle]) > value) {
                right = middle - 1;
            } else {
                for (var i = middle + 1; i < positionables.length; i++) {
                    if (selector(positionables[i]) !== value) {
                        return i - 1;
                    }
                }
                return positionables.length - 1;
            }
        }

        if (selector(positionables[middle]) <= value) {
            return middle;
        }
        return middle - 1;
    }

    getPositionablesInRange(
        left: number,
        top: number,
        right: number,
        bottom: number
    ): Positionable[] {
        var leftIndex: number | null = null;
        var rightIndex: number | null = null;
        var array: Positionable[] | null = null;
        const answer: Positionable[] = [];

        if (right - left <= bottom - top) {
            // traverse x array as its smaller
            leftIndex =
                this.binarySearchEqualOrBigger(
                    left,
                    this.sortedByXY,
                    pos => pos.footprintFromStart().left
                ) ?? this.sortedByXY.length;
            rightIndex =
                this.binarySearchEqualOrSmaller(
                    right,
                    this.sortedByXY,
                    pos => pos.footprintFromStart().right
                ) ?? -1;
            array = this.sortedByXY;

            for (var i = leftIndex; i <= rightIndex; i++) {
                if (inRangeIncInc(this.sortedByXY[i].y, top, bottom)) {
                    answer.push(this.sortedByXY[i]);
                }
            }
        } else {
            // traverse y array as its smaller
            leftIndex =
                this.binarySearchEqualOrBigger(
                    top,
                    this.sortedByYX,
                    pos => pos.footprintFromStart().top
                ) ?? this.sortedByYX.length;
            rightIndex =
                this.binarySearchEqualOrSmaller(
                    bottom,
                    this.sortedByYX,
                    pos => pos.footprintFromStart().bottom
                ) ?? -1;
            array = this.sortedByYX;

            for (var i = leftIndex; i <= rightIndex; i++) {
                if (inRangeIncInc(this.sortedByYX[i].x, left, right)) {
                    answer.push(this.sortedByYX[i]);
                }
            }
        }

        return answer;
    }

    insert(positionable: Positionable) {
        if (this.sortedByXY.length == 0) {
            this.sortedByXY.push(positionable);
            this.sortedByYX.push(positionable);
            return;
        }

        var inserted = false;
        for (var i = 0; i < this.sortedByXY.length; i++) {
            if (this.compareXYZ(positionable, this.sortedByXY[i]) != 1) {
                this.sortedByXY.splice(i, 0, positionable);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            this.sortedByXY.push(positionable);
        }
        inserted = false;
        for (var i = 0; i < this.sortedByYX.length; i++) {
            if (this.compareYXZ(positionable, this.sortedByYX[i]) != 1) {
                this.sortedByYX.splice(i, 0, positionable);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            this.sortedByYX.push(positionable);
        }
    }

    remove(positionable: Positionable) {
        for (var i = 0; i < this.sortedByXY.length; i++) {
            if (positionable.id === this.sortedByXY[i].id) {
                this.sortedByXY.splice(i, 1);
                break;
            }
        }
        for (var i = 0; i < this.sortedByYX.length; i++) {
            if (positionable.id === this.sortedByYX[i].id) {
                this.sortedByYX.splice(i, 1);
                break;
            }
        }
    }

    compare(any1: any, any2: any) {
        if (any1 < any2) {
            return -1;
        }
        if (any1 > any2) {
            return 1;
        }
        return 0;
    }

    compareBy(positionable1: Positionable, positionable2: Positionable, ...criteria: string[]) {
        for (const crit of criteria) {
            const comparison = this.compare(
                (positionable1 as any)[crit],
                (positionable2 as any)[crit]
            );
            if (comparison !== 0) {
                return comparison;
            }
        }
        return 0;
    }

    compareXYZ(positionable1: Positionable, positionable2: Positionable) {
        return this.compareBy(positionable1, positionable2, "x", "y", "z");
    }

    compareYXZ(positionable1: Positionable, positionable2: Positionable) {
        return this.compareBy(positionable1, positionable2, "y", "x", "z");
    }

    getById(id?: string): Positionable | null {
        for (const positionable of this.sortedByXY) {
            if (positionable.id === id) {
                return positionable;
            }
        }
        return null;
    }
}
