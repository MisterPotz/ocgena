import { Subject } from "rxjs";

export class EditorDelegate {
    currentContent: string | null = null;
    onNewEditorInput: (newInput: string) => void = (newInput: string) => {
        this.currentContent = newInput;
        this.editorCurrentInput$.next(newInput);
    };
    
    editorInputRequest$ = new Subject<string>();
    editorCurrentInput$ = new Subject<string>();

    updateEditorWithContents(newContents: string) {
        this.currentContent = newContents;
        this.editorInputRequest$.next(newContents);
    }

    createProps() {
        return {
            editorInputRequest$: this.editorInputRequest$,
            onInputUpdated: this.onNewEditorInput
        };
    }
}
