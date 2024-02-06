import { useEffect, useRef } from "react";
import "./CanvasEditor.css";

interface ICoord {
  x: number;
  y: number;

  plusCopy(offset: { x?: number; y?: number }): ICoord;
  minusCopy(coord: Coord): ICoord;
  set(offset: Coord): any;
}

class Coord implements ICoord {
  x: number = 0;
  y: number = 0;

  constructor(df?: { x?: number; y?: number }) {
    this.x = df?.x ?? 0;
    this.y = df?.y ?? 0;
  }

  set(offset: Coord) {
    this.x = offset.x;
    this.y = offset.y;
  }

  plusCopy(offset: { x?: number; y?: number }) {
    return new Coord({
      x: this.x + (offset.x ?? 0),
      y: this.y + (offset.y ?? 0),
    });
  }

  minusCopy(coord: Coord): Coord {
    return new Coord({ x: this.x - coord.x, y: this.y - coord.y });
  }
}

interface Rect {
  height: number;
  width: number;
}

class ChildElement {
  shape: Shape;
  localCoord: ICoord;
  zCoord: number;

  constructor(args: { shape: Shape; localCoord?: ICoord; zCoord?: number }) {
    this.shape = args.shape;
    this.localCoord = args.localCoord ?? new Coord();
    this.zCoord = args.zCoord ?? 0;
  }
}

abstract class Shape {
  abstract isMineCoord(coordRelativeTopLeft: ICoord): Boolean;
  abstract draw(canvas: CanvasProxy): any;
}

class CanvasProxy {
  canvas: CanvasRenderingContext2D;
  currentOffset: ICoord = new Coord();

  constructor(canvas: CanvasRenderingContext2D) {
    this.canvas = canvas;
  }

  drawRect(rect: Rect) {
    this.canvas.fillRect(
      this.currentOffset.x,
      this.currentOffset.y,
      rect.width,
      rect.height
    );
  }
}

class Rectangle extends Shape {
  rect: Rect;

  constructor(rect: Rect = { height: 100, width: 100 }) {
    super();
    this.rect = rect;
  }

  draw(canvas: CanvasProxy) {
    canvas.drawRect(this.rect);
  }
  isMineCoord(coordRelativeTopLeft: Coord): Boolean {
    throw new Error("Method not implemented.");
  }
}

class ArrangementPainter {
  canvasProxy: CanvasProxy;

  constructor(canvasProxy: CanvasProxy) {
    this.canvasProxy = canvasProxy;
  }

  paint(localArrangementStore: LocalArrangementStore) {
    localArrangementStore.childsWithMetadata.forEach((element) => {
      this.canvasProxy.currentOffset.set(element.localCoord);
      element.shape.draw(this.canvasProxy);
    });
  }
}

class LocalArrangementStore {
  childsWithMetadata: ChildElement[] = [];

  getClickedChild(localCoord: Coord): ChildElement | null {
    let foundChild = this.childsWithMetadata.find((child) => {
      let offset = localCoord.minusCopy(child.localCoord);

      child.shape.isMineCoord(offset);
    });
    return foundChild ?? null;
  }

  addChild(childWithMetadata: ChildElement) {
    let insertionIndex = this.childsWithMetadata.findIndex((value) => {
      value.zCoord - childWithMetadata.zCoord;
    });
    this.childsWithMetadata.splice(insertionIndex ?? 0, 0, childWithMetadata);
  }

  sortChildrenByZ() {
    this.childsWithMetadata.sort((child1, child2) => {
      return child1.zCoord - child2.zCoord;
    });
  }
}

function CanvasEditor() {
  //   let myPoint = new Coord({x: 3});
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (canvasRef.current) {
      console.log(`in current `, canvasRef.current);
      const child = new Rectangle({ height: 100, width: 1000 });
      const localArrangementStore = new LocalArrangementStore();
      localArrangementStore.addChild(
        new ChildElement({
          shape: child,
          localCoord: new Coord({ x: 100, y: 10 }),
        })
      );
      var ctx = canvasRef.current.getContext("2d")!;

      const arrangementPainter = new ArrangementPainter(new CanvasProxy(ctx));
      arrangementPainter.paint(localArrangementStore);
      canvasRef.current.addEventListener(
        "click",
        (ev) => {
          let clientX = ev.clientX;
          let clientY = ev.clientY;
          console.log(new Coord({ x: clientX, y: clientY }));
        },
        false
      );
    }
  }, []);
  return (
    <>
      <div>
        <p>Hallo Guten Tag</p>
        <canvas ref={canvasRef}></canvas>
      </div>
    </>
  );
}

export default CanvasEditor;
