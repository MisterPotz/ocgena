import { trimIndent } from "ocdot-parser/lib/exts";

export const exampleModel = trimIndent(`
    ocnet {
      places { 
        p1 [color=green] p2 p3
      }
      transitions {
        t1 t2
      }
      
      p1 -> t1 -> p2 -> t2 -> p3
    }
  `);

export type OpenedFile = {
  contents?: string;
  filePath?: string;
};

import { useRef, useState, useEffect } from "react";
import * as monaco from "monaco-editor/esm/vs/editor/editor.api";
import { isOcDotRegistered, registerOcDot } from "./OcDotMonarch";
import {
  exampleConfiguration,
  modelUri,
  setupYamlLanguageServer,
} from "./simconfig_yaml";
import { dots } from "../editor/DotField";
import Graph from "./Graph";

export type EditorProps = {
  // readonly editorId : string,
  // readonly onInputUpdated: (newInput: string) => void,
  // readonly editorInputRequest$ : Observable<string>,
  // readonly editorCreator: (htmlElement : HTMLElement) => monaco.editor.IStandaloneCodeEditor
};

const createSimulatorEditor = (htmlElement: HTMLElement) => {
  const value = exampleConfiguration;

  
  let newEditor = monaco.editor.create(htmlElement, {
    automaticLayout: true,
    model: monaco.editor.createModel(value, "yaml", modelUri),
  });
  setupYamlLanguageServer();
  newEditor.setValue(value);
  return newEditor;
};

const createModelEditor = (htmlElement: HTMLElement) => {
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
    value: [exampleModel].join("\n"),
    language: "ocdot",
    automaticLayout: true,
    theme: "ocDotTheme",
    fontLigatures: true,
    autoIndent: "full",
  });
  newEditor.setValue(exampleModel);
  return newEditor;
};

export const OcDotEditor = (
  {
    // editorId,
    // onInputUpdated,
    // editorInputRequest$: inputObservable,
    // editorCreator
  }: EditorProps
) => {
  console.log("redrawing editor");
  const [editor, setEditor] =
    useState<monaco.editor.IStandaloneCodeEditor | null>(null);
  const monacoEl = useRef(null);

  // const input = useObservableState(inputObservable)

  // console.log("receiving at editor this input: " + input);

  useEffect(() => {
    if (monacoEl.current) {
      setEditor((editor) => {
        if (editor) return editor;

        console.log("creating new editor");
        let newEditor = createModelEditor(monacoEl.current!);
        newEditor.onDidChangeModelContent(function (event: any) {
          let newContent = newEditor.getValue();
          console.log("new content of editor +  is :" + newContent);
          // onInputUpdated(newContent)
        });
        newEditor.setValue(exampleModel);
        return newEditor;
      });
    }

    return () => editor?.dispose();
  }, [monacoEl.current]);

  // useEffect(() => {
  // 	if (editor) {
  // 		console.log("setting ocdot to editor from file")
  // 		editor.setValue(input ? input : "");
  // 	}
  // }, [input])

  return <div className="h-full w-full container" ref={monacoEl}></div>;
};

export const EditorWrapper = (editorProps: EditorProps) => {
  return <OcDotEditor {...editorProps} />;
};

export type GraphvizProps = {
  dotSrc: string,
  loading: boolean,
  registerParentSizeUpdate: (onParentSizeUpdate: () => void) => void;
}

export const GraphvizPane = (
  {
      registerParentSizeUpdate,
      dotSrc,
      loading = true
  }: GraphvizProps
) => {
  // const [graphvizs, setGraphviz] = useState<string>("digraph {a -> b}'")
  console.log("pane is invoked with callback " + registerParentSizeUpdate);

  // useEffect(() => {
  //     if (!graphvizEl.current) return;

  //     setGraphviz((graphvizs) => {
  //         if (graphvizs) {
  //             graphvizs.fit(true)
  //             .renderDot("digraph {a -> b}'")
  //             return graphvizs
  //         }

  //         const graph = graphviz(graphvizEl.current)
  //         graph.fit(true)
  //         .renderDot("digraph {a -> b}'")
  //         return graph
  //     })

  // }, [dotString])

  // const [dot, updateDot] = useObservableState(
  //   () => appService.getGraphvizObservable(),
  //   ""
  // );

  console.log("doing this dot: \n" + dotSrc);

  return (
    <div className="w-full h-full bg-white relative">
      {/* <div className={`${loading ? "visible" : "invisible"} animate-spin ease-linear rounded-full border-4 border-t-4 border-blue-600 border-t-blue-200 h-8 w-8 absolute right-2 top-2`}></div> */}
      <svg
        className={`${loading ? "visible" : "invisible"} animate-spin ease-linear h-5 w-5 mr-3 text-blue-500 absolute right-2 top-2`}
        viewBox="0 0 24 24"
      >
        <circle
          className="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="4"
          fill="none"
        ></circle>
        <path
          className="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.789 3 7.938l3-2.647z"
        ></path>
      </svg>

      <Graph
        dotSrc={dotSrc}
        fit
        transitionDuration={1}
        registerParentSizeUpdate={registerParentSizeUpdate}
      />
    </div>
  );
}

