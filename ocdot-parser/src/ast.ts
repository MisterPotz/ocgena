import { Compass, Int } from 'ts-graphviz';
import { parse as _parse, FileRange as _FileRange } from './ocdot.peggy';
import { prependIndent } from './exts';

/**
 * The `AST` module provides the ability to handle the AST as a result of parsing the ocdot language
 * for lower level operations.
 *
 * @alpha
 */
export namespace AST {
  export type FileRange = _FileRange;
  type ValueOf<T> = T[keyof T];

  /**
   * DOT object types.
   */
  export const Types = Object.freeze({
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
  } as const);
  // 'ocdot' | 'comment' etc
  export type Types = ValueOf<typeof Types>;

  export const SubgraphSpecialTypes = Object.freeze({
    Places: 'places',
    Transitions: 'transitions',
  } as const)
  export type SubgraphSpecialTypes = ValueOf<typeof SubgraphSpecialTypes>

  export const OpTypes = Object.freeze({
    Normal: '->',
    Variable: '=>'
  } as const)
  export type OpTypes = ValueOf<typeof OpTypes>

  export const OpParamsTypes = Object.freeze({
    Expression : "expression",
    Number : "number"
  })
  export type OpParamsTypes = ValueOf<typeof OpParamsTypes>

  export function isASTBaseNode(value: unknown): value is ASTBaseNode {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return typeof value === 'object' && value !== null && typeof (value as any).type === 'string';
  }


  /**
   * AST node.
   */
  export interface ASTBaseNode {
    /**
     * Every leaf interface that extends ASTBaseNode
     * must specify a type property.
     */
    type: string;
    location: FileRange;
  }

  export interface ASTBaseParent<STMT extends ASTBaseNode = ASTBaseNode> extends ASTBaseNode {
    body: STMT[];
  }

  export interface Literal<T extends string = string> extends ASTBaseNode {
    type: typeof Types.Literal;
    value: T;
    quoted: boolean | 'html';
  }

  export interface OcDot extends ASTBaseParent<OcDotStatement> {
    type: typeof Types.OcDot;
  }

  /**
   * Graph AST object.
   */
  export interface OcNet extends ASTBaseParent<ClusterStatement> {
    type: typeof Types.Ocnet;
    id?: Literal;
  }

  export interface KeyValue {
    key: Literal;
    value: Literal;
  }

  /**
   * Attribute AST object.
   */
  export interface Attribute extends ASTBaseNode, KeyValue {
    type: typeof Types.Attribute;
  }

  /**
   * Comment AST object.
   */
  export interface Comment extends ASTBaseNode {
    type: typeof Types.Comment;
    kind: Comment.Kind;
    value: string;
  }
  export namespace Comment {
    export const Kind = Object.freeze({
      Block: 'block',
      Slash: 'slash',
      Macro: 'macro',
    } as const);
    export type Kind = ValueOf<typeof Kind>;
  }

  /** Attributes AST object. */
  export interface Attributes extends ASTBaseParent<Attribute | Comment> {
    type: typeof Types.Attributes;
    kind: Attributes.Kind;
  }
  export namespace Attributes {
    export const Kind = Object.freeze({
      Ocnet: Types.Ocnet,
      Edge: Types.Edge,
      Node: Types.Node,
    } as const);
    export type Kind = ValueOf<typeof Kind>;
  }

  /** NodeRef AST object. */
  export interface NodeRef extends ASTBaseNode {
    type: typeof Types.NodeRef;
    id: Literal;
    port?: Literal;
    compass?: Literal<Compass>;
  }

  /** NodeRefGroup AST object. */
  // export interface NodeRefGroup extends ASTBaseParent<NodeRef> {
  //   type: typeof Types.NodeRefGroup;
  // }

  export interface EdgeSubgraph extends ASTBaseParent<ClusterStatement> {
    type: typeof Types.EdgeSubgraph;
    id?: Literal;
  }

  export type EdgeTarget = NodeRef | EdgeSubgraph;

  export interface NumberLiteral extends ASTBaseNode {
    value: number,
  }

  export interface Variable { 
    variable : string
  }

