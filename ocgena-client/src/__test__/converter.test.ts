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
                    p1 [ label= "place 1" ] p2
                }

                transitions { 
                    t1
                }

                p1 10=> t1 1-> p2 -> p3 [color="orange"];

                subgraph s1 { 
                    subgraph ss1 { 

                    }
                } 3-> t1 2-> { p1 p2 }
            }
        `
        const myOcDot = s.trimIndent(myOcDotRaw)
        const result = AST.parse(myOcDot, { rule: AST.Types.OcDot })

        const converter = new OCDotToDOTConverter(result);

        const plainDot = converter.compileDot();

        fs.writeFileSync("./test_artefacts/converter.test.txt", myOcDot + "\n" + plainDot);
        console.log(plainDot);
        var expected = fs.readFileSync("./test_artefacts/ocdot_dot_ex1.txt")
        
        expect(s.removeEmptyLineSpaces(plainDot.trim()))
            .toEqual(s.removeEmptyLineSpaces(expected.toString().trim()))
    })

    test('test string equality', () => {
        expect('biba').toEqual("biba")
    })
})