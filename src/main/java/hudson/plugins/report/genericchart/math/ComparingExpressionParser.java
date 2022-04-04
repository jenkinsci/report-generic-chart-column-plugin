package hudson.plugins.report.genericchart.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class ComparingExpressionParser extends AbstractSplittinParser {

    private static final String[] primaryChars = new String[]{"!=", "==", ">=", "<="};
    private static final String[] secondaryChars = new String[]{"<", ">"};

    public ComparingExpressionParser(String expression, ExpressionLogger log) {
        super(expression, log);

    }


    public String[] getPrimaryChars() {
        return Arrays.copyOf(primaryChars, primaryChars.length);
    }

    public String[] getSecondaryChars() {
        return Arrays.copyOf(secondaryChars, secondaryChars.length);
    }

    public boolean evaluate() {
        log.log("evaluating: " + getOriginal());
        if (split.size() == 1) {
            boolean r =  Boolean.parseBoolean(split.get(0).trim());
            log.log("is: " + r);
            return r;
        } else if (split.size() == 3) {
            BigDecimal result1 = new AlgebraExpressionParser(split.get(0), new ExpressionLogger.InheritingExpressionLogger(log)).evaluate();
            BigDecimal result2 = new AlgebraExpressionParser(split.get(2), new ExpressionLogger.InheritingExpressionLogger(log)).evaluate();
            String op = split.get(1);
            log.log("... " + result1.toString() + " " + op + " " + result2.toString());
            if (">".equals(op)) {
                boolean r =   result1.compareTo(result2) > 0;
                log.log("is: " + r);
                return r;
            } else if ("<".equals(op)) {
                boolean r =   result1.compareTo(result2) < 0;
                log.log("is: " + r);
                return r;
            } else if (">=".equals(op)) {
                boolean r =   result1.compareTo(result2) >= 0;
                log.log("is: " + r);
                return r;
            } else if ("<=".equals(op)) {
                boolean r =   result1.compareTo(result2) <= 0;
                log.log("is: " + r);
                return r;
            } else if ("!=".equals(op)) {
                boolean r =   result1.compareTo(result2) != 0;
                log.log("is: " + r);
                return r;
            } else if ("==".equals(op)) {
                boolean r =   result1.compareTo(result2) == 0;
                log.log("is: " + r);
                return r;
            } else {
                throw new ArithmeticException("unknow comparsion operator" + op);
            }
        } else {
            throw new ArithmeticException("The comparsion operator needs to be operand between operators or true/false. Is " + getOriginal());
        }
    }


}