  export interface ExpressionOp {
    op : "*" | "+" | "-" | "/",
    target : Expression
  }

  export interface Expression { 
    head : Expression | number | Variable
    tail : ExpressionOp[]
  }

  export interface RootExpression extends EdgeOpParams, Expression { 
    type : typeof OpParamsTypes.Expression
  }

  export interface EdgeOpParams extends ASTBaseNode {
    type : OpParamsTypes,
  }

  export interface OpParamsNumber extends EdgeOpParams {
    type : typeof OpParamsTypes.Number
    value : number
  }

  export interface EdgeOperator extends ASTBaseNode {
    type: "->" | "=>",
    params?: EdgeOpParams
  }

  export interface EdgeRHSElement {
    id: EdgeTarget,
    edgeop: EdgeOperator
  }


  /** Edge AST object. */
  export interface Edge extends ASTBaseParent<Attribute> {
    type: typeof Types.Edge;
    from: EdgeTarget,
    targets: [to: EdgeRHSElement, ...rest: EdgeRHSElement[]];
  }

  /** Node AST object. */
  export interface Node extends ASTBaseParent<Attribute> {
    type: typeof Types.Node;
    id: Literal;
  }

  /** Subgraph AST object. */
  export interface Subgraph extends ASTBaseParent<ClusterStatement> {
    type: typeof Types.Subgraph;
    id?: Literal;
    specialType?: SubgraphSpecialTypes
  }

  export type TypeDefinition = Node | Comment;
  export type OcDotStatement = OcNet | Comment;
  export type ClusterStatement = Attribute | Attributes | Edge | Node | Subgraph | Comment;

  export type ASTNode =
    | Attribute
    | Attributes
    | Comment
    | OcDot
    | Edge
    | OcNet
    | Literal
    | Node
    | NodeRef
    | EdgeSubgraph
    | Subgraph;

  export type Rule =
    | typeof Types.OcDot
    | typeof Types.Ocnet
    | typeof Types.Node
    | typeof Types.Edge
    | typeof Types.Attributes
    | typeof Types.Subgraph
    | typeof Types.Attribute
    | typeof Types.ClusterStatements;

  /**
   * Option interface for {@link parse} function.
   */
  export interface ParseOption<T extends Rule = Rule> {
    rule?: T;
  }

