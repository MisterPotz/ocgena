import { AST } from '../ast'

describe('parse', () => {

    test('try parsing', () => { 
        const myOcDot = `
            ocnet { 
                places { 
                    p1 p2
                }

                transitions { 
                    t1
                }
                
                p1 10=> t1 1-> p2 [  ];

                subgraph s1 { 
                    subgraph ss1 { 

                    }
                } 3-> t1 2-> { p1 p2 }
            }
        `
        const result = AST.parse(myOcDot, {rule: AST.Types.OcDot})
        console.log(JSON.stringify(result))
        console.log(AST.stringify(result))
        expect(result).toBeDefined();
    })
});