import { AST } from '../ast';

describe('correctly expanded into list', () => {
  test('parsing first case', () => {
    let expr = AST.parseExpression('((1 - 5) + 3*k - k * 7)');

    const evaluation: AST.MathExpression = {
      ops: [ '(', '-', ')', '+', '*', '-', '*'],
      values: [1, 5, 3, 'k', 'k', 7],
    };

    let compiler = new AST.Compiler();
    let stringification = compiler.stringifyExpression(expr)
    expect(expr).toMatchObject<AST.MathExpression>(evaluation);
  });

  test.skip('parsing second case', () => {
    let expr = AST.parseExpression('(4*k+1)');

    const evaluation: AST.MathExpression = {
      ops: ['*', '+'],
      values: [4, 'k', 1],
    };
    expect(expr).toMatchObject<AST.MathExpression>(evaluation);
  });
});
