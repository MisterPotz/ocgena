import { FC, useRef, useState, useEffect } from 'react';
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import React from 'react';
import { isOcDotRegistered, registerOcDot } from 'renderer/ocdot/OcDotMonarch';

export const Editor: FC = () => {
	const [editor, setEditor] = useState<monaco.editor.IStandaloneCodeEditor | null>(null);
	const monacoEl = useRef(null);

	useEffect(() => {
		if (monacoEl) {
			setEditor((editor) => {
				if (editor) return editor;

				if (!isOcDotRegistered()) { 
					registerOcDot();
				}

				return monaco.editor.create(monacoEl.current!, {
					value: ['digraph {\n\ta -> b\n}'].join('\n'),
					language: 'ocdot',
					automaticLayout: true,
				});
			});
		}

		return () => editor?.dispose();
	}, [monacoEl.current]);

	return <div className='container h-full w-full' ref={monacoEl}></div>;
};

export const EditorWrapper = () => {
	return (<React.StrictMode>
		<Editor />
	</React.StrictMode>);
}