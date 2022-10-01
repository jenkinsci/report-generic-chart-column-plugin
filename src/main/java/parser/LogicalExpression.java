package parser;

import interfaces.Solvable;
import math.Main;
import parser.logical.ComparingExpressionParser;
import parser.logical.ExpressionLogger;
import parser.logical.LogicalExpressionParser;
import parser.methods.Declarations;

import java.util.Arrays;

public class LogicalExpression implements Solvable {

    private final String originalExpression;
    private final ExpressionLogger mainLogger;

    public static final ExpressionLogger verboseStderrLogger = new ExpressionLogger() {
        @Override
        public void log(String s) {
            if (Main.isVerbose()) {
                System.err.println(s);
            }
        }
    };

    public LogicalExpression(String s, ExpressionLogger logger) {
        this.originalExpression = s;
        this.mainLogger = logger;
    }

    public static void main(String args[]) {
        String in  = Main.joinArgs(Arrays.asList(args), true);
        if (Main.isVerbose()) {
            System.err.println(in);
        }
        System.out.println(new LogicalExpression(in, verboseStderrLogger).solve());

    }//end method

    @Override
    public String solve() {
        if (originalExpression.trim().equalsIgnoreCase(Declarations.HELP)) {
            return getHelp();
        }
        return evalBrackets(originalExpression, mainLogger);
    }

    private String evalBrackets(String ex, ExpressionLogger logger) {
        logger.log("brackets: " + ex);
        for (int x = 0; x < ex.length(); x++) {
            if (ex.charAt(x) == '[') {
                int c = 1;
                for (int y = x + 1; y < ex.length(); y++) {
                    if (ex.charAt(y) == '[') {
                        c++;
                    }
                    if (ex.charAt(y) == ']') {
                        c--;
                        if (c == 0) {
                            String s = ex.substring(x + 1, y);
                            String eval = null;
                            if (s.contains("[")) {
                                eval = evalBrackets(s, new ExpressionLogger.InheritingExpressionLogger(logger));
                            } else {
                                eval = evalDirect(s, new ExpressionLogger.InheritingExpressionLogger(logger));
                            }
                            String s1 = ex.substring(0, x);
                            String s2 = ex.substring(y + 1);
                            ex = s1 + " " + eval + " " + s2;
                            logger.log("to: " + ex);
                        }
                    }
                }
            }
        }
        return evalDirect(ex, new ExpressionLogger.InheritingExpressionLogger(logger));
    }

    private String evalDirect(String s, ExpressionLogger logger) {
        LogicalExpressionParser lex = new LogicalExpressionParser(s, new ExpressionLogger.InheritingExpressionLogger(logger));
        return "" + lex.evaluate();
    }


    public static String getHelp() {
        return new ComparingExpressionParser(" 1 == 1", ExpressionLogger.DEV_NULL).getHelp()+"\n"+
               new LogicalExpressionParser(" 1 == 1", ExpressionLogger.DEV_NULL).getHelp() + "\n" +
                "As Mathematical parts are using () as brackets, Logical parts must be grouped by [] eg: " + "\n" +
                "1+1 < (2+0)*1 impl [ [5 == 6 || 33<(22-20)*2 ]xor [ [  5-3 < 2 or 7*(5+2)<=5 ] and 1+1 == 2]] eq [ true && false ]" + "\n" +
                "Note, that logical parsser supports only dual operators, so 1<2<3  or true|false|true are invalid!" + "\n" +
                "Thus:  [1<2]<3   or   [[true|false]<true] must be used. " + "\n" +
                "Single letter can logical operands can be used in row. So eg | have same meaning as ||. But also unluckily also eg < is same as <<" + "\n";

    }
}
