export interface Positionable {
  x: number
  y: number
  z: number
  id: string

  containsXY: (x: number, y: number) => boolean
}

export class Rectangle implements Positionable {
  type!: "rect"
  width: number
  height: number
  z!: 1
  x!: 0
  y!: 0
  id: string

  constructor(id: string, width: number, height: number) {
    this.width = width
    this.height = height
    this.id = id
  }
  containsXY(x: number, y: number) {
    return (
      this.x <= x &&
      x <= this.x + this.width &&
      this.y <= y &&
      y <= this.y + this.height
    )
  }
}

export class Circle implements Positionable {
  type!: "circle"
  radius: number
  x!: 0
  y!: 0
  z!: 1
  id: string

  constructor(id: string, radius: number) {
    this.radius = radius
    this.z = 1
    this.id = id
  }

  containsXY(x: number, y: number) {
    const fastCheck =
      this.x - this.radius <= x &&
      x <= this.x + this.radius &&
      this.y - this.radius <= y &&
      y <= this.y + this.radius

    if (!fastCheck) return false

    return (
      Math.sqrt((this.x - x) * (this.x - x) + (this.y - y) * (this.y - y)) <=
      this.radius
    )
  }
}

export type Shape = Rectangle | Circle

export interface Selector {
  elements: Positionable[]
  moveX: number
  moveY: number
}

export interface Transformer {
  element: Positionable
}

export interface Space {
  positionables: Positionable[]
  selector: Selector | null
  transformer: Transformer | null
}

export interface SpaceViewer {
  offsetX: number
  offsetY: number
}

export type Keys = "space" | "left" | "right"

export type MouseKeys = "left" | "right"

export interface Navigator {
  areaSelection: null | { startX: number; startY: number }
  pressedKeys: Set<Keys>
  x: number
  y: number
}

function findPositionableByCoordinate(space: Space, x: number, y: number) {
  let highestZ = Number.MIN_SAFE_INTEGER
  let highestPositionable = null

  for (const positionable of space.positionables) {
    if (positionable.z >= highestZ && positionable.containsXY(x, y)) {
      highestPositionable = positionable
      highestZ = positionable.z
    }
  }
  return highestPositionable
}

function mouseLeftClick(space: Space, x: number, y: number) {
  
}

function mouseRelease(space: Space, x: number, y: number) {

}

function containsXY(positionable: Positionable, x: number, y: number) {
  
}
