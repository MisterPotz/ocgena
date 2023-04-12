import { Compass } from 'ts-graphviz';
import { FileRange as _FileRange } from './ocdot.peggy';
/**
 * The `AST` module provides the ability to handle the AST as a result of parsing the ocdot language
 * for lower level operations.
 *
 * @alpha
 */
export declare namespace AST {
    export type FileRange = _FileRange;
    type ValueOf<T> = T[keyof T];
    /**
     * DOT object types.
     */
    export const Types: Readonly<{
        readonly OcDot: "ocdot";
        readonly Comment: "comment";
        readonly Ocnet: "ocnet";
        readonly Attribute: "attribute";
        readonly Attributes: "attributes";
        readonly Edge: "edge";
        readonly Node: "node";
        readonly NodeRef: "node_ref";
        readonly Subgraph: "subgraph";
        readonly EdgeSubgraph: "edge_subgraph";
        readonly Literal: "literal";
        readonly ClusterStatements: "cluster_statements";
        readonly TypeDefinitions: "type_definitions";
    }>;
    export type Types = ValueOf<typeof Types>;
    export const SubgraphSpecialTypes: Readonly<{
        readonly Places: "places";
        readonly Transitions: "transitions";
        readonly ObjectTypes: "object types";
        readonly InitialMarking: "initial marking";
        readonly PlacesForType: "places for";
        readonly Inputs: "inputs";
        readonly Outputs: "outputs";
    }>;
    export type SubgraphSpecialTypes = ValueOf<typeof SubgraphSpecialTypes>;
    export const OpTypes: Readonly<{
        readonly Normal: "->";
        readonly Variable: "=>";
    }>;
    export type OpTypes = ValueOf<typeof OpTypes>;
    export function isASTBaseNode(value: unknown): value is ASTBaseNode;
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
        const Kind: Readonly<{
            readonly Block: "block";
            readonly Slash: "slash";
            readonly Macro: "macro";
        }>;
        type Kind = ValueOf<typeof Kind>;
    }
    /** Attributes AST object. */
    export interface Attributes extends ASTBaseParent<Attribute | Comment> {
        type: typeof Types.Attributes;
        kind: Attributes.Kind;
    }
    export namespace Attributes {
        const Kind: Readonly<{
            readonly Ocnet: "ocnet";
            readonly Edge: "edge";
            readonly Node: "node";
        }>;
        type Kind = ValueOf<typeof Kind>;
    }
    /** NodeRef AST object. */
    export interface NodeRef extends ASTBaseNode {
        type: typeof Types.NodeRef;
        id: Literal;
        port?: Literal;
        compass?: Literal<Compass>;
    }
    /** NodeRefGroup AST object. */
    export interface EdgeSubgraph extends ASTBaseParent<ClusterStatement> {
        type: typeof Types.EdgeSubgraph;
        id?: Literal;
    }
    export type EdgeTarget = NodeRef | EdgeSubgraph;
    export interface NumberLiteral extends ASTBaseNode {
        value: number;
    }
    export interface EdgeOpParams {
        number: NumberLiteral;
    }
    export interface EdgeOperator extends ASTBaseNode {
        type: "->" | "=>";
        params?: EdgeOpParams;
    }
    export interface EdgeRHSElement {
        id: EdgeTarget;
        edgeop: EdgeOperator;
    }
    /** Edge AST object. */
    export interface Edge extends ASTBaseParent<Attribute> {
        type: typeof Types.Edge;
        from: EdgeTarget;
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
        specialType?: SubgraphSpecialTypes;
    }
    export type TypeDefinition = Node | Comment;
    export type OcDotStatement = OcNet | Comment;
    export type ClusterStatement = Attribute | Attributes | Edge | Node | Subgraph | Comment;
    export type ASTNode = Attribute | Attributes | Comment | OcDot | Edge | OcNet | Literal | Node | NodeRef | EdgeSubgraph | Subgraph;
    export type Rule = typeof Types.OcDot | typeof Types.Ocnet | typeof Types.Node | typeof Types.Edge | typeof Types.Attributes | typeof Types.Subgraph | typeof Types.Attribute | typeof Types.ClusterStatements;
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
    export interface StringifyOption {
        indentSize?: number;
    }
    export class Compiler {
        protected indentSize: number;
        constructor({ indentSize }?: StringifyOption);
        protected indent(line: string): string;
        protected pad(pad: string): (l: string) => string;
        protected printAttribute(ast: AST.Attribute): string;
        protected printAttributes(ast: AST.Attributes): string;
        protected printComment(ast: AST.Comment): string;
        protected printOcDot(ast: AST.OcDot): string;
        protected printEdge(ast: AST.Edge): string;
        protected printEdgeRHSElement(edgeRHSElement: EdgeRHSElement): string;
        protected printNode(ast: AST.Node): string;
        protected printNodeRef(ast: AST.NodeRef): string;
        protected printEdgeSubgraphName(ast: AST.EdgeSubgraph): (string | null)[];
        protected printEdgeSubgraph(ast: AST.EdgeSubgraph): string;
        protected closingBracket(): string;
        protected withIndentIncrease(block: () => string): string;
        protected withIndentDecrease(block: () => string): string;
        protected closingBracketIndented(): string;
        protected printOcNet(ast: AST.OcNet): string;
        protected checkSubgraphKeyword(ast: AST.Subgraph): boolean;
        protected printSubgraphName(ast: AST.Subgraph): (string | null)[];
        protected printSubgraph(ast: AST.Subgraph): string;
        protected printLiteral(ast: AST.Literal): string;
        private isAstNode;
        stringify(ast: AST.ASTNode | EdgeRHSElement): string;
    }
    /**
     * Stringify Graphviz AST Node.
     *
     * @param ast Graphviz AST node.
     * @returns DOT language string.
     */
    export function stringify(ast: AST.ASTNode, options?: StringifyOption): string;
    export {};
}
