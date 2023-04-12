import * as monaco from 'monaco-editor';
import { select as d3_select } from 'd3-selection';
import 'd3-graphviz'


self.MonacoEnvironment = {

	getWorkerUrl: function (moduleId, label) {
		if (label === 'json') {
			return './json.worker.bundle.js';
		}
		if (label === 'css' || label === 'scss' || label === 'less') {
			return './css.worker.bundle.js';
		}
		if (label === 'html' || label === 'handlebars' || label === 'razor') {
			return './html.worker.bundle.js';
		}
		if (label === 'typescript' || label === 'javascript') {
			return './ts.worker.bundle.js';
		}
		return './editor.worker.bundle.js';
	}
};

monaco.editor.create(document.getElementById('container'), {
	value: ['function x() {', '\tconsole.log("Hello world!");', '}'].join('\n'),
	language: 'javascript',
	automaticLayout: true,
});

const graphvizView = document.querySelector('#rendered-graphviz');
const monacoOCDotEditor = document.querySelector('#container');

const parseButton = document.querySelector('#test')

const updateGraphviz = () => {

	let width = graphvizView.clientWidth;
	let height = graphvizView.clientHeight;

	d3_select('#rendered-graphviz')
		.graphviz({
			height: height,
			width: width
		})
		.renderDot('digraph {a -> b}');
}

parseButton.addEventListener('click', (event) => {
	updateGraphviz();
})
