import { AST } from '../ast'

describe('parse', () => {

    test('try parsing', () => { 
        const myOcDot = `
            ocnet { 
                places { 
                    p1 p2
                    p3 p4
                }

                transitions { 
                    t1
                    t2
                }
                
                p1 10=> t1 1-> p2 [  ];

                subgraph s1 { 
                    subgraph ss1 { 

                    }
                } 3-> t1 2-> { p1 p2 }

                p3 3-> t2 ((2+k)*1 + k)=> p4 
            }
        `
        const result = AST.parse(myOcDot, {rule: AST.Types.OcDot})
        console.log(JSON.stringify(result))
        console.log(AST.stringify(result))
        expect(result).toBeDefined();
    })
});