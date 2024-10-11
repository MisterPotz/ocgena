export interface Rect {
    left: number
    top: number
    right: number
    bottom: number
}

export type Offsets = Rect

export interface RectangleShape {
    id: string
    width: number
    height: number
    type: "rectangle"
}

export interface CircleShape {
    id: string
    radius: number
    type: "circle"
}

export type Shape = RectangleShape | CircleShape

export interface Selector {
    elements: Shape[]
    topLeftElement: Shape
    bottomRightElement: Shape
    startX?: number
    startY?: number
    borders: Rect
}

export interface Transformer {
    element: Shape
    borders: Rect
    moveX?: number
    moveY?: number
    startX?: number
    startY?: number
}

export interface Space {
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

export function containsXY(rect: Rect, x: number, y: number) {
    return rect.left <= x && rect.top <= y && x <= rect.right && y <= rect.bottom
}

// export function leftBorder(positionable: Positionable) {
//     return positionable.footprintFromStart().left + positionable.x
// }

// export function rightBorder(positionable: Positionable) {
//     return positionable.footprintFromStart().right + positionable.x
// }

// export function topBorder(positionable: Positionable) {
//     return positionable.footprintFromStart().top + positionable.y
// }

// export function bottomBorder(positionable: Positionable) {
//     return positionable.footprintFromStart().bottom + positionable.y
// }

// export function borders(positionable: Positionable): Rect {
//     return {
//         left: leftBorder(positionable),
//         right: rightBorder(positionable),
//         top: topBorder(positionable),
//         bottom: bottomBorder(positionable),
//     }
// }
