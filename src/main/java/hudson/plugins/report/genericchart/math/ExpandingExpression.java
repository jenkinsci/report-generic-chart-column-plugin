package hudson.plugins.report.genericchart.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import interfaces.Solvable;
import math.Main;
import parser.logical.ExpressionLogger;
import parser.methods.Declarations;

public class ExpandingExpression implements Solvable {

    private final String originalExpression;
    private final List<String> points;
    private final ExpressionLogger mainLogger;
    public static final String VALUES_PNG = "VALUES_PNG";
    public static final String VALUES_IPNG = "VALUES_IPNG";

    public static final ExpressionLogger verboseStderrLogger = new ExpressionLogger() {
        @Override
        public void log(String s) {
//FIXME comment back once integrated to ParserNG
//            if (Main.isVerbose()) {
                System.err.println(s);
//            }
        }
    };

    public ExpandingExpression(String s, List<String> points, ExpressionLogger logger) {
        this.originalExpression = s;
        this.mainLogger = logger;
        this.points = points;
    }

    public static void main(String args[]) {
        String in = Main.joinArgs(Arrays.asList(args), true);
        verboseStderrLogger.log(in);
        if (in.trim().equalsIgnoreCase(Declarations.HELP)) {
            System.out.println(getHelp());
        }
        String values_png = System.getenv(VALUES_PNG);
        String values_ipng = System.getenv(VALUES_IPNG);
        if (values_png != null && values_ipng != null) {
            throw new RuntimeException("Both " + VALUES_PNG + " and " + VALUES_IPNG + " are declared. That is no go");
        }
        List<String> values;
        if (values_png != null) {
            values = new ArrayList<>(Arrays.asList(values_png.split("\\s+")));
            Collections.reverse(values);
        } else if (values_ipng != null) {
            values = new ArrayList<>(Arrays.asList(values_ipng.split("\\s+")));
        } else {
            throw new RuntimeException("None of " + VALUES_PNG + " or " + VALUES_IPNG + " declared. Try help");
        }
        System.out.println(new ExpandingExpression(in, values, verboseStderrLogger).solve());

    }//end method

    @Override
    public String solve() {
        if (originalExpression.trim().equalsIgnoreCase(Declarations.HELP)) {
            return getHelp();
        }
        ExpandingExpressionParser eep = new ExpandingExpressionParser(originalExpression, points, mainLogger);
        mainLogger.log(eep.getExpanded());
        String r = eep.solve();
        return r;
    }


    public static String getHelp() {
        return "This is abstraction which allows to set with slices, rows and subset of immutable known numbers." + "\n" +
                "Instead of numbers, you can use literalls L0, L1...L99, which you can then call by:" + "\n" +
                "Ln - vlaue of Nth number" + "\n" +
                "L2..L4 - will expand to values of L2,L3,L4 - order is hnoured" + "\n" +
                "L2.. - will expand to values of L2,L3,..Ln-1,Ln" + "\n" +
                "..L5.. - will expand to values of  L0,L1...L4,L5" + "\n" +
                "When used as standalone, " + VALUES_PNG + " xor " + VALUES_IPNG + "  are used to pass in the space separated numbers (the I is inverted order)" + "\n" +
                "Assume " + VALUES_PNG + "='5 9 3 8', then it is the same as " + VALUES_IPNG + "='8 3 9 5'; BUt be aware, with I the L.. and ..L are a bit oposite then expected" + "\n" +
                "L0 then expand to 8; L2.. expands to 9,3,8; ' ..L2 expands to 5,9 " + "\n" +
                "L2..L4 expands to 9,5; L4..L2 expands to 5,9" + "\n" +
                "This parser by default uses LogicalExpression interpreter, but should work directly in" + "\n" +
                "In verbose mode, the expanded expression is always printed";

    }
}
