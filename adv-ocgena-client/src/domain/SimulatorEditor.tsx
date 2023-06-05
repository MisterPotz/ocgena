import { EditorWrapper } from "renderer/components/Editor";
import { Observable } from "rxjs";
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import { SchemasSettings, setDiagnosticsOptions } from 'monaco-yaml';
import { Uri } from "monaco-editor/esm/vs/editor/editor.api";
import { EditorDelegate } from "./EditorDelegate";
import { ProjectWindow, ProjectWindowId } from "./domain";
import { ProjectWindowManager } from "./StructureNode";
import { ClickHandler } from "./ModelEditor";


export class SimulatorEditor implements ProjectWindow {

    readonly title: string = "config of my model";
    static id: ProjectWindowId = "sim-config";
    isOpened: boolean = false;
    editorDelegate = new EditorDelegate();
    id = SimulatorEditor.id
    
    constructor() {
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
            const modelUri = Uri.parse('a://sim-config.yaml');

            setDiagnosticsOptions({
                enableSchemaRequest: true,
                hover: true,
                completion: true,
                validate: true,
                format: true,
                schemas: [
                    {
                        // Id of the first schema
                        uri: 'http://myserver/sim-config.json',
                        // Associate with our model
                        fileMatch: [String(modelUri)],
                        schema: {
                            type: 'object',
                            properties: {
                                inputPlaces: {
                                    type: "string",
                                    title: "input places",
                                    description: "List all input places separated by space"
                                },
                                outputPlaces: {
                                    type: "string",
                                    title: "output places",
                                    description: "List all output places separated by space"
                                },
                                ocNetDefinition: {
                                    enum: ["aalst", "lomazova"],
                                    title: "Definition of OC-net",
                                    description: "Chosen definition of OC-net may affect calculations and consistency checks"
                                },
                                placeTyping: {
                                    type: "object",
                                    description: "Mapping from object type to places, list all places with spaces",
                                    additionalProperties: {
                                        type: "string"
                                    }
                                },
                                labelMapping: {
                                    type: "object",
                                    description: "Mapping from place id to activity label",
                                    additionalProperties: {
                                        type: "string"
                                    }
                                },
                                initialMarking: {
                                    type: "object",
                                    description: "Places to their respective initial marking",
                                    additionalProperties: {
                                        type: "integer"
                                    }
                                },
                                transitionInterval: {
                                    type: "object",
                                    properties: {
                                        defaultTransitionInterval: {
                                            $ref: 'http://myserver/transition-interval.json'
                                        },
                                        transitionsToIntervals: {
                                            type: "object",
                                            additionalProperties: {
                                                $ref: "http://myserver/transition-interval.json"
                                            }
                                        }
                                    }
                                }
                                // p1: {
                                //     enum: ['v1', 'v2']
                                // },
                                // p2: {
                                //     // Reference the second schema
                                //     $ref: 'http://myserver/bar-schema.json'
                                // }
                            }
                        }
                    },
                    {
                        // Id of the first schema
                        uri: 'http://myserver/time-range.json',
                        fileMatch: [],
                        schema: {
                            type: 'array',
                            items: {
                              type: "integer"
                            },
                            minItems: 2,
                            maxItems: 2,
                            description: "Start and end value of the possible range"
                        }
                    } as SchemasSettings,
                    {
                        // Id of the first schema
                        uri: 'http://myserver/transition-interval.json',
                        fileMatch: [],
                        schema: {
                            type: "object",
                            properties: {
                                duration: {
                                    $ref: "http://myserver/time-range.json"
                                },
                                minOccurrenceInterval: {
                                    $ref: "http://myserver/time-range.json"
                                }
                            },
                            description: "Define possible ranges for duration and minOccurrenceInterval"
                        }
                    } as SchemasSettings,
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
