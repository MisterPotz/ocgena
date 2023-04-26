import { AST } from 'ocdot-parser'
import { isEmptyOrBlank } from './exts';

interface EdgePair {
  from: AST.EdgeTarget
  to: AST.EdgeTarget
  edgeOp: '->' | '=>'
}

export class OCDotToDOTConverter extends AST.Compiler {
  ocDot: AST.OcDot;

  constructor(
    ocDot: AST.OcDot
  ) {
    super({ indentSize: 0 });
    this.ocDot = ocDot;
  }

  compileDot(): string {
    return this.stringify(this.ocDot)
  }

  protected printSubgraph(ast: AST.Subgraph): string {
    if (ast.specialType == null) {
      return super.printSubgraph(ast);
    } else {
      return ""
    }
  }

  protected findColorAttr(ast: AST.Edge): string | null {
    return ast.body.find((attr) => attr.key.value == 'color')?.value?.value;
  }

  protected makeEdgeOpColorAttr(edgeOp: '->' | '=>', color: string | null): string {
    let colorAttr;
    if (color == null) {
      color = 'black';
    }
    switch (edgeOp) {
      case "->":
        colorAttr = `color="${color}"`
        break;
      case "=>":
        colorAttr = `color="${color}:white:${color}"`
        break;
    }
    return colorAttr;
  }

  protected makeEdgePairs(ast: AST.Edge): EdgePair[] {
    let size = ast.targets.length
    if (size > 1) {
      const pairs = []
      const firstPair: EdgePair = {
        from: ast.from,
        to: ast.targets[0].id,
        edgeOp: ast.targets[0].edgeop.type
      }
      pairs.push(firstPair)

      for (let i = 0; i < size - 1; i++) {
        const pair: EdgePair = {
          from: ast.targets[i].id,
          to: ast.targets[i + 1].id,
          edgeOp: ast.targets[i + 1].edgeop.type
        }
        pairs.push(pair);
      }

      return pairs;
    } else {
      return []
    }
  }

  protected printEdgePair(edgePair: EdgePair, edge: AST.Edge, originalEdgeAttributes: AST.Attribute[]): string {
    const foundColor = this.findColorAttr(edge);
    const color = this.makeEdgeOpColorAttr(edgePair.edgeOp, foundColor)
    const from = this.stringify(edgePair.from);

    const value = `${from} -> ${this.stringify(edgePair.to)
      } [${color}; ${originalEdgeAttributes
        .map(this.stringify, this)
        .join('; ')
      }];`

    return value;
  }


  protected indentAllButFirst(items : string[]) : string[] {
    if (items.length == 0) return items;
    let newItems = []
    newItems.push(items[0]);
    for (let i = 1; i < items.length; i++) {
      newItems.push(this.indent(items[i]));
    }
    return newItems
  }

  protected override printEdge(ast: AST.Edge): string {
    const edgePairs = this.makeEdgePairs(ast);
    if (edgePairs.length === 0) {
      return ""
    }
    const colorFilteredAttrs = ast.body.filter((value) => {
        return value.key.value !== 'color'
    }, this)

    return this.indentAllButFirst(edgePairs.map((edgePair) => {
      return this.printEdgePair(edgePair, ast, colorFilteredAttrs);
    }))
      .join('\n') + "\n";
  }

  protected override printNode(ast: AST.Node): string {
    this.increaseIndent();
    const value = ast.body.length == 0
      ? `${this.stringify(ast.id)};`
      : `${this.stringify(ast.id)} [\n${ast.body
        .map(this.printFilteredAttribute, this)
        .filter((str) => !isEmptyOrBlank(str))
        .map(this.indent.bind(this))
        .join('\n')}\n];`;
    this.decreaseIndent();
    return value;
  }

  protected isAttributeOcdotSpecific(ast: AST.Attribute): boolean {
    return false
  }

  protected printFilteredAttribute(ast: AST.Attribute): string {
    if (this.isAttributeOcdotSpecific(ast)) {
      return ""
    }
    return `${this.stringify(ast.key)} = ${this.stringify(ast.value)};`;
  }

  override stringify(ast: AST.EdgeRHSElement | AST.ASTNode): string {
      const value = super.stringify(ast);
      if (isEmptyOrBlank(value)) { 
        return "";
      }
      return value;
  }

  protected printOcNet(ast: AST.OcNet): string {
    const body = this.withIndentIncrease(() => {
      return ast.body.length === 0
        ? 'digraph {}'
        : `digraph {\n${ast.body
          .map(this.stringify, this)
          .filter((v) => !isEmptyOrBlank(v), this)
          .map(this.indent.bind(this))
          .join('\n')
        }\n${this.closingBracketIndented()}`
    })

    return [
      // ast.strict ? 'strict' : null,
      // ast.directed ? 'digraph' : 'graph',
      ast.id ? this.stringify(ast.id) : null,
      body,
    ]
      .filter((v) => v !== null)
      .join(' ');
  }
}