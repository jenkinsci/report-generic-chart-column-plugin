package hudson.plugins.report.genericchart.math;

import parser.LogicalExpression;
import parser.MathExpression;
import parser.logical.ExpressionLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExpandingExpressionParser {

    private static final int MAX = 100;
    private static final Pattern downRange = Pattern.compile("\\.\\.L\\d+");
    private static final Pattern upRange = Pattern.compile("L\\d+\\.\\.");
    private static final Pattern bothRange = Pattern.compile("L\\d+\\.\\.L\\d+");
    private final String originalExpression;
    private final List<String> points;
    private final LogicalExpression logicalExpressionParser;
    private final ExpressionLogger log;
    private final String expanded;

    public ExpandingExpressionParser(String expression, List<String> points, ExpressionLogger log) {
        this.log = log;
        this.points = points;
        originalExpression = expression;
        log.log("Expression : " + originalExpression);
        List<String> naturalisedOrder = new ArrayList<>(points);
        Collections.reverse(naturalisedOrder);
        log.log("Upon       : " + naturalisedOrder.stream().collect(Collectors.joining(",")));
        log.log("As         : Ln...L1,L0");
        log.log("MN         = " + points.size());
        expanded = expandALL(expression);
        log.log("Expanded as: " + expanded);
        logicalExpressionParser = new LogicalExpression(expanded, new ExpressionLogger.InheritingExpressionLogger(log));
    }

    String expandALL(String expression) {
        //order metters!
        String expanded = expression;
        expanded = expandMN(expression);
        expanded = expandCurlyIndexes(expanded);
        expanded = expandLL(expanded);
        expanded = expandLd(expanded);
        expanded = expandLu(expanded);
        if (points.size() > 0) {
            //maybe better to throw and die?
            expanded = expandL(expanded);
        } else {
            log.log("Warning! no points in input!");
        }
        return expanded;
    }

    String expandCurlyIndexes(String expression) {
        return expression;
    }

    String expandMN(String expression) {
        return expression.replace("MN", ""+points.size());
    }

    String expandLL(String expression) {
        while (true) {
            Matcher m = bothRange.matcher(expression);
            boolean found = m.find();
            if (!found) {
                break;
            }
            String group = m.group();
            expression = expression.replace(group, createRange(group.replace("L", "").replaceAll("\\.\\..*", ""), group.replace("L", "").replaceAll(".*\\.\\.", "")));
        }
        return expression;
    }

    String expandLd(String expression) {
        while (true) {
            Matcher m = downRange.matcher(expression);
            boolean found = m.find();
            if (!found) {
                break;
            }
            String group = m.group();
            expression = expression.replace(group, createRange((points.size() - 1) + "", group.replace("..L", "")));
        }
        return expression;
    }

    String expandLu(String expression) {
        while (true) {
            Matcher m = upRange.matcher(expression);
            boolean found = m.find();
            if (!found) {
                break;
            }
            String group = m.group();
            expression = expression.replace(group, createRange(group.replace("L", "").replaceAll("\\.\\..*", ""), "0"));
        }
        return expression;
    }

    String expandL(String expression) {
        for (int x = MAX; x >= 0; x--) {
            expression = expression.replace("L" + x, points.get(limit(x)));
        }
        return expression;
    }

    private String createRange(String from, String to) {
        return createRange(Integer.parseInt(from), Integer.parseInt(to));
    }

    private String createRange(int from, int to) {
        from = limit(from);
        to = limit(to);
        int ffrom = Math.min(from, to);
        boolean revert = false;
        if (ffrom != from) {
            revert = true;
        }
        int tto = Math.max(from, to);
        if (tto >= points.size()) {
            tto = points.size() - 1;
        }
        List<String> l = new ArrayList(tto - ffrom + 5);
        for (int i = ffrom; i <= tto; i++) {
            l.add(points.get(i));
        }
        if (revert) {
            Collections.reverse(l);
        }
        return l.stream().collect(Collectors.joining(","));
    }

    private int limit(int i) {
        if (i < 0) {
            return 0;
        }
        if (i >= points.size()) {
            return points.size() - 1;
        }
        return i;
    }

    public String solve() {
        String rs = logicalExpressionParser.solve();
        log.log("is: " + rs);
        return rs;
    }

    public boolean evaluate() {
        Boolean r = Boolean.parseBoolean(solve());
        return r;
    }

    String getExpanded() {
        return expanded;
    }
}
