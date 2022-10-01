package parser.logical;

import java.math.BigDecimal;
import java.util.Arrays;

public class ComparingExpressionParser extends AbstractSplittingParser {

    private static final String[] primaryChars = new String[]{"!=", "==", ">=", "<=", "neq", "le", "ge", "lt", "gt"};
    private static final String[] secondaryChars = new String[]{"<", ">", "eq"};

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
            boolean r = Boolean.parseBoolean(split.get(0).trim());
            log.log("is: " + r);
            return r;
        } else if (split.size() == 3) {
            BigDecimal result1 = new AlgebraExpressionParser(split.get(0), new ExpressionLogger.InheritingExpressionLogger(log)).evaluate();
            BigDecimal result2 = new AlgebraExpressionParser(split.get(2), new ExpressionLogger.InheritingExpressionLogger(log)).evaluate();
            String op = split.get(1);
            log.log("... " + result1.toString() + " " + op + " " + result2.toString());
            if (">".equals(op) || "gt".equals(op)) {
                boolean r = result1.compareTo(result2) > 0;
                log.log("is: " + r);
                return r;
            } else if ("<".equals(op) || "lt".equals(op)) {
                boolean r = result1.compareTo(result2) < 0;
                log.log("is: " + r);
                return r;
            } else if (">=".equals(op) || "ge".equals(op)) {
                boolean r = result1.compareTo(result2) >= 0;
                log.log("is: " + r);
                return r;
            } else if ("<=".equals(op) || "le".equals(op)) {
                boolean r = result1.compareTo(result2) <= 0;
                log.log("is: " + r);
                return r;
            } else if ("!=".equals(op) || "neq".equals(op)) {
                boolean r = result1.compareTo(result2) != 0;
                log.log("is: " + r);
                return r;
            } else if ("==".equals(op) || "eq".equals(op)) {
                boolean r = result1.compareTo(result2) == 0;
                log.log("is: " + r);
                return r;
            } else {
                throw new ArithmeticException("unknown comparison operator" + op);
            }
        } else {
            throw new ArithmeticException("The comparison operator needs to be operand between operators or true/false. Is " + getOriginal());
        }
    }

    @Override
    public String getName() {
        return "Comparing operators";
    }
}
