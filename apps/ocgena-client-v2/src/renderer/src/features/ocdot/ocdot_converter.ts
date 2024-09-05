import { AST } from 'ocdot-parser'
import { isEmptyOrBlank } from 'ocdot-parser/lib/exts';

interface EdgePair {
  from: AST.EdgeTarget
  to: AST.EdgeTarget
  edgeOp: '->' | '=>'
}

class ElementsSaver { 
  placesMap = new Map<string, AST.Node>()
  transitionsMap = new Map<string, AST.Node>()
  ocDot : AST.OcDot
  
  constructor(ocDot : AST.OcDot) {
    this.ocDot = ocDot
  }

  tryExtractPlacesAndTransitions() {
    this.placesMap.clear();
    this.transitionsMap.clear();
    let ocNet = this.ocDot.body.find((ocDotStatement: AST.OcDotStatement) => {
      return ocDotStatement.type == 'ocnet'
    })
    if (ocNet && ocNet.type == 'ocnet') {
      ocNet.body.forEach((clusterStatement) => {
        switch(clusterStatement.type) {
          case 'subgraph': {
            if (clusterStatement.specialType) {
              clusterStatement.body.forEach((node) => {
                if (node.type == 'node') {
                  let mapToSave = (clusterStatement.specialType === 'places' ? this.placesMap : this.transitionsMap)
                  mapToSave.set(node.id.value, node)
                }
              })
            }
            break;
          }
          default: {

          }
        }
      })
    }
  }
}

export class OCDotToDOTConverter extends AST.Compiler {
  ocDot: AST.OcDot;
  elementsSaver;

  public get placesMap(): Map<string, AST.Node> {
    return this.elementsSaver.placesMap;
  }
  
  public get transitionsMap(): Map<string, AST.Node> {
    return this.elementsSaver.transitionsMap;
  }

  constructor(
    ocDot: AST.OcDot
  ) {
    super({ indentSize: 0 });
    this.ocDot = ocDot;
    this.elementsSaver = new ElementsSaver(ocDot);
  }

  getNodeType(node : AST.Node) : 'place' | 'transition' | undefined {
    if (this.placesMap.has(node.id.value)) {
      return 'place'
    }
    if (this.transitionsMap.has(node.id.value)) {
      return 'transition'
    }
  }

  compileDot(): string {
    this.elementsSaver.tryExtractPlacesAndTransitions();

    return this.stringify(this.ocDot)
  }

  protected printSpecialSubgraph(ast: AST.Subgraph): string {
    return ast.body.filter((value) => value.type === AST.Types.Node, this)
      .map(this.stringify, this)
      .join('\n')
  }

  protected printSubgraph(ast: AST.Subgraph): string {
    if (ast.specialType == null) {
      return super.printSubgraph(ast);
    } else {
      const subgraphString = this.printSpecialSubgraph(ast);
      return subgraphString;
    }
  }

  protected findColorAttr(ast: AST.Edge): string | null {
    return ast.body.find((attr) => attr.key.value == 'color')?.value?.value || null;
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
    } else if (size == 1) { 
      return [
        <EdgePair>{
          from : ast.from,
          to: ast.targets[0].id,
          edgeOp: ast.targets[0].edgeop.type
        }
      ]
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

  protected override printEdge(ast: AST.Edge): string {
    const edgePairs = this.makeEdgePairs(ast);
    if (edgePairs.length === 0) {
      return ""
    }
    const colorFilteredAttrs = ast.body.filter((value) => {
      return value.key.value !== 'color'
    }, this)

    return edgePairs.map((edgePair) => {
      return this.printEdgePair(edgePair, ast, colorFilteredAttrs);
    })
      .join('\n') + "\n";
  }

  protected override printNode(ast: AST.Node): string {
    let nodeType = this.getNodeType(ast)
    let attr : string = " "
    if (nodeType == 'transition') {
      attr = "shape=box "
    }
    const value = ast.body.length == 0
      ? `${this.stringify(ast.id)} [ ${attr} ];`
      : `${this.stringify(ast.id)} [ ${attr} \n${this.withIndentIncrease(ast.body
        .map(this.printFilteredAttribute, this)
        .filter((str) => !isEmptyOrBlank(str))
        .join('\n'))}\n];`;

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
    if (ast.body.length === 0) return "digraph {}"

    var strings = []
    for(var i = 0; i < ast.body.length; i++) {
      var newString = this.stringify(ast.body[i])
      if (!isEmptyOrBlank(newString)) {
        strings.push(newString)
      }
    }
    var result = this.withIndentIncrease(strings.join('\n'))

    // const bodyValue = this.withIndentIncrease(ast.body.map(this.stringify, this).filter((v) => {
    //   return !isEmptyOrBlank(v)
    // }, this)
    //   .join('\n')
    // );

    const body = `digraph {\n${result}\n}`

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