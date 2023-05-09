/**
 * This file will automatically be loaded by webpack and run in the "renderer" context.
 * To learn more about the differences between the "main" and the "renderer" context in
 * Electron, visit:
 *
 * https://electronjs.org/docs/latest/tutorial/process-model
 *
 * By default, Node.js integration in this file is disabled. When enabling Node.js integration
 * in a renderer process, please be aware of potential security implications. You can read
 * more about security risks here:
 *
 * https://electronjs.org/docs/tutorial/security
 *
 * To enable Node.js integration in this file, open up `main.js` and enable the `nodeIntegration`
 * flag:
 *
 * ```
 *  // Create the browser window.
 *  mainWindow = new BrowserWindow({
 *    width: 800,
 *    height: 600,
 *    webPreferences: {
 *      nodeIntegration: true
 *    }
 *  });
 * ```
 */

import './index.css';
import * as monaco from 'monaco-editor';
import $ from 'jquery';


declare var window: any;
window.$ = window.jQuery = $;

import { GoldenLayout, LayoutConfig, LayoutManager, ComponentItem } from 'golden-layout';

console.log('👋 This message is being logged by "renderer.js", included via webpack');

// monaco.editor.create(
//     document.getElementById('code_container'), {
//     value: 'console.log("Hello, world!")',
//     language: 'javascript',
//     automaticLayout: true
// });



var config: LayoutConfig = {
    root: {
        type: 'row',
        content: [
            {
                type: 'component',
                componentName: 'testComponent',
                componentType: "testComponent",
                componentState: { label: 'A' }
            }, {
                type: 'column',
                content: [{
                    type: 'component',
                    componentType: "testComponent",
                    componentName: 'testComponent',
                    componentState: { label: 'B' }
                }, {
                    type: 'component',
                    componentName: 'testComponent',
                    componentType: "testComponent",
                    componentState: { label: 'C' }
                }]
            }
        ]
    }
};

var myLayout: GoldenLayout = new GoldenLayout(document.getElementById("multiwindow"));


myLayout.registerComponentFactoryFunction('testComponent', function (container, state, virtual) {
    let stateA : any = state;
    container.element.innerHTML = '<h2>' + stateA.label + '</h2>';
});

myLayout.loadLayout(config);

myLayout.init();