  /**
   * The basic usage is the same as the `parse` function,
   * except that it returns the ocdot language
   *
   * ```ts
   * import { AST } from '@ts-graphviz/parser';
   *
   * const ast = parse(`
   *   digraph example {
   *     node1 [
   *       label = "My Node",
   *     ]
   *   }
   * `);
   *
   * console.log(ast);
   * // {
   * //   type: 'ocdot',
   * //   body: [
   * //     {
   * //       type: 'graph',
   * //       id: {
   * //         type: 'literal',
   * //         value: 'example',
   * //         quoted: false,
   * //         location: {
   * //           start: { offset: 11, line: 2, column: 11 },
   * //           end: { offset: 18, line: 2, column: 18 }
   * //         }
   * //       },
   * //       directed: true,
   * //       strict: false,
   * //       body: [
   * //         {
   * //           type: 'node',
   * //           id: {
   * //             type: 'literal',
   * //             value: 'node1',
   * //             quoted: false,
   * //             location: {
   * //               start: { offset: 25, line: 3, column: 5 },
   * //               end: { offset: 30, line: 3, column: 10 }
   * //             }
   * //           },
   * //           body: [
   * //             {
   * //               type: 'attribute',
   * //               key: {
   * //                 type: 'literal',
   * //                 value: 'label',
   * //                 quoted: false,
   * //                 location: {
   * //                   start: { offset: 39, line: 4, column: 7 },
   * //                   end: { offset: 44, line: 4, column: 12 }
   * //                 }
   * //               },
   * //               value: {
   * //                 type: 'literal',
   * //                 value: 'My Node',
   * //                 quoted: true,
   * //                 location: {
   * //                   start: { offset: 47, line: 4, column: 15 },
   * //                   end: { offset: 56, line: 4, column: 24 }
   * //                 }
   * //               },
   * //               location: {
   * //                 start: { offset: 39, line: 4, column: 7 },
   * //                 end: { offset: 57, line: 4, column: 25 }
   * //               }
   * //             }
   * //           ],
   * //           location: {
   * //             start: { offset: 25, line: 3, column: 5 },
   * //             end: { offset: 63, line: 5, column: 6 }
   * //           }
   * //         }
   * //       ],
   * //       location: {
   * //         start: { offset: 3, line: 2, column: 3 },
   * //         end: { offset: 67, line: 6, column: 4 }
   * //       }
   * //     }
   * //   ],
   * //   location: {
   * //     start: { offset: 3, line: 2, column: 3 },
   * //     end: { offset: 68, line: 7, column: 1 }
   * //   }
   * // }
   * ```
   *
   * @param ocdot string in the ocdot language to be parsed.
   * @param options.rule Object type of ocdot string.
   * This can be "graph", "subgraph", "node", "edge",
   * "attributes", "attribute", "cluster_statements".
   *
   * @example
   * const ast = parse('test [ style=filled; ];', { rule: 'node' });
   *
   * console.log(ast);
   * // {
   * //   type: 'node',
   * //   id: {
   * //     type: 'literal',
   * //     value: 'test',
   * //     quoted: false,
   * //     location: {
   * //       start: { offset: 0, line: 1, column: 1 },
   * //       end: { offset: 4, line: 1, column: 5 }
   * //     }
   * //   },
   * //   body: [
   * //     {
   * //       type: 'attribute',
   * //       key: {
   * //         type: 'literal',
   * //         value: 'style',
   * //         quoted: false,
   * //         location: {
   * //           start: { offset: 7, line: 1, column: 8 },
   * //           end: { offset: 12, line: 1, column: 13 }
   * //         }
   * //       },
   * //       value: {
   * //         type: 'literal',
   * //         value: 'filled',
   * //         quoted: false,
   * //         location: {
   * //           start: { offset: 13, line: 1, column: 14 },
   * //           end: { offset: 19, line: 1, column: 20 }
   * //         }
   * //       },
   * //       location: {
   * //         start: { offset: 7, line: 1, column: 8 },
   * //         end: { offset: 20, line: 1, column: 21 }
   * //       }
   * //     }
   * //   ],
   * //   location: {
   * //     start: { offset: 0, line: 1, column: 1 },
   * //     end: { offset: 23, line: 1, column: 24 }
   * //   }
   * // }
   *
   * @returns The AST object of the parse result is returned.
   *
   * @throws {SyntaxError}
   */
  export function parse(ocdot: string): OcDot;
  export function parse(ocdot: string, options: ParseOption<typeof Types.Edge>): Edge;
  export function parse(ocdot: string, options: ParseOption<typeof Types.Node>): Node;
  export function parse(ocdot: string, options: ParseOption<typeof Types.Ocnet>): OcNet;
  export function parse(ocdot: string, options: ParseOption<typeof Types.Attribute>): Attribute;
  export function parse(ocdot: string, options: ParseOption<typeof Types.Attributes>): Attributes;
  export function parse(ocdot: string, options: ParseOption<typeof Types.Subgraph>): Subgraph;
  export function parse(ocdot: string, options: ParseOption<typeof Types.ClusterStatements>): ClusterStatement[];
  export function parse(ocdot: string, options: ParseOption<typeof Types.OcDot>): OcDot;
  export function parse(ocdot: string, options: ParseOption): OcDot | OcNet | ClusterStatement | ClusterStatement[];
  export function parse(ocdot: string, { rule }: ParseOption = {}): OcDot | OcNet | ClusterStatement | ClusterStatement[] {
    return _parse(ocdot, {
      startRule: rule,
    });
  }

  export function parseExpression(expression : string) : RootExpression {
    return _parse(expression, {
      startRule: "RootExpression"
    })
  }

  export interface StringifyOption {
    indentSize?: number;
  }

  
  export function isEdgeOpParamsExpression(edgeOpParams : EdgeOpParams): edgeOpParams is RootExpression { 
    return edgeOpParams.type == OpParamsTypes.Expression
  }

