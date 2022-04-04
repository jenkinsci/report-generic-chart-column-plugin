package hudson.plugins.report.genericchart.math;


import java.util.Arrays;

public class LogicalExpressionParser extends AbstractSplittinParser {

    private static final String[] chars = new String[]{"|", "&"};

    public LogicalExpressionParser(String expression, ExpressionLogger log) {
        super(expression, log);
    }


    public String[] getPrimaryChars() {
        return Arrays.copyOf(chars, chars.length);
    }

    public String[] getSecondaryChars() {
        return new String[0];
    }

    public boolean evaluate() {
        log.log("evaluating: " + getOriginal());
        boolean result = new ComparingExpressionParser(split.get(0), new ExpressionLogger.InheritingExpressionLogger(log)).evaluate();
        for (int i = 1; i <= split.size() - 2; i = i + 2) {
            String op = split.get(i);
            ComparingExpressionParser comp2 = new ComparingExpressionParser(split.get(i + 1),new ExpressionLogger.InheritingExpressionLogger(log));
            boolean r2 = comp2.evaluate();
            log.log("... " + result + " " + op + " " + r2);
            if ("&".equals(op)) {
                result = result && r2;
            } else if ("|".equals(op)) {
                result = result || r2;
            } else {
                throw new ArithmeticException("invalid operator " + op);
            }
        }
        log.log("is: " + result);
        return result;
    }
}
