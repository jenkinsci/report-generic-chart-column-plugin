package hudson.plugins.report.genericchart.math;


import java.util.Arrays;

public class LogicalExpressionParser extends AbstractSplittinParser {

    private static final String[] chars = new String[]{"|", "&"};

    public LogicalExpressionParser(String expression) {
        super(expression);
    }


    public String[] getPrimaryChars() {
        return Arrays.copyOf(chars, chars.length);
    }

    public String[] getSecondaryChars() {
        return new String[0];
    }

    public boolean evaluate() {
        boolean result = new ComparingExpressionParser(split.get(0)).evaluate();
        for (int i = 1; i <= split.size() - 2; i = i + 2) {
            String op = split.get(i);
            ComparingExpressionParser comp2 = new ComparingExpressionParser(split.get(i + 1));
            boolean r2 = comp2.evaluate();
            if ("&".equals(op)) {
                result = result && r2;
            } else if ("|".equals(op)) {
                result = result || r2;
            } else {
                throw new ArithmeticException("invalid operator " + op);
            }
        }
        return result;
    }
}