  export function isEdgeOpParamsNumber(edgeOpParams : EdgeOpParams) : edgeOpParams is OpParamsNumber {
    return edgeOpParams.type == OpParamsTypes.Number
  }

  export function isExpression(item : any) : item is Expression {
    if (typeof item !== "object") {
      return false
    }
    return "head" in item
  }

  export function isVariable(object : any) : object is Variable { 
    if (typeof object !== "object") {
      return false
    }
    return "variable" in object
  }

  export class Compiler {
    protected indentSize: number;
    constructor({ indentSize = 0 }: StringifyOption = {}) {
      this.indentSize = indentSize;
    }

    protected indent(line: string): string {
      return '  '.repeat(this.indentSize) + line;
    }

    protected indentForSize(size : number) : string {
      return '  '.repeat(size);
    }

    protected buildIndent(): string {
      return this.indentForSize(this.indentSize)
    }

    protected withIndentIncrease(multiline: string): string {
      // this.increaseIndent()
      const saved = this.indentSize
      this.indentSize = 2
      const value = prependIndent(multiline, this.buildIndent())
      // this.decreaseIndent()
      this.indentSize = saved;
      return value
    }

    protected pad(pad: string): (l: string) => string {
      return (l: string) => pad + l;
    }

    protected printAttribute(ast: AST.Attribute): string {
      return `${this.stringify(ast.key)} = ${this.stringify(ast.value)};`;
    }

    protected increaseIndent() {
      this.indentSize += 2;
    }

    protected decreaseIndent() {
      this.indentSize -= 2;
    }

    protected printAttributes(ast: AST.Attributes): string {
      const value = ast.body.length === 0
        ? `${ast.kind};`
        : `${ast.kind} [\n${this.withIndentIncrease(ast.body.map(this.stringify, this).join('\n'))}\n];`;
      return value;
    }

    protected printComment(ast: AST.Comment): string {
      switch (ast.kind) {
        case AST.Comment.Kind.Block:
          return '/**\n' + ast.value.split('\n').map(this.pad(' * ')).join('\n') + '\n */';
        case AST.Comment.Kind.Slash:
          return ast.value.split('\n').map(this.pad('// ')).join('\n');
        case AST.Comment.Kind.Macro:
          return ast.value.split('\n').map(this.pad('# ')).join('\n');
      }
    }
    protected printOcDot(ast: AST.OcDot): string {
      return ast.body.map(this.stringify.bind(this)).join('\n');
    }

    protected printEdge(ast: AST.Edge): string {
      const edgeOp = ast
      const from = this.stringify(ast.from)
      const targets = ast.targets.map(this.stringify.bind(this))/* .join(this.directed ? ' -> ' : ' -- '); */
      const allEdgeTargets = [from, ...targets].join(' ')

      if (ast.body.length === 0) {
        return allEdgeTargets + ";"
      }

      return `${allEdgeTargets} [\n${this.withIndentIncrease(ast.body.map(this.stringify, this)
        .join('\n'))
        }\n]`
    }

    protected stringifyExpressionElement(head : Expression | number | Variable) : string {
      if (isExpression(head)) {
        return this.stringifyExpression(head);
      } else if (isVariable(head)) {
        return head.variable
      } else {
        return head.toString()
      }
    }

    protected stringifyExpressionOp(expressionOp : ExpressionOp) : string { 
      let op = expressionOp.op
      let expression = this.stringifyExpressionElement(expressionOp.target)
      return `${op} ${expression}`
    }

    public stringifyExpression(expression : Expression) : string {
      let arr = [ ]
      if (expression.tail.length == 0 && !isExpression(expression.head)) {
        return this.stringifyExpressionElement(expression.head);
      }
      arr.push(this.stringifyExpressionElement(expression.head))
      for (let i = 0; i < expression.tail.length; i++) {
        arr.push(this.stringifyExpressionOp(expression.tail[i]))
      }
      return "(" + arr.join(' ') + ")";
    }

    protected printEdgeOpParams(edgeOpParams : EdgeOpParams) : string {
      if (isEdgeOpParamsExpression(edgeOpParams)) {
        return this.stringifyExpression(edgeOpParams)
      } else if (isEdgeOpParamsNumber(edgeOpParams))  {
        return edgeOpParams.value.toString()
      } else {
        return "UNKNOWN_EDGE_OP_PARAM"
      }
    }

