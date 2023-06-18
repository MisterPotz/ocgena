import { AST } from '../ast';

describe('correctly expanded into list', () => {
  test('infix stringification', () => {
    let expr = AST.parseExpression('((-5*k + 4) * 2 * 3 * (1 + 3))');

    const evaluation = '(-5*k+4)*2*3*(1+3)'

    let compiler = new AST.Compiler();
    let stringification = compiler.stringifyExpression(expr);

    expect(stringification).toBe(evaluation);
  });

  test('infix parser', () => {
    let expr = AST.parseExpression('((-5*k + 4) * 2 * 3 * (1 + 3))');

    const evaluation = {
        infix: ['(', '-', 5, '*', 'k', '+', 4, ')', '*', 2, '*', 3, '*', '(', 1, '+', 3, ')']
    }

    expect(expr).toMatchObject(evaluation)
});
});
