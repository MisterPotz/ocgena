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

                p1 10=> t1 1-> p2 [  ];

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
        const result = AST.parse(myOcDot, {rule: AST.Types.OcDot})
        console.log(JSON.stringify(result))
        console.log(AST.stringify(result))
        expect(result).toBeDefined();
    })
});