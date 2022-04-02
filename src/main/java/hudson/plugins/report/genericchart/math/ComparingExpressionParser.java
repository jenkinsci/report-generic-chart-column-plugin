package hudson.plugins.report.genericchart.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class ComparingExpressionParser extends AbstractSplittinParser {

    private static final String[] primaryChars = new String[]{"!=", "==", ">=", "<="};
    private static final String[] secondaryChars = new String[]{"<", ">"};

    public ComparingExpressionParser(String expression) {
        super(expression);

    }


    public String[] getPrimaryChars() {
        return Arrays.copyOf(primaryChars, primaryChars.length);
    }

    public String[] getSecondaryChars() {
        return Arrays.copyOf(secondaryChars, secondaryChars.length);
    }

    public boolean evaluate() {
        if (split.size() == 1) {
            return Boolean.parseBoolean(split.get(0).trim());
        } else if (split.size() == 3) {
            BigDecimal result1 = new AlgebraExpressionParser(split.get(0)).evaluate();
            BigDecimal result2 = new AlgebraExpressionParser(split.get(2)).evaluate();
            String op = split.get(1);
            if (">".equals(op)) {
                return result1.compareTo(result2) > 0;
            } else if ("<".equals(op)) {
                return result1.compareTo(result2) < 0;
            } else if (">=".equals(op)) {
                return result1.compareTo(result2) >= 0;
            } else if ("<=".equals(op)) {
                return result1.compareTo(result2) <= 0;
            } else if ("!=".equals(op)) {
                return result1.compareTo(result2) != 0;
            } else if ("==".equals(op)) {
                return result1.compareTo(result2) == 0;
            } else {
                throw new ArithmeticException("unknow comparsion operator" + op);
            }
        } else {
            throw new ArithmeticException("The comparsion operator needs to be operand between operators or true/false. Is " + getOriginal());
        }
    }


}
