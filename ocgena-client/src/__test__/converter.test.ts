import { AST } from 'ocdot-parser'
import { OCDotToDOTConverter } from '../converter'
import * as shelljs from 'shelljs'
import * as s from '../exts'
import * as fs from 'fs'

describe("convertion from OCDot to plain dot", () => {
    test("convertion", () => {
        const myOcDotRaw = `
            ocnet { 
                places { 
                    p1 p2
                }

                transitions { 
                    t1
                }
                
                object types {
                     type1 type2
                }

                inputs {
                     p1 
                }

                outputs { 
                    p2
                }

                places for type1 {
                    p1
                }

                places for type2 {
                    p2
                }

                p1 10=> t1 1-> p2 -> p3 [color="orange"];

                subgraph s1 { 
                    subgraph ss1 { 

                    }
                } 3-> t1 2-> { p1 p2 }
                
                initial marking { 
                    p1=2
                    p2=3
                }
            }
        `
        const myOcDot = s.trimIndent(myOcDotRaw)
        const result = AST.parse(myOcDot, { rule: AST.Types.OcDot })

        const converter = new OCDotToDOTConverter(result);

        const plainDot = converter.compileDot();

        fs.writeFileSync("./test_artefacts/converter.test.txt", myOcDot + "\n" + plainDot);
        console.log(plainDot);
    })
})