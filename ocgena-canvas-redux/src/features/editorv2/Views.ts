import Konva from "konva"
import _ from "lodash"
import RBush, { BBox } from "rbush"
import { Rect } from "./SpaceModel"
import { nlog } from "./EditorV2"
import { prettyPrintJson } from "pretty-print-json"

class ViewRBush extends RBush<View> {
    toBBox(view: View): BBox {
        return view.boundBox
    }

    compareMinX(a: View, b: View): number {
        return a.boundBox.minX - b.boundBox.minX
    }

    compareMinY(a: View, b: View): number {
        return a.boundBox.minY - b.boundBox.minY
    }

    minX(a: View) {
        return a.boundBox.minX
    }

    minY(a: View) {
        return a.boundBox.minY
    }
}

export type RBBox = {
    readonly minX: number
    readonly minY: number
    readonly maxX: number
    readonly maxY: number
    readonly id: string
}

function dlogbush(debugElementId: string, tree: RBush<unknown>) {
    document.getElementById(debugElementId)!.innerHTML = prettyPrintJson.toHtml(
        tree.toJSON(),
        {
            indent: 3,
            trailingCommas: false,
            quoteKeys: false,
            lineNumbers: false,
        },
    )
}

class ViewPositionIndex {
    tree = new RBush()
    idToReference: Map<String, RBBox> = new Map()
    idToView: Map<String, View> = new Map()

    debugTag: string
    constructor(debugTag: string) {
        this.debugTag = debugTag
    }

    addElements(views: View[]) {
        for (const view of views) {
            this.idToView.set(view.id, view)
            this.idToReference.set(view.id, view.boundBox)
        }
        this.tree.load(views.map(el => el.boundBox))
        dlogbush(this.debugTag, this.tree)
    }

    addElement(view: View) {
        this.idToView.set(view.id, view)
        this.idToReference.set(view.id, view.boundBox)
        this.tree.insert(view.boundBox)
        nlog(["debug"], "rbush after in", this.tree.all())
        nlog(
            ["debug"],
            "rbush collision ",
            this.tree.collides({ minX: 20, maxX: 21, minY: 3, maxY: 5 }),
        )
        dlogbush(this.debugTag, this.tree)
    }

    removeElement(view: ViewId | View) {
        const item = this.idToView.get(getViewId(view))
        if (!!item) {
            const reference = this.idToReference.get(getViewId(view))
            if (!!reference) {
                this.idToReference.delete(getViewId(view))
                this.tree.remove(reference)
            }
        }
        dlogbush(this.debugTag, this.tree)
    }

    removeElements(views: (ViewId | View)[]) {
        for (const view of views) {
            this.removeElement(view)
        }
    }

    updateElement(view: View) {
        const prevVersion = this.idToReference.get(view.id)

        if (!!prevVersion) {
            this.tree.remove(prevVersion)
        }

        const newVersion = view.boundBox
        this.idToReference.set(view.id, newVersion)
        this.idToView.set(view.id, view)
        this.tree.insert(newVersion)
        dlogbush(this.debugTag, this.tree)
    }

    updateElements(views: View[]) {
        for (const view of views) {
            this.updateElement(view)
        }
    }

    searchIntersecting(left: number, top: number, right: number, bottom: number): View[] {
        const boxes = this.tree.search({
            minY: top,
            maxY: bottom,
            minX: left,
            maxX: right,
        }) as (BBox & { id: string })[]
        return boxes.map(el => this.idToView.get(el.id)).filter(el => !!el)
    }
}

type KonvaChild = Konva.Group | Konva.Shape

class AttachmentDelegate {
    nodeGetter: () => KonvaChild | null
    childrenGetter?: () => View[]

    constructor(nodeGetter: () => KonvaChild | null, childGetter?: () => View[]) {
        this.nodeGetter = nodeGetter
        this.childrenGetter = childGetter
    }
    layer: Konva.Layer | null = null
    node: KonvaChild | null = null

