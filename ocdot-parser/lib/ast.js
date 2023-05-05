"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.AST = void 0;
const ocdot_peggy_1 = require("./ocdot.peggy");
const exts_1 = require("./exts");
/**
 * The `AST` module provides the ability to handle the AST as a result of parsing the ocdot language
 * for lower level operations.
 *
 * @alpha
 */
var AST;
(function (AST) {
    /**
     * DOT object types.
     */
    AST.Types = Object.freeze({
        OcDot: 'ocdot',
        Comment: 'comment',
        Ocnet: 'ocnet',
        Attribute: 'attribute',
        Attributes: 'attributes',
        Edge: 'edge',
        Node: 'node',
        NodeRef: 'node_ref',
        // NodeRefGroup: 'node_ref_group',
        Subgraph: 'subgraph',
        EdgeSubgraph: 'edge_subgraph',
        Literal: 'literal',
        ClusterStatements: 'cluster_statements',
        TypeDefinitions: 'type_definitions'
    });
    AST.SubgraphSpecialTypes = Object.freeze({
        Places: 'places',
        Transitions: 'transitions',
    });
    AST.OpTypes = Object.freeze({
        Normal: '->',
        Variable: '=>'
    });
    AST.OpParamsTypes = Object.freeze({
        Expression: "expression",
        Number: "number"
    });
    function isASTBaseNode(value) {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        return typeof value === 'object' && value !== null && typeof value.type === 'string';
    }
    AST.isASTBaseNode = isASTBaseNode;
    let Comment;
    (function (Comment) {
        Comment.Kind = Object.freeze({
            Block: 'block',
            Slash: 'slash',
            Macro: 'macro',
        });
    })(Comment = AST.Comment || (AST.Comment = {}));
    let Attributes;
    (function (Attributes) {
        Attributes.Kind = Object.freeze({
            Ocnet: AST.Types.Ocnet,
            Edge: AST.Types.Edge,
            Node: AST.Types.Node,
        });
    })(Attributes = AST.Attributes || (AST.Attributes = {}));
    function parse(ocdot, { rule } = {}) {
        return (0, ocdot_peggy_1.parse)(ocdot, {
            startRule: rule,
        });
    }
    AST.parse = parse;
    class Compiler {
        indentSize;
        constructor({ indentSize = 0 } = {}) {
            this.indentSize = indentSize;
        }
        indent(line) {
            return '  '.repeat(this.indentSize) + line;
        }
        indentForSize(size) {
            return '  '.repeat(size);
        }
        buildIndent() {
            return this.indentForSize(this.indentSize);
        }
        withIndentIncrease(multiline) {
            // this.increaseIndent()
            const saved = this.indentSize;
            this.indentSize = 2;
            const value = (0, exts_1.prependIndent)(multiline, this.buildIndent());
            // this.decreaseIndent()
            this.indentSize = saved;
            return value;
        }
        pad(pad) {
            return (l) => pad + l;
        }
        printAttribute(ast) {
            return `${this.stringify(ast.key)} = ${this.stringify(ast.value)};`;
        }
        increaseIndent() {
            this.indentSize += 2;
        }
        decreaseIndent() {
            this.indentSize -= 2;
        }
        printAttributes(ast) {
            const value = ast.body.length === 0
                ? `${ast.kind};`
                : `${ast.kind} [\n${this.withIndentIncrease(ast.body.map(this.stringify, this).join('\n'))}\n];`;
            return value;
        }
        printComment(ast) {
            switch (ast.kind) {
                case AST.Comment.Kind.Block:
                    return '/**\n' + ast.value.split('\n').map(this.pad(' * ')).join('\n') + '\n */';
                case AST.Comment.Kind.Slash:
                    return ast.value.split('\n').map(this.pad('// ')).join('\n');
                case AST.Comment.Kind.Macro:
                    return ast.value.split('\n').map(this.pad('# ')).join('\n');
            }
        }
        printOcDot(ast) {
            return ast.body.map(this.stringify.bind(this)).join('\n');
        }
        printEdge(ast) {
            const edgeOp = ast;
            const from = this.stringify(ast.from);
            const targets = ast.targets.map(this.stringify.bind(this)); /* .join(this.directed ? ' -> ' : ' -- '); */
            const allEdgeTargets = [from, ...targets].join(' ');
            if (ast.body.length === 0) {
                return allEdgeTargets + ";";
            }
            return `${allEdgeTargets} [\n${this.withIndentIncrease(ast.body.map(this.stringify, this)
                .join('\n'))}\n]`;
        }
        isEdgeOpParamsExpression(edgeOpParams) {
            return edgeOpParams.type == AST.OpParamsTypes.Expression;
        }
        isEdgeOpParamsNumber(edgeOpParams) {
            return edgeOpParams.type == AST.OpParamsTypes.Number;
        }
        isExpression(item) {
            if (typeof item !== "object") {
                return false;
            }
            return "head" in item;
        }
        isVariable(object) {
            if (typeof object !== "object") {
                return false;
            }
            return "variable" in object;
        }
        stringifyExpressionElement(head) {
            if (this.isExpression(head)) {
                return this.stringifyExpression(head);
            }
            else if (this.isVariable(head)) {
                return head.variable;
            }
            else {
                return head.toString();
            }
        }
        stringifyExpressionOp(expressionOp) {
            let op = expressionOp.op;
            let expression = this.stringifyExpressionElement(expressionOp.target);
            return `${op} ${expression}`;
        }
        stringifyExpression(expression) {
            let arr = [];
            if (expression.tail.length == 0 && !this.isExpression(expression.head)) {
                return this.stringifyExpressionElement(expression.head);
            }
            arr.push(this.stringifyExpressionElement(expression.head));
            for (let i = 0; i < expression.tail.length; i++) {
                arr.push(this.stringifyExpressionOp(expression.tail[i]));
            }
            return "(" + arr.join(' ') + ")";
        }
        printEdgeOpParams(edgeOpParams) {
            if (this.isEdgeOpParamsExpression(edgeOpParams)) {
                return this.stringifyExpression(edgeOpParams);
            }
            else if (this.isEdgeOpParamsNumber(edgeOpParams)) {
                return edgeOpParams.value.toString();
            }
            else {
                return "UNKNOWN_EDGE_OP_PARAM";
            }
        }
        printEdgeRHSElement(edgeRHSElement) {
            const edgeOp = edgeRHSElement.edgeop.type;
            const multiplicity = edgeRHSElement.edgeop.params
                ? this.printEdgeOpParams(edgeRHSElement.edgeop.params)
                : "";
            return `${multiplicity}${edgeOp} ${this.stringify(edgeRHSElement.id)}`;
        }
        printNode(ast) {
            if (ast.body.length === 0) {
                return this.stringify(ast.id);
            }
            return `${this.stringify(ast.id)} [\n${this.withIndentIncrease(ast.body.map(this.stringify, this)
                .join('\n'))}\n];`;
        }
        printNodeRef(ast) {
            return [
                this.stringify(ast.id),
                ast.port ? this.stringify(ast.port)
                    : null,
                ast.compass ? this.stringify(ast.compass) : null,
            ]
                .filter((v) => v !== null)
                .join(':');
        }
        printEdgeSubgraphName(ast) {
            if (ast.id == null) {
                return [];
            }
            else {
                return ['subgraph', this.stringify(ast.id)];
            }
        }
        printEdgeSubgraph(ast) {
            if (ast.body.length === 0) {
                return "{}";
            }
            const body = `{\n${this.withIndentIncrease(ast.body.map(this.stringify, this).join('\n'))}\n}`;
            return [
                ...this.printEdgeSubgraphName(ast),
                body
            ]
                .filter((v) => v !== null)
                .join(' ');
        }
        printOcNet(ast) {
            var body = "ocnet {";
            if (ast.body.length === 0) {
                body += "}";
                return body;
            }
            body += "\n";
            body += this.withIndentIncrease(ast.body
                .map(this.stringify.bind(this))
                .join('\n'));
            body += "\n}";
            return [
                // ast.strict ? 'strict' : null,
                // ast.directed ? 'digraph' : 'graph',
                ast.id ? this.stringify(ast.id) : null,
                body,
            ]
                .filter((v) => v !== null)
                .join(' ');
        }
        checkSubgraphKeyword(ast) {
            return ast.specialType != null;
        }
        printSubgraphName(ast) {
            if (this.checkSubgraphKeyword(ast)) {
                return [ast.specialType ?? "", ast.id ? this.stringify(ast.id) : null];
            }
            else {
                return ['subgraph', ast.id ? this.stringify(ast.id) : null];
            }
        }
        printSubgraph(ast) {
            var body = "{";
            if (ast.body.length === 0) {
                body += "}";
                return body;
            }
            body += "\n";
            body += this.withIndentIncrease(ast.body.map(this.stringify.bind(this)).join('\n'));
            body += "\n}";
            return [
                ...this.printSubgraphName(ast),
                body
            ]
                .filter((v) => v !== null)
                .join(' ');
        }
        printLiteral(ast) {
            switch (ast.quoted) {
                case true:
                    return `"${ast.value}"`;
                case false:
                    return ast.value;
                case 'html':
                    return `<${ast.value}>`;
            }
        }
        isAstNode(object) {
            if (typeof object !== "object") {
                return false;
            }
            return 'type' in object;
        }
        stringify(ast) {
            if (this.isAstNode(ast)) {
                switch (ast.type) {
                    case AST.Types.Attribute:
                        return this.printAttribute(ast);
                    case AST.Types.Attributes:
                        return this.printAttributes(ast);
                    case AST.Types.Comment:
                        return this.printComment(ast);
                    case AST.Types.OcDot:
                        return this.printOcDot(ast);
                    case AST.Types.Edge:
                        return this.printEdge(ast);
                    case AST.Types.Node:
                        return this.printNode(ast);
                    case AST.Types.NodeRef:
                        return this.printNodeRef(ast);
                    case AST.Types.EdgeSubgraph:
                        return this.printEdgeSubgraph(ast);
                    case AST.Types.Ocnet:
                        // this.directed = ast.directed;
                        return this.printOcNet(ast);
                    case AST.Types.Subgraph:
                        return this.printSubgraph(ast);
                    case AST.Types.Literal:
                        return this.printLiteral(ast);
                }
            }
            else {
                return this.printEdgeRHSElement(ast);
            }
        }
    }
    AST.Compiler = Compiler;
    /**
     * Stringify Graphviz AST Node.
     *
     * @param ast Graphviz AST node.
     * @returns DOT language string.
     */
    function stringify(ast, options) {
        return new Compiler(options).stringify(ast);
    }
    AST.stringify = stringify;
})(AST = exports.AST || (exports.AST = {}));
//# sourceMappingURL=ast.js.map