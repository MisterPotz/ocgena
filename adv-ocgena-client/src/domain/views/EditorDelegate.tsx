import { Subject } from "rxjs";

export class EditorDelegate {
    currentContent: string | undefined;
    openedFilePath : string | undefined;
    extension : string
    fileType: string

    constructor(
        extension: string,
        fileType: string,
    ) {
        this.extension = extension;
        this.fileType = fileType
    }

    onNewEditorInput: (newInput: string) => void = (newInput: string) => {
        this.currentContent = newInput;
        console.log('received editor input for %s', this.extension)
        this.editorCurrentInput$.next(newInput);
    };
    
    readonly editorInputRequest$ = new Subject<string>();
    private editorCurrentInput$ = new Subject<string>();

    updateEditorWithContentsFromFile(newContents: string, filePath : string) {
        this.currentContent = newContents;
        this.openedFilePath = filePath;
        this.editorInputRequest$.next(newContents);
    }

    getEditorCurrentInput$() {
        return this.editorCurrentInput$
    }

    createProps() {
        return {
            editorInputRequest$: this.editorInputRequest$,
            onInputUpdated: this.onNewEditorInput
        };
    }
}
