import { useEffect, useRef, useState } from "react";
import "./CanvasEditor.css";

interface ICoord {
  x: number;
  y: number;
}

interface IArea {
  topLeft : ICoord,
  bottomRight : ICoord
}

class Coord implements ICoord {
  x: number = 0;
  y: number = 0;

  constructor(df?: { x?: number; y?: number }) {
    this.x = df?.x ?? 0;
    this.y = df?.y ?? 0;
  }

  set(offset: ICoord) {
    this.x = offset.x;
    this.y = offset.y;
  }

  plusCopy(offset: { x?: number; y?: number }) {
    return new Coord({
      x: this.x + (offset.x ?? 0),
      y: this.y + (offset.y ?? 0),
    });
  }

  minusCopy(coord: ICoord): Coord {
    return new Coord({ x: this.x - coord.x, y: this.y - coord.y });
  }
}

interface Rect {
  height: number;
  width: number;
}

class Element {
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
  currentOffset: Coord = new Coord();

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
  isMineCoord(localCoord: Coord): Boolean {
    return this.rect.height >= localCoord.y && this.rect.width >= localCoord.x
  }
}

class ArrangementPainter {
  canvasProxy: CanvasProxy;

  constructor(canvasProxy: CanvasProxy) {
    this.canvasProxy = canvasProxy;
  }

  paint(localArrangementStore: Arrangement) {
    localArrangementStore.childsWithMetadata.forEach((element) => {
      this.canvasProxy.currentOffset.set(element.localCoord);
      element.shape.draw(this.canvasProxy);
    });
  }
}

class CanvasWrapper {
  canvas: HTMLCanvasElement;

  constructor(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
  }

  resolveCanvasCoord(): Coord {
    return new Coord({
      x: this.canvas.offsetLeft + this.canvas.clientLeft,
      y: this.canvas.offsetTop + this.canvas.clientTop,
    });
  }
}

class Arrangement {
  childsWithMetadata: Element[] = [];

  getClickedChild(localCoord: Coord): Element | null {
    let foundChild = this.childsWithMetadata.reduceRight(
      (prevValue: Element | null, current: Element) => {
        if (prevValue) return prevValue;
        let offset = localCoord.minusCopy(current.localCoord);
        return current.shape.isMineCoord(offset) ? current : null;
      },
      null
    );
    return foundChild ?? null;
  }

  addChild(childWithMetadata: Element) {
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
  const eventListenerAdded = useRef(false);
  useEffect(() => {
    if (canvasRef.current) {
      console.log(`in current `, canvasRef.current);
      const canvasWrapper = new CanvasWrapper(canvasRef.current);
      const child = new Rectangle({ height: 100, width: 1000 });
      const localArrangementStore = new Arrangement();

      if (!eventListenerAdded.current) {
        canvasRef.current.addEventListener(
          "click",
          (ev: MouseEvent) => {
            let clientX = ev.clientX;
            let clientY = ev.clientY;
            let clientCoord = new Coord({ x: clientX, y: clientY });
            let htmlCoord = canvasWrapper.resolveCanvasCoord();

            console.log("html coord", htmlCoord);
            let localHtmlCoord = clientCoord.minusCopy(htmlCoord);
            console.log("local html coord", localHtmlCoord);
            let clickedChild =
              localArrangementStore.getClickedChild(localHtmlCoord);
            console.log("clicked child", clickedChild);
          },
          false
        );
        eventListenerAdded.current = true;
      }
      localArrangementStore.addChild(
        new Element({
          shape: child,
          localCoord: new Coord({ x: 100, y: 10 }),
        })
      );
      var ctx = canvasRef.current.getContext("2d")!;
      const arrangementPainter = new ArrangementPainter(new CanvasProxy(ctx));
      arrangementPainter.paint(localArrangementStore);
    }
  }, []);
  return (
    <>
      <div>
        <p>Hallo Guten Tag</p>
        <canvas style={{ margin: 100 }} ref={canvasRef}></canvas>
      </div>
    </>
  );
}

export default CanvasEditor;