    isAttached() {
        return !!this.layer
    }

    attach(layer: Konva.Layer) {
        this.detach()
        this.node = this.nodeGetter()
        this.layer = layer
        if (!!this.node) {
            layer.add(this.node)
        }
        if (!!this.childrenGetter) {
            const children = this.childrenGetter()
            this.attachChildren(children)
        }
    }

    detach() {
        if (this.isAttached()) {
            this.node?.remove()
            if (!!this.childrenGetter) {
                this.detachChildren(this.childrenGetter())
            }
        }
        this.node = null
        this.layer = null
    }

    addChild(view: View) {
        this.attachChildren([view])
    }

    removeChild(view: View) {
        this.detachChildren([view])
    }

    attachChildren(views: View[]) {
        if (!!this.layer) {
            for (const view of views) {
                view.attach(this.layer)
            }
        }
    }

    detachChildren(children: View[]) {
        for (const view of children) {
            if (view.isAttached()) {
                view.detach()
            }
        }
    }
}

class UpdatesDelegate {
    parentGetter: () => ViewParent | null
    view: View

    constructor(view: View, parentGetter: () => ViewParent | null) {
        this.view = view
        this.parentGetter = parentGetter
    }

    notifyParentOfChange() {
        const parent = this.parentGetter()
        if (!!parent) {
            parent.childIndex.updateElement(this.view)
        }
    }
}

interface ViewParent {
    childIndex: ViewPositionIndex
    children: View[]
    addChild(child: View): void
    removeChild(child: ViewId | View): void
    searchIntersecting(point: Rect): View[]
}

export interface View {
    id: ViewId
    x: number
    y: number
    boundBox: RBBox
    parent: ViewParent | null

    containsXY: (x: number, y: number) => boolean
    attach(layer: Konva.Layer): void
    isAttached(): boolean
    draw(): void
    detach(): void
}

type ViewId = string

interface ViewGroup extends View {
    children: View[]

    addChild(child: View): void
    removeChild(child: ViewId | View): void
}

export class RectangleView implements View {
    private _width: number
    private _height: number
    x = 0
    y = 0
    id: string
    node: Konva.Group | null = null
    textNode: Konva.Text | null = null
    attachedLayerDelegate = new AttachmentDelegate(() => this.getOrCreateNode())
    parent = null

    boundBox: RBBox = {
        minX: 0,
        maxX: 0,
        minY: 0,
        maxY: 0,
        id: "",
    }

    private recalcBounds() {
        const newBBox: BBox & { id: string } = {
            minX: this.x,
            maxX: this.x + this._width,
            minY: this.y,
            maxY: this.y + this._height,
            id: this.id,
        }

        if (!_.isEqual(this.boundBox, newBBox)) {
            this.boundBox = newBBox
        }
    }

    constructor(id: string, width: number, height: number) {
        this._width = width
        this._height = height
        this.id = id
        this.recalcBounds()
    }
    private getOrCreateNode() {
        if (!!this.node) return this.node

        const nodeGroup = new Konva.Group({
            id: this.id,
            x: this.x,
            y: this.y,
        })

        const rect = new Konva.Rect({
            x: 0,
            y: 0,
            width: this._width,
            height: this._height,
            strokeEnabled: true,
            stroke: "black",
            fillEnabled: false,
        })
        const text = new Konva.Text({
            text: this.id,
            fontSize: 24,
            ellipsis: true,
            align: "center",
            verticalAlign: "middle",
            wrap: "word",
        })

        nodeGroup.add(rect)
        nodeGroup.add(text)
        this.node = nodeGroup
        return nodeGroup
    }
    attach(layer: Konva.Layer): void {
        this.attachedLayerDelegate.attach(layer)
    }
    isAttached() {
        return this.attachedLayerDelegate.isAttached()
    }
    draw(): void {
        this.node?.draw()
    }
    detach(): void {
        this.attachedLayerDelegate.detach()
    }
    containsXY(x: number, y: number) {
        return this.x <= x && x <= this.x + this._width && this.y <= y && y <= this.y + this._height
    }
}

