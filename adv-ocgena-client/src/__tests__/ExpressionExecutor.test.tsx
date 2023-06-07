import exp from "constants"
import { AST } from "ocdot-parser"
import { converter } from "ocgena"

describe("expression executor", () => {
    test("how it works", () => {
        // 3*k+1
        let rootExpressionString = `{"type":"expression","head":{"head":3,"tail":[{"op":"*","target":{"head":{"variable":"k"},"tail":[]}}]},"tail":[{"op":"+","target":{"head":1,"tail":[]}}],"location":{"start":{"offset":0,"line":1,"column":1},"end":{"offset":9,"line":1,"column":10}}}`
        let rootExpression = JSON.parse(rootExpressionString)
        let expressionExecutor = new converter.ExpressionExecutor(rootExpression)

        expect(expressionExecutor.variable).toBe("k")

        console.log(expressionExecutor.substitute(10))
        expect(expressionExecutor.substitute(10)).toBe(31)
    })

    test("with ast", () => {
        // 3*k+1
        let expr =  "(3 + k * 5)"
        let parsed = AST.parseExpression(expr)
        let expressionExecutor = new converter.ExpressionExecutor(parsed)

        expect(expressionExecutor.variable).toBe("k")

        console.log(expressionExecutor.substitute(10))
        expect(expressionExecutor.substitute(10)).toBe(53)
    })

    
    test("complex without variable", () => {
        // 3*k+1
        let expr =  "(140 - (3 + 5) * 5)"
        let parsed = AST.parseExpression(expr)
        let expressionExecutor = new converter.ExpressionExecutor(parsed)

        console.log(expressionExecutor.substitute(10))
        expect(expressionExecutor.substitute(10)).toBe(100)
    })

    test("complex with variable", () => {
        // 3*k+1
        let expr =  "(- (3 + 5) * 5 + 120 / k )"
        let parsed = AST.parseExpression(expr)
        let expressionExecutor = new converter.ExpressionExecutor(parsed)

        // console.log(expressionExecutor.substitute(3))
        // expect(expressionExecutor.substitute(3)).toBe(0)
    })
})