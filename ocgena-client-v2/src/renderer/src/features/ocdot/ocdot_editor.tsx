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
