import { Subject } from "rxjs";

export class EditorDelegate {
    currentContent: string | null = null;
    onNewEditorInput: (newInput: string) => void = (newInput: string) => {
        this.currentContent = newInput;
    };
    inputObservable = new Subject<string>();

    updateEditorWithContents(newContents: string) {
        this.currentContent = newContents;
        this.inputObservable.next(newContents);
    }

    createProps() {
        return {
            inputObservable: this.inputObservable,
            onInputUpdated: this.onNewEditorInput
        };
    }
}
