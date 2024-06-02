export interface DotFieldPoint {
  x: number
  y: number
}

export class DotField {
  minStepPx: number
  dotSize: number
  fieldOffsetX: number
  fieldOffsetY: number
  inCellDotCoordinate: number

  private static _instance: DotField;


  constructor(minStepPx?: number) {
    this.minStepPx = minStepPx ? minStepPx : 20
    this.dotSize = 2
    this.fieldOffsetX = 0
    this.fieldOffsetY = 0
    this.inCellDotCoordinate = this.minStepPx / 2
  }

  getClosestDot(x: number, y: number): DotFieldPoint {
    return {
      x: this.getClosestX(x),
      y: this.getClosestY(y),
    }
  }

  getClosestX(x: number): number {
    const approxCellIndexX = Math.floor(
      (this.fieldOffsetX + x) / this.minStepPx,
    )
    return approxCellIndexX * this.minStepPx + this.inCellDotCoordinate
  }

  getClosestY(y: number): number {
    const approxCellIndexY = Math.floor(
      (this.fieldOffsetY + y) / this.minStepPx,
    )
    return approxCellIndexY * this.minStepPx + this.inCellDotCoordinate
  }

  getClosestSize(size: number): number {
    return Math.round(size / this.minStepPx) * this.minStepPx
  }

  public static Instance(minStepPx?: number) : DotField {
    return this._instance || (this._instance = new this(minStepPx));
  }
}

export const dots = DotField.Instance()
