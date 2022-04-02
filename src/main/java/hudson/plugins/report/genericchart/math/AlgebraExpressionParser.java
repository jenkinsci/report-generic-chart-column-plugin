package hudson.plugins.report.genericchart.math;

import parser.MathExpression;

import java.math.BigDecimal;

public class AlgebraExpressionParser {

    private final MathExpression mathExpression;
    private final String original;

    public AlgebraExpressionParser(String expr) {
        original = expr;
        mathExpression = new MathExpression(original);
    }

    public BigDecimal evaluate() {
        return new BigDecimal(mathExpression.solve());
    }
}
