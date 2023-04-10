import { AST } from '../ast'

describe('parse', () => {
    test('try parsing', () => { 
        const myOcDot = `
            ocnet { 
                p1
                
                p1 => t1 -> p2
            }
        `
        const result = AST.parse(myOcDot, {rule: AST.Types.OcDot})
        
        console.log(AST.stringify(result))
        expect(result).toBeDefined();
    })
});