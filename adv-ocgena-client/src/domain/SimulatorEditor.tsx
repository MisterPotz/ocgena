import { EditorWrapper } from "renderer/components/Editor";
import { Observable } from "rxjs";
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import { SchemasSettings, setDiagnosticsOptions } from 'monaco-yaml';
import { Uri } from "monaco-editor/esm/vs/editor/editor.api";
import { EditorDelegate } from "./EditorDelegate";
import { ProjectWindow, ProjectWindowId } from "./domain";
import { ProjectWindowManager } from "./StructureNode";
import { ClickHandler } from "./ModelEditor";
import { modelUri, setupTemplate, setupYamlLanguageServer } from "simconfig/simconfig_yaml";


export class SimulatorEditor implements ProjectWindow {

    readonly title: string = "config of my model";
    static id: ProjectWindowId = "sim-config";
    isOpened: boolean = false;
    private editorDelegate = new EditorDelegate();
    id = SimulatorEditor.id
    
    constructor() {
    }

    getEditorCurrentInput$() {
        return this.editorDelegate.getEditorCurrentInput$();
    }

    updateEditorWithContents(newContents: string) {
        this.editorDelegate.updateEditorWithContents(newContents);
    }

    collectSimulationConfig() {
        return null;
    }

    createReactComponent = (onSizeChangeObservable: Observable<number[]>) => <EditorWrapper
        editorCreator={(htmlElement : HTMLElement) => {
            // The uri is used for the schema file match.

            const value = setupTemplate;

            setupYamlLanguageServer();

            let newEditor = monaco.editor.create(htmlElement, {
                automaticLayout: true,
                model: monaco.editor.createModel(value, 'yaml', modelUri)
            });

            return newEditor;
        }}
        editorId={this.title}
        key={this.title}
        {...this.editorDelegate.createProps()} />;
}
