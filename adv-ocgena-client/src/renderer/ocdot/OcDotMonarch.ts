// import { editor } from "monaco-editor";
import { languages } from 'monaco-editor';
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';


export function isOcDotRegistered(): boolean {
    return monaco.languages.getLanguages().find((item) => item.id === "ocdot") != null
}

export function registerOcDot() {

    monaco.languages.register({ id: "ocdot" });

    monaco.languages.setMonarchTokensProvider(
        "ocdot",
        ocDotLanguage
    );
}

export const ocDotLanguage = <languages.IMonarchLanguage>{
    // Set defaultToken to invalid to see what you do not tokenize yet
    // defaultToken: 'invalid',

    keywords: [
        'strict', 'graph', 'digraph', 'node', 'edge', 'subgraph', 'rank', 'abstract',
        'n', 'ne', 'e', 'se', 's', 'sw', 'w', 'nw', 'c', '_',
        '->', ':', '=', ',',
    ],

    builtins: [
        'rank', 'rankdir', 'ranksep', 'size', 'ratio',
        'label', 'headlabel', 'taillabel',
        'arrowhead', 'samehead', 'samearrowhead',
        'arrowtail', 'sametail', 'samearrowtail', 'arrowsize',
        'labeldistance', 'labelangle', 'labelfontsize',
        'dir', 'width', 'height', 'angle',
        'fontsize', 'fontcolor', 'same', 'weight', 'color',
        'bgcolor', 'style', 'shape', 'fillcolor', 'nodesep', 'id',
    ],

    attributes: [
        'doublecircle', 'circle', 'diamond', 'box', 'point', 'ellipse', 'record',
        'inv', 'invdot', 'dot', 'dashed', 'dotted', 'filled', 'back', 'forward',
    ],

    // we include these common regular expressions
    symbols: /[=><!~?:&|+\-*\/\^%]+/,

    // The main tokenizer for our languages
    tokenizer: {
        root: [
            // identifiers and keywords
            [/[a-zA-Z_\x80-\xFF][\w\x80-\xFF]*/, {
                cases: {
                    '@keywords': 'keyword',
                    '@builtins': 'predefined',
                    '@attributes': 'constructor',
                    '@default': 'identifier'
                }
            }],

            // whitespace
            { include: '@whitespace' },

            // declare for multiplicities arithmetics
            [/(?=(\([()+\-\d*\s\w]+(=>|->)))/, 'mult','@mult'],


            // html identifiers
            [/<(?!@symbols)/, { token: 'string.html.quote', bracket: '@open', next: 'html' }],

            // do not scan as brackets 
            // [/\((?=(@symbols|@numbers)+\)@whitespace->)/, 'multiplicity.operator', '@multiplicity'],

            // delimiters and operators
            [/[{}()\[\]]/, '@brackets'],
            [/@symbols/, {
                cases: {
                    '@keywords': 'keyword',
                    '@default': 'operator'
                }
            }],

            // delimiter
            [/[;,]/, 'delimiter'],

            // numbers
            [/\d*\.\d+([eE][\-+]?\d+)?/, 'number.float'],
            [/0[xX][0-9a-fA-F]+/, 'number.hex'],
            [/\d+/, 'number'],

            // strings
            [/"([^"\\]|\\.)*$/, 'string.invalid'],  // non-teminated string
            [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }],
        ],

        comment: [
            [/[^\/*]+/, 'comment'],
            [/\/\*/, 'comment', '@push'],    // nested comment
            ["\\*/", 'comment', '@pop'],
            [/[\/*]/, 'comment']
        ],

        html: [
            [/[^<>&]+/, 'string.html'],
            [/&\w+;/, 'string.html.escape'],
            [/&/, 'string.html'],
            [/</, { token: 'string.html.quote', bracket: '@open', next: '@push' }], //nested bracket
            [/>/, { token: 'string.html.quote', bracket: '@close', next: '@pop' }],
        ],

        string: [
            [/[^\\"&]+/, 'string'],
            [/\\"/, 'string.escape'],
            [/&\w+;/, 'string.escape'],
            [/[\\&]/, 'string'],
            [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }]
        ],

        mult: [
            // not arrows nor opening/closinj brackat, cauz we wanna track in the stack all da brakkaz
            [/((?!((->)|(=>)|\(|\))).)+/, 'mult'],
            // [/bro/, 'broment', "@push"],
            [/\(/, 'mult', '@push'],
            [/\)/, 'mult', '@pop'],
            [/((->)|(=>))/, 'mult', "@pop"],
          ],

        whitespace: [
            [/[ \t\r\n]+/, 'white'],
            [/\/\*/, 'comment', '@comment'],
            [/\/\/.*$/, 'comment'],
            [/#.*$/, 'comment'],
        ],

        // multiplicity: [
        //     [/\(/, { token: '@brackets.type' }, '@multiplicity_nested'],
        //     { include: '@multiplicity_content' }
        // ],
        // multiplicity_nested: [
        //     [/\)/, { token: '@brackets.type' }, '@pop'],
        //     [/\(/, { token: '@brackets.type' }, '@push'],
        //     { include: '@multiplicity_content' }
        // ],

        // multiplicity_content: [
        //     { include: '@whitespace' },
        //     { include: '@symbols' },
        //     { include: '@numbers' },
        //     // // type identifiers
        //     // [/@symbols|@numbers/, 'multiplicity.item'],
        //     ['', '', '@pop']  // catch all
        // ]

    },
};
