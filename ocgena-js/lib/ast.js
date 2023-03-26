import { parse as _parse } from './ocdot.peggy';
/**
 * The `AST` module provides the ability to handle the AST as a result of parsing the ocdot language
 * for lower level operations.
 *
 * @alpha
 */
export var AST;
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
        NodeRefGroup: 'node_ref_group',
        Subgraph: 'subgraph',
        Literal: 'literal',
        ClusterStatements: 'cluster_statements',
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
        return _parse(ocdot, {
            startRule: rule,
        });
    }
    AST.parse = parse;
    class Compiler {
        constructor({ indentSize = 2 } = {}) {
            this.indentSize = indentSize;
        }
        indent(line) {
            return ' '.repeat(this.indentSize) + line;
        }
        pad(pad) {
            return (l) => pad + l;
        }
        printAttribute(ast) {
            return `${this.stringify(ast.key)} = ${this.stringify(ast.value)};`;
        }
        printAttributes(ast) {
            return ast.body.length === 0
                ? `${ast.kind};`
                : `${ast.kind} [\n${ast.body.map(this.stringify.bind(this)).map(this.indent.bind(this)).join('\n')}\n];`;
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
            return ast.body.length === 0
                ? `${allEdgeTargets};`
                : `${allEdgeTargets} [\n${ast.body.map(this.stringify.bind(this)).map(this.indent.bind(this)).join('\n')}\n];`;
        }
        printEdgeRHSElement(edgeRHSElement) {
            const edgeOp = edgeRHSElement.edgeop.type;
            return `${edgeOp} ${this.stringify(edgeRHSElement.id)}`;
        }
        printNode(ast) {
            return ast.body.length == 0
                ? `${this.stringify(ast.id)};`
                : `${this.stringify(ast.id)} [\n${ast.body
                    .map(this.stringify.bind(this))
                    .map(this.indent.bind(this))
                    .join('\n')}\n];`;
        }
        printNodeRef(ast) {
            return [
                this.stringify(ast.id),
                ast.port ? this.stringify(ast.port) : null,
                ast.compass ? this.stringify(ast.compass) : null,
            ]
                .filter((v) => v !== null)
                .join(':');
        }
        printNodeRefGroup(ast) {
            return `{${ast.body.map(this.stringify.bind(this)).join(' ')}}`;
        }
        printOcNet(ast) {
            return [
                // ast.strict ? 'strict' : null,
                // ast.directed ? 'digraph' : 'graph',
                ast.id ? this.stringify(ast.id) : null,
                ast.body.length === 0
                    ? 'ocnet {}'
                    : `ocnet {\n${ast.body.map(this.stringify.bind(this)).map(this.indent.bind(this)).join('\n')}\n}`,
            ]
                .filter((v) => v !== null)
                .join(' ');
        }
        printSubgraph(ast) {
            return [
                'subgraph',
                ast.id ? this.stringify(ast.id) : null,
                ast.body.length === 0
                    ? '{}'
                    : `{\n${ast.body.map(this.stringify.bind(this)).map(this.indent.bind(this)).join('\n')}\n}`,
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
                    case AST.Types.NodeRefGroup:
                        return this.printNodeRefGroup(ast);
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
})(AST || (AST = {}));
//# sourceMappingURL=ast.js.map