export class SelectionView implements View {
    private _width: number
    private _height: number
    x = 0
    y = 0
    id: string = "selection-area"
    node: Konva.Rect | null = null
    attachedLayerDelegate = new AttachmentDelegate(() => this.getOrCreateNode())
    updateDelegate = new UpdatesDelegate(this, () => this.parent)
    parent = null
    boundBox: RBBox = {
        minX: 0,
        maxX: 0,
        minY: 0,
        maxY: 0,
        id: "",
    }

    private recalcBounds() {
        const newBBox: RBBox = {
            minX: this.x,
            maxX: this.x + this._width,
            minY: this.y,
            maxY: this.y + this._height,
            id: this.id,
        }

        if (!_.isEqual(this.boundBox, newBBox)) {
            this.boundBox = newBBox
        }
    }

    constructor(width: number, height: number) {
        this._width = width
        this._height = height
        this.recalcBounds()
    }

    update(updates: { x?: number; y?: number; height?: number; width?: number }) {
        if (!!updates.x) {
            this.x = updates.x
        }
        if (!!updates.y) {
            this.y = updates.y
        }
        if (!!updates.height) {
            this._height = updates.height
        }
        if (!!updates.width) {
            this._width = updates.width
        }
        // todo need to notify parent here
        this.recalcBounds()
        this.updateDelegate.notifyParentOfChange()
        if (!!this.node) {
            this.node.setAttrs({
                x: this.x,
                y: this.y,
                height: this._height,
                width: this._width,
            })
            this.draw()
        }
    }

    private getOrCreateNode() {
        if (!!this.node) return this.node
        const rect = new Konva.Rect({
            x: 0,
            y: 0,
            height: this._height,
            width: this._width,
            strokeEnabled: true,
            stroke: "blue",
            dashEnabled: true,
            fillEnabled: false,
        })
        this.node = rect
        return rect
    }
    attach(layer: Konva.Layer): void {
        this.attachedLayerDelegate.attach(layer)
    }
    isAttached() {
        return this.attachedLayerDelegate.isAttached()
    }
    draw(): void {
        this.node?.draw()
    }
    detach(): void {
        this.attachedLayerDelegate.detach()
    }
    containsXY(x: number, y: number) {
        return this.x <= x && x <= this.x + this._width && this.y <= y && y <= this.y + this._height
    }
}

export class CircleView implements View {
    private _radius: number
    x = 0
    y = 0
    id: string
    boundBox: RBBox = {
        minX: 0,
        minY: 0,
        maxX: 0,
        maxY: 0,
        id: "",
    }
    layerDelegate = new AttachmentDelegate(() => this.getOrCreateNode())
    private node: KonvaChild | null = null

    constructor(id: string, radius: number) {
        this._radius = radius
        this.id = id
        this.recalcBoundBox()
    }
    parent: ViewParent | null = null

    isAttached(): boolean {
        return this.layerDelegate.isAttached()
    }

    private recalcBoundBox() {
        const newBBox: BBox & { id: string } = {
            minX: this.x - this._radius,
            maxX: this.x + this._radius,
            minY: this.y - this._radius,
            maxY: this.y + this._radius,
            id: this.id,
        }

        if (!_.isEqual(this.boundBox, newBBox)) {
            this.boundBox = newBBox
        }
    }

    attach(layer: Konva.Layer): void {
        this.layerDelegate.attach(layer)
    }
    draw(): void {
        this.node?.draw()
    }
    detach(): void {
        this.layerDelegate.detach()
    }

    containsXY(x: number, y: number) {
        const fastCheck =
            this.x - this._radius <= x &&
            x <= this.x + this._radius &&
            this.y - this._radius <= y &&
            y <= this.y + this._radius

        if (!fastCheck) return false

        return Math.sqrt((this.x - x) * (this.x - x) + (this.y - y) * (this.y - y)) <= this._radius
    }

