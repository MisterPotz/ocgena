// const { marked } = require('marked');
// const { sanitize } = require('dompurify')
const { ipcRenderer, webContents, BrowserWindow } = require('electron')
// const path = require('path');
// const d3 = require('d3');

const ocgena = require('ocgena')
let filePath = null;
let originalContent = '';

const graphvizView = document.querySelector('#rendered-graphviz');
const monacoOCDotEditor = document.querySelector('#container');

// const newFileButton = document.querySelector('#new-file');
// const openFileButton = document.querySelector('#open-file');
// const saveMarkdownButton = document.querySelector('#save-markdown');
// const revertButton = document.querySelector('#revert');
// const saveHtmlButton = document.querySelector('#save-html');
// const showFileButton = document.querySelector('#show-file');
// const openInDefaultButton = document.querySelector('#open-in-default');
// 
const parseButton = document.querySelector('#test')

const updateGraphviz = () => {
    // d3.select('#rendered-graphviz')
    //     .graphviz()
    //     .renderDot('digraph {a -> b}');
    
    
}

parseButton.addEventListener('click', (event) => {
    updateGraphviz();
})

// const renderMarkdownToHtml = (markdown) => {
//     // const sanitized = sanitize(markdown)

//     // htmlView.innerHTML = marked(sanitized);
// }


// const updateUserInterface = () => { 
//     // let title = "Fire Sale";
//     // if (filePath) {
//     //     title = `${filePath} - ${title}`
//     // }
    
//     // let window = BrowserWindow.fromWebContents(webContents);
//     // window.setTitle(title);
// }


// graphvizView.addEventListener('keyup', (event) => {
//     // const currentContent = event.target.value;
//     // renderMarkdownToHtml(currentContent);
// })

// openFileButton.addEventListener('click', () => { 
//     ipcRenderer.send(openFile);
// });

// newFileButton.addEventListener('click', () => {
//     ipcRenderer.send(newfile)
// })

// ipcRenderer.on('file-opened', (event, file, content) => {
//     // filePath = file;
//     // originalContent = content;

//     // graphvizView.value = content;
//     // renderMarkdownToHtml(content);
    
//     // updateUserInterface();
// });

// testParseOcgena.addEventListener('click', async () => {
//     // let probeResult = JSON.stringify(await ipcRenderer.invoke("test"));
//     // graphvizView.value = probeResult;
//     // renderMarkdownToHtml(probeResult);
// });