import { EditorWrapper } from "renderer/components/Editor";
import { isOcDotRegistered, registerOcDot } from "renderer/ocdot/OcDotMonarch";
import { Observable } from "rxjs";
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import { EditorDelegate } from "./EditorDelegate";
import { ProjectWindow } from "./domain";


export class ModelEditor implements ProjectWindow {

    readonly title: string = "model";
    isOpened: boolean = false;
    editorDelegate = new EditorDelegate();

    updateEditorWithContents(newContents: string) {
        this.editorDelegate.updateEditorWithContents(newContents);
    }

    collectOcDot() {
        return this.editorDelegate.currentContent;
    }

    createReactComponent = (onSizeChangeObservable: Observable<number>, htmlElement: HTMLElement) => <EditorWrapper
        editorCreator={() => {
            if (!isOcDotRegistered()) {
                monaco.editor.defineTheme("ocDotTheme", {
                    base: "vs",
                    inherit: true,
                    rules: [
                        { token: "mult", foreground: "2563eb", fontStyle: "italic bold" },
                    ],
                    colors: {},
                });

                registerOcDot();
            }

            let newEditor = monaco.editor.create(htmlElement, {
                value: ['digraph {\n\ta -> b\n}'].join('\n'),
                language: 'ocdot',
                automaticLayout: true,
                theme: 'ocDotTheme',
                fontLigatures: true,
                autoIndent: 'full',
            });

            return newEditor;
        }}
        editorId={this.title}
        key={this.title}

        {...this.editorDelegate.createProps()} />;
}