    private getOrCreateNode() {
        if (!!this.node) return this.node

        const nodeGroup = new Konva.Group({
            id: this.id,
            x: this.x,
            y: this.y,
        })

        const circle = new Konva.Circle({
            x: 0,
            y: 0,
            radius: this._radius,
            strokeEnabled: true,
            stroke: "black",
            fillEnabled: false,
        })
        const text = new Konva.Text({
            text: this.id,
            fontSize: 24,
            ellipsis: true,
            align: "center",
            verticalAlign: "middle",
            wrap: "word",
        })

        nodeGroup.add(circle)
        nodeGroup.add(text)
        this.node = nodeGroup
        return nodeGroup
    }
}

function isViewId(view: ViewId | View): view is ViewId {
    return typeof view === "string"
}

function getViewId(view: ViewId | View): ViewId {
    if (isViewId(view)) {
        return view
    } else {
        return view.id
    }
}

export interface LayerViewCollection extends ViewParent {
    x: number
    y: number
    childIndex: ViewPositionIndex
    children: View[]
    attachmentDelegate: AttachmentDelegate

    addChild(child: View): void
    addChildren(children: View[]): void
    removeChild(child: ViewId | View): View | null
    removeChildren(children: (ViewId | View)[]): View[]
    attach(layer: Konva.Layer): void
    draw(): void
    detach(): void
}

class BaseLayerViewCollection implements LayerViewCollection {
    x: number = 0
    y: number = 0
    childIndex: ViewPositionIndex
    children: View[] = []
    attachmentDelegate = new AttachmentDelegate(
        () => null,
        () => this.children,
    )
    debugTag: string
    constructor(debugTag: string) {
        this.debugTag = debugTag
        this.childIndex = new ViewPositionIndex(debugTag)
    }

    addChild(child: View): void {
        this.children.push(child)
        this.attachmentDelegate.attachChildren([child])
        this.childIndex.addElement(child)

        child.parent = this
    }
    addChildren(children: View[]): void {
        for (const child of children) {
            this.addChild(child)
        }
    }
    removeChild(child: ViewId | View): View | null {
        const removed = _.remove(this.children, el => el.id === getViewId(child))
        this.attachmentDelegate.detachChildren(removed)
        this.childIndex.removeElement(child)
        for (const removedChild of removed) {
            removedChild.parent = null
        }
        if (removed.length > 0) {
            return removed[0]
        }
        return null
    }
    removeChildren(children: (ViewId | View)[]): View[] {
        return children.map(el => this.removeChild(getViewId(el))).filter(el => !!el)
    }
    attach(layer: Konva.Layer): void {
        this.attachmentDelegate.attach(layer)
    }
    draw(): void {
        this.attachmentDelegate.layer?.batchDraw()
    }
    detach(): void {
        this.attachmentDelegate.detach()
    }
    searchIntersecting(point: Rect): View[] {
        return this.childIndex.searchIntersecting(point.left, point.top, point.right, point.bottom)
    }
}

export class LayerViewCollectionDelegate extends BaseLayerViewCollection {
    constructor(debugTag: string) {
        super((debugTag = debugTag))
    }
}

export class SelectionLayerViewCollection extends BaseLayerViewCollection {
    currentSelection: Rect | null = null
    selectionRect: SelectionView = new SelectionView(0, 0)

    updateSelection(selectionRect: Rect | null) {
        if (!_.isEqual(selectionRect, this.currentSelection)) {
            if (!selectionRect) {
                this.selectionRect.detach()
            } else {
                if (this.selectionRect.isAttached() && this.attachmentDelegate.layer) {
                    this.selectionRect.attach(this.attachmentDelegate.layer)
                    this.selectionRect.update({
                        x: selectionRect.left,
                        y: selectionRect.top,
                        width: selectionRect.right - selectionRect.left,
                        height: selectionRect.bottom - selectionRect.top,
                    })
                }
            }
        }
    }
}
