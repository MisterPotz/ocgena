export type Rule = 'ocdot' | 'ocnet' | 'node' | 'edge' | 'subgraph';
/**
 * Parse string written in dot language and convert it to a model.
 *
 * @remarks
 * The returned values are [ts-graphviz](https://github.com/ts-graphviz/ts-graphviz) models
 * such as `Digraph`, `Graph`, `Node`, `Edge`, `Subgraph`.
 *
 * ```ts
 * import { parse } from '@ts-graphviz/parser';
 *
 * const G = parse(`
 * digraph hoge {
 *   a -> b;
 * }`);
 * ```
 *
 * This is equivalent to the code below when using ts-graphviz.
 *
 * ```ts
 * import { digraph } from 'ts-graphviz';
 *
 * const G = digraph('hoge', (g) => {
 *   g.edge(['a', 'b']);
 * });
 * ```
 *
 * If the given string is invalid, a SyntaxError exception will be thrown.
 *
 * ```ts
 * import { parse, SyntaxError } from '@ts-graphviz/parser';
 *
 * try {
 *   parse(`invalid`);
 * } catch (e) {
 *   if (e instanceof SyntaxError) {
 *     console.log(e.message);
 *   }
 * }
 * ```
 *
 * @param dot string written in the dot language.
 * @param options.rule Object type of dot string.
 * This can be "graph", "subgraph", "node", "edge".
 *
 * @example
 * ```
 * import { Node } from 'ts-graphviz';
 * import { parse } from '@ts-graphviz/parser';
 *
 * const node = parse(
 *   `test [
 *     style=filled;
 *     color=lightgrey;
 *     label = "example #1";
 *   ];`,
 *   { rule: 'node' },
 * );
 *
 * console.log(node instanceof Node);
 * // true
 * ```
 * @throws {SyntaxError}
 */
