import { EditorWrapper } from "renderer/components/Editor";
import { Observable } from "rxjs";
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import { SchemasSettings, setDiagnosticsOptions } from 'monaco-yaml';
import { Uri } from "monaco-editor/esm/vs/editor/editor.api";
import { EditorDelegate } from "./views/EditorDelegate";
import { ProjectWindow, ProjectWindowId } from "./domain";
import { ProjectWindowManager } from "./StructureNode";
import { ClickHandler, EditorHolder, OpenedFile } from "./views/ModelEditor";
import { modelUri, setupTemplate, setupYamlLanguageServer } from "simconfig/simconfig_yaml";



export class SimulatorEditor implements ProjectWindow, EditorHolder {

    readonly title: string = "Simulation Configuration";
    static id: ProjectWindowId = "sim-config";
    isOpened: boolean = false;
    readonly editorDelegate = new EditorDelegate("yaml", "Simulation Configuration");
    id = SimulatorEditor.id
    readonly editorKey = 'editor';

    constructor() {
    }

    collectFile(): OpenedFile {
        return {
          contents : this.editorDelegate.currentContent,
          filePath : this.editorDelegate.openedFilePath
        }
      }

    getEditorCurrentInput$() {
        return this.editorDelegate.getEditorCurrentInput$();
    }

    updateEditorWithContents(newContents: string, filePath : string) {
        this.editorDelegate.updateEditorWithContentsFromFile(newContents, filePath);
    }

    collectSimulationConfig() {
        return null;
    }

    createReactComponent = (onSizeChangeObservable: Observable<number[]>, visible: boolean) => <EditorWrapper
        editorCreator={(htmlElement : HTMLElement) => {
            // The uri is used for the schema file match.

            const value = setupTemplate;

            setupYamlLanguageServer();

            let newEditor = monaco.editor.create(htmlElement, {
                automaticLayout: true,
                model: monaco.editor.createModel(value, 'yaml', modelUri)
            });
            this.editorDelegate.onNewEditorInput(setupTemplate)

            return newEditor;
        }}
        editorId={this.title}
        key={this.title}
        {...this.editorDelegate.createProps()} />;
}
