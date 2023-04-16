import { AST } from 'ocdot-parser'
import { isEmptyOrBlank } from './exts';



export class OCDotToDOTConverter extends AST.Compiler {
    ocDot: AST.OcDot;

    constructor(
        ocDot: AST.OcDot
    ) {
        super({ indentSize: 0 });
        this.ocDot = ocDot;
    }

    compileDot() : string {
        return this.stringify(this.ocDot)
    }

    protected printSubgraph(ast: AST.Subgraph): string {
        if (ast.specialType == null) { 
            return super.printSubgraph(ast);
        } else {
            return ""
        }
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