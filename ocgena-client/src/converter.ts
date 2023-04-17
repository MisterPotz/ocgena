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

      for (let i = 1; i < size - 1; i++) {
        const pair: EdgePair = {
          from: ast.targets[i].id,
          to: ast.targets[i + 1].id,
          edgeOp: ast.targets[i + 1].edgeop.type
        }
        pairs.push(pair);
      }

    } else {
      return []
    }
  }

  protected printEdgePair(edgePair: EdgePair, originalEdge: AST.Edge): string {
    const foundColor = this.findColorAttr(originalEdge);
    const color = this.makeEdgeOpColorAttr(edgePair.edgeOp, foundColor)
    this.increaseIndent();
    const value = `${this.stringify(edgePair.from)
      } ${edgePair.edgeOp
      } ${this.stringify(edgePair.to)
      } [${color}; ${originalEdge.body
        .map(this.stringify, this)
        .join('; ')
      }];`

    this.decreaseIndent();
    return value;
  }

  protected override printEdge(ast: AST.Edge): string {
    const edgePairs = this.makeEdgePairs(ast);
    if (edgePairs.length === 0) {
      return ""
    }

    return edgePairs.map((edgePair) => {
      return this.printEdgePair(edgePair, ast);
    })
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