    protected printEdgeRHSElement(edgeRHSElement: EdgeRHSElement): string {
      const edgeOp = edgeRHSElement.edgeop.type
      const multiplicity = edgeRHSElement.edgeop.params
        ? this.printEdgeOpParams(edgeRHSElement.edgeop.params)
        : ""
      return `${multiplicity}${edgeOp} ${this.stringify(edgeRHSElement.id)}`
    }

    protected printNode(ast: AST.Node): string {
      if (ast.body.length === 0) {
        return this.stringify(ast.id)
      }
      return `${this.stringify(ast.id)} [\n${this.withIndentIncrease(ast.body.map(this.stringify, this)
        .join('\n'))
        }\n];`
    }

    protected printNodeRef(ast: AST.NodeRef): string {
      return [
        this.stringify(ast.id),
        ast.port ? this.stringify(ast.port)
          : null,
        ast.compass ? this.stringify(ast.compass) : null,
      ]
        .filter((v) => v !== null)
        .join(':');
    }

    protected printEdgeSubgraphName(ast: AST.EdgeSubgraph): (string | null)[] {
      if (ast.id == null) {
        return []
      } else {
        return ['subgraph', this.stringify(ast.id)]
      }
    }

    protected printEdgeSubgraph(ast: AST.EdgeSubgraph): string {
      if (ast.body.length === 0) {
        return "{}"
      }
      const body = `{\n${this.withIndentIncrease(ast.body.map(this.stringify, this).join('\n'))
        }\n}`

      return [
        ...this.printEdgeSubgraphName(ast),
        body
      ]
        .filter((v) => v !== null)
        .join(' ');
    }

    protected printOcNet(ast: AST.OcNet): string {
      var body = "ocnet {"
      if (ast.body.length === 0) {
        body += "}"
        return body
      }
      body += "\n"
      body += this.withIndentIncrease(ast.body
        .map(this.stringify.bind(this))
        .join('\n'))
      body += "\n}"

      return [
        // ast.strict ? 'strict' : null,
        // ast.directed ? 'digraph' : 'graph',
        ast.id ? this.stringify(ast.id) : null,
        body,
      ]
        .filter((v) => v !== null)
        .join(' ');
    }

    protected checkSubgraphKeyword(ast: AST.Subgraph): boolean {
      return ast.specialType != null
    }

    protected printSubgraphName(ast: AST.Subgraph): (string | null)[] {
      if (this.checkSubgraphKeyword(ast)) {
        return [ast.specialType ?? "", ast.id ? this.stringify(ast.id) : null]
      } else {
        return ['subgraph', ast.id ? this.stringify(ast.id) : null]
      }
    }

    protected printSubgraph(ast: AST.Subgraph): string {
      var body = "{"
      if (ast.body.length === 0) {
        body += "}"
        return body
      }
      body += "\n"
      body += this.withIndentIncrease(
        ast.body.map(this.stringify.bind(this)).join('\n')
      )
      body += "\n}"

      return [
        ...this.printSubgraphName(ast),
        body
      ]
        .filter((v) => v !== null)
        .join(' ');
    }

    protected printLiteral(ast: AST.Literal): string {
      switch (ast.quoted) {
        case true:
          return `"${ast.value}"`;
        case false:
          return ast.value;
        case 'html':
          return `<${ast.value}>`;
      }
    }


    protected isAstNode(object: any): object is AST.ASTNode {
      if (typeof object !== "object") {
        return false
      }
      return 'type' in object;
    }

    public stringify(ast: AST.ASTNode | EdgeRHSElement): string {
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
      } else {
        return this.printEdgeRHSElement(ast)
      }
    }
  }

  /**
   * Stringify Graphviz AST Node.
   *
   * @param ast Graphviz AST node.
   * @returns DOT language string.
   */
  export function stringify(ast: AST.ASTNode, options?: StringifyOption): string {
    return new Compiler(options).stringify(ast);
  }
}
