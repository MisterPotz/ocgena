export interface Rect {
    left: number
    top: number
    right: number
    bottom: number
}

type Offsets = Rect

export interface Positionable {
    x: number
    y: number
    z: number
    id: string

    containsXY: (x: number, y: number) => boolean
    footprintFromStart(): Offsets
}

export class Rectangle implements Positionable {
    type!: "rect"
    private _width: number
    private _height: number
    z!: 1
    x!: 0
    y!: 0
    id: string
    offset: Offsets

    constructor(id: string, width: number, height: number) {
        this._width = width
        this._height = height
        this.offset = {
            left: 0,
            top: 0,
            right: width,
            bottom: height,
        }
        this.id = id
    }
    footprintFromStart(): Rect {
        return this.offset
    }

    containsXY(x: number, y: number) {
        return this.x <= x && x <= this.x + this.width && this.y <= y && y <= this.y + this.height
    }

    public set width(v: number) {
        this._width = v
        this.offset.right = v
    }

    public get width(): number {
        return this._width
    }

    public get height(): number {
        return this._height
    }

    public set height(v: number) {
        this._height = v
        this.offset.bottom = v
    }
}

export class Circle implements Positionable {
    type!: "circle"
    private _radius: number
    x!: 0
    y!: 0
    z!: 1
    id: string
    offsets: Rect

    constructor(id: string, radius: number) {
        this._radius = radius
        this.z = 1
        this.id = id
        this.offsets = {
            left: -radius,
            top: -radius,
            right: radius,
            bottom: radius,
        }
    }

    footprintFromStart(): Offsets {
        return this.offsets
    }

    containsXY(x: number, y: number) {
        const fastCheck =
            this.x - this.radius <= x &&
            x <= this.x + this.radius &&
            this.y - this.radius <= y &&
            y <= this.y + this.radius

        if (!fastCheck) return false

        return Math.sqrt((this.x - x) * (this.x - x) + (this.y - y) * (this.y - y)) <= this.radius
    }

    public get radius(): number {
        return this._radius
    }

    public set radius(v: number) {
        this._radius = v
        this.offsets = {
            left: -v,
            top: -v,
            right: v,
            bottom: v,
        }
    }
}

export type Shape = Rectangle | Circle

export interface Selector {
    elements: Positionable[]
    topLeftElement: Positionable
    bottomRightElement: Positionable
    startX?: number
    startY?: number
    borders: Rect
}

export interface Transformer {
    element: Positionable
    borders: Rect
    moveX?: number
    moveY?: number
    startX?: number
    startY?: number
}

export interface PositionablesIndex {
    getPositionablesInRange(
        left: number,
        top: number,
        right: number,
        bottom: number,
    ): Positionable[]
    insert(positionable: Positionable): void
    remove(positionable: Positionable): void
    getById(id?: string): Positionable | null
    getByCoordinate(x: number, y: number): Positionable | null
}

export interface Space {
    positionables: PositionablesIndex
    selector: Selector | null
    // transformer: Transformer | null
}

export interface SpaceViewer {
    offsetX: number
    offsetY: number
    startOffsetX?: number
    startOffsetY?: number
}

export type Keys = "space" | "left" | "right"

export type MouseKeys = "left" | "right"

export type ButtonKeys = "space"

export interface Navigator {
    areaSelection: null | { startX: number; startY: number }
    pressedKeys: Set<Keys>
    x: number
    y: number
}

export function containsXY(rect: Rect, x: number, y: number) {
    return rect.left <= x && rect.top <= y && x <= rect.right && y <= rect.bottom
}

export function leftBorder(positionable: Positionable) {
    return positionable.footprintFromStart().left + positionable.x
}

export function rightBorder(positionable: Positionable) {
    return positionable.footprintFromStart().right + positionable.x
}

export function topBorder(positionable: Positionable) {
    return positionable.footprintFromStart().top + positionable.y
}

export function bottomBorder(positionable: Positionable) {
    return positionable.footprintFromStart().bottom + positionable.y
}

export function borders(positionable: Positionable): Rect {
    return {
        left: leftBorder(positionable),
        right: rightBorder(positionable),
        top: topBorder(positionable),
        bottom: bottomBorder(positionable),
    }
}
