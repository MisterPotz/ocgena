import { FC, useRef, useState, useEffect } from 'react';
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import React from 'react';
import { isOcDotRegistered, registerOcDot } from 'renderer/ocdot/OcDotMonarch';
import { appService } from 'renderer/AppService';
import { Observable } from 'rxjs';
import { useObservableState } from 'observable-hooks';

export type EditorProps = {
	readonly editorId : string,
	readonly onInputUpdated: (newInput: string) => void, 
	readonly editorInputRequest$ : Observable<string>,
	readonly editorCreator: (htmlElement : HTMLElement) => monaco.editor.IStandaloneCodeEditor
}

export const Editor = (
	{
		editorId,
		onInputUpdated,
		editorInputRequest$: inputObservable,
		editorCreator
	} : EditorProps
) => {
	const [editor, setEditor] = useState<monaco.editor.IStandaloneCodeEditor | null>(null);
	const monacoEl = useRef(null);

	const input = useObservableState(inputObservable)

	console.log("receiving at editor this input: " + input);

	useEffect(() => {
		if (monacoEl.current) {
			setEditor((editor) => {
				if (editor) return editor;
				
				console.log("creating new editor")
				let newEditor = editorCreator(monacoEl.current!)
				newEditor.onDidChangeModelContent(function (event) {
					let newContent = newEditor.getValue();
					console.log("new content of editor  " + editorId + " is :" + newContent);
					onInputUpdated(newContent)
				});
				return newEditor;
			});
		}

		return () => editor?.dispose();
	}, [monacoEl.current]);

	useEffect(() => {
		if (editor) {
			console.log("setting ocdot to editor from file")
			editor.setValue(input ? input : "");
		}
	}, [input])

	return <div className='h-full w-full' ref={monacoEl}></div>;
};

export const EditorWrapper = (editorProps : EditorProps) => {
	return (<React.StrictMode>
		<Editor {...editorProps}/>
	</React.StrictMode>);
}