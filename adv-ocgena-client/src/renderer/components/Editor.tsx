import { FC, useRef, useState, useEffect } from 'react';
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import React from 'react';
import { isOcDotRegistered, registerOcDot } from 'renderer/ocdot/OcDotMonarch';
import { appService } from 'renderer/AppService';

export type EditorProps = {
	editorId : string,
	onNewInput: (newInput: string) => void, 
	ocDot?: string | null,
}

export const Editor = (
	{
		editorId,
		onNewInput,
		ocDot
	} : EditorProps
) => {
	const [editor, setEditor] = useState<monaco.editor.IStandaloneCodeEditor | null>(null);
	const monacoEl = useRef(null);

	console.log("receiving at editor this input: " + ocDot);
	useEffect(() => {
		if (monacoEl) {
			setEditor((editor) => {
				if (editor) return editor;

				if (!isOcDotRegistered()) { 
					monaco.editor.defineTheme("ocDotTheme", {
						base: "vs", // can also be vs-dark or hc-black
						inherit: true, // can also be false to completely replace the builtin rules
						rules: [
							{ token: "mult", foreground: "2563eb", fontStyle: "italic bold" },
						],
						colors: {
						},

					});

					registerOcDot();
				}


				let newEditor = monaco.editor.create(monacoEl.current!, {
					value: ['digraph {\n\ta -> b\n}'].join('\n'),
					language: 'ocdot',
					automaticLayout: true,
					theme: 'ocDotTheme',
					fontLigatures: true,
					autoIndent: 'full',
				
				});
				newEditor.onDidChangeModelContent(function (event) {
					
					let newContent = newEditor.getValue();
					console.log("new content of editor " + newContent);
					appService.onNewOcDotEditorValue(newContent)
				});
				return newEditor;
			});
		}

		return () => editor?.dispose();
	}, [monacoEl.current]);

	useEffect(() => {
		if (editor) {
			console.log("setting ocdot to editor from file")
			editor.setValue(ocDot ? ocDot : "");
		}
	}, [ocDot])

	return <div className='container h-full w-full' ref={monacoEl}></div>;
};

export const EditorWrapper = (editorProps : EditorProps) => {
	console.log("at wrapper have " + JSON.stringify(editorProps))
	return (<React.StrictMode>
		<Editor {...editorProps}/>
	</React.StrictMode>);
}