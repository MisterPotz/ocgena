import { Circle, Positionable, PositionableShape } from "./SpaceModel"

export class PositionablesRepository {
    private positionables: Map<string, Positionable> = new Map()
    private positionableShapes: Map<string, PositionableShape> = new Map()

    getPositionables(shapes: PositionableShape[]) {
        const arr = []

        for (const shape of shapes) {
            arr.push(this.positionables.get(shape.id)!)
        }
        return arr
    }

    getPositionable(id: string) {
        return this.positionables.get(id)!
    }

    mapToShapes(): PositionableShape[] {
        const arr = []
        for (const [id, positionable] of this.positionables) {
            arr.push(positionable.toPositionableShape())
        }
        return arr
    }

    updateShapes() {
        for (const [id, positionable] of this.positionables) {
            const shape = this.positionableShapes.get(id)!

            
        }
    }
}