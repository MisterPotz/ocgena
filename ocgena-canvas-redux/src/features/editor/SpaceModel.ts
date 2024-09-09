interface Positionable {
  x: number
  y: number
  z: number
}

interface Rectangle extends Positionable {
  type: "rect"
  width: number
  height: number
}

interface Circle extends Positionable {
  type: "circle"
  radius: number
}

interface Selector {
  elements: Positionable[]
  moveX: number
  moveY: number
}

interface Transformer {
  element: Positionable
  startX: number
  startY: number
  endX: number
  endY: number
}

interface Space {
    positionables: Positionable[]
    selector: Selector | null
    transformer: Transformer | null
}

interface SpaceViewer {
  offsetX: number
  offsetY: number
}

type Keys = 'space' | 'left' | 'right'

interface Navigator {
  areaSelection: null | { startX: number; startY: number }
  pressedKeys: Set<Keys>
  x: number
  y: number
}
