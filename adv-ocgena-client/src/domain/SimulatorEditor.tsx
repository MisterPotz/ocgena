import { EditorWrapper } from "renderer/components/Editor";
import { Observable } from "rxjs";
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import { SchemasSettings, setDiagnosticsOptions } from 'monaco-yaml';
import { Uri } from "monaco-editor/esm/vs/editor/editor.api";
import { EditorDelegate } from "./EditorDelegate";
import { ProjectWindow } from "./domain";


export class SimulatorEditor implements ProjectWindow {

    readonly title: string = "config";
    isOpened: boolean = false;
    editorDelegate = new EditorDelegate();

    updateEditorWithContents(newContents: string) {
        this.editorDelegate.updateEditorWithContents(newContents);
    }

    collectSimulationConfig() {

        return null;
    }

    createReactComponent = (onSizeChangeObservable: Observable<number[]>) => <EditorWrapper
        editorCreator={(htmlElement : HTMLElement) => {
            // The uri is used for the schema file match.
            const modelUri = Uri.parse('a://b/foo.yaml');

            setDiagnosticsOptions({
                enableSchemaRequest: true,
                hover: true,
                completion: true,
                validate: true,
                format: true,
                schemas: [
                    {
                        // Id of the first schema
                        uri: 'http://myserver/foo-schema.json',
                        // Associate with our model
                        fileMatch: [String(modelUri)],
                        schema: {
                            type: 'object',
                            properties: {
                                p1: {
                                    enum: ['v1', 'v2']
                                },
                                p2: {
                                    // Reference the second schema
                                    $ref: 'http://myserver/bar-schema.json'
                                }
                            }
                        }
                    },
                    {
                        // Id of the first schema
                        uri: 'http://myserver/bar-schema.json',
                        fileMatch: [],
                        schema: {
                            type: 'object',
                            properties: {
                                q1: {
                                    enum: ['x1', 'x2']
                                }
                            }
                        }
                    } as SchemasSettings
                ]
            });

            const value = 'p1: \np2: \n';

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
