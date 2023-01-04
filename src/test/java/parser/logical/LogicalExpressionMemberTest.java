package parser.logical;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import parser.LogicalExpression;

class LogicalExpressionMemberTest {

    private static PrintingExpressionLogger log = new PrintingExpressionLogger();

    private static class AlwaysTrueMemberFactory implements LogicalExpressionMember.LogicalExpressionMemberFactory {
        @Override
        public LogicalExpressionMember createLogicalExpressionMember(String expression, ExpressionLogger log) {
            return new LogicalExpressionMember() {
                @Override
                public String getHelp() {
                    return "fake help";
                }

                @Override
                public boolean evaluate() {
                    log.log("fake exposition");
                    return true;
                }

                @Override
                public boolean isLogicalExpressionMember(String futureExpression) {
                    return false;
                }
            };
        }
    }

    private static class HogwartsMember implements LogicalExpressionMember {

        private final String expression;
        private final ExpressionLogger log;

        HogwartsMember(String expression, ExpressionLogger log) {
            this.expression = expression;
            this.log = log;
        }

        @Override
        public String getHelp() {
            return "Only Harry and Ron are true functions Tom is Voldemort!";
        }

        @Override
        public boolean evaluate() {
            log.log("evaluating hogwarts members");
            if (expression.trim().toLowerCase().equals("tom()")) {
                return false;
            } else if (expression.trim().toLowerCase().equals("ron()")) {
                return true;
            } else if (expression.trim().toLowerCase().equals("harry()")) {
                return true;
            } else if (expression.trim().toLowerCase().equals("true")) {
                return true;
            } else if (expression.trim().toLowerCase().equals("false")) {
                return false;
            }else {
                throw new RuntimeException("unknown member of hogwarts - " + expression);
            }
        }

        @Override
        public boolean isLogicalExpressionMember(String futureExpression) {
            return (futureExpression.toLowerCase().contains("harry()")
                    || futureExpression.toLowerCase().contains("tom()")
                    || futureExpression.toLowerCase().contains("ron()"));
        }
    }

    private static class HogwartsMemberFactory implements LogicalExpressionMember.LogicalExpressionMemberFactory {
        @Override
        public LogicalExpressionMember createLogicalExpressionMember(String expression, ExpressionLogger log) {
            return new HogwartsMember(expression, log);
        }
    }

    @Test
    void replacableLogicalExpressionMember() {
        //correct
        Assertions.assertTrue(new LogicalExpressionParser("0<1 and -1<2", log).evaluate());
        Assertions.assertFalse(new LogicalExpressionParser("0>1 and -1<2", log).evaluate());
        String h1 = new LogicalExpressionParser("true and true", log).getHelp();
        //custom
        Assertions.assertTrue(new LogicalExpressionParser("0>1 and -1<2", log, new AlwaysTrueMemberFactory()).evaluate());
        String h2 = new LogicalExpressionParser("0>1 and -1<2", log, new AlwaysTrueMemberFactory()).getHelp();
        //not affected
        Assertions.assertTrue(new LogicalExpressionParser("true and true", log).evaluate());
        Assertions.assertFalse(new LogicalExpressionParser("false and true", log).evaluate());
        String h3 = new LogicalExpressionParser("true and true", log).getHelp();
        Assertions.assertEquals(h1, h2); // jsut the logical part
        Assertions.assertEquals(h3, h2);
    }

    @Test
    void replacableLogicalExpressionMemberBrackets() {
        //correct
        Assertions.assertEquals("true", new LogicalExpression("[ 0<1 and -1<2 ] or [ 0<1 and -1<2 ] ", log).solve());
        Assertions.assertEquals("false", new LogicalExpression("[ 0>1 and -1<2] or [0>1 and -1<2]", log).solve());
        String h1 = new LogicalExpression("help", log).solve();
        //custom
        Assertions.assertEquals("true", new LogicalExpression("[ 0>1 and -1<2] or [0>1 and -1<2]",
                log, new AlwaysTrueMemberFactory()).solve());
        String h2 = new LogicalExpression("help",
                log, new AlwaysTrueMemberFactory()).solve();
        //not affected
        Assertions.assertEquals("true", new LogicalExpression("[ 0<1 and -1<2 ] or [ 0<1 and -1<2 ] ", log).solve());
        Assertions.assertEquals("false", new LogicalExpression("[ 0>1 and -1<2] or [0>1 and -1<2]", log).solve());
        String h3 = new LogicalExpression("help", log).solve();
        Assertions.assertEquals(h3, h1);
        Assertions.assertNotEquals(h3, h2); //combined with factory
    }

    @Test
    void replacableLogicalExpressionMemberWithException() {
        //correct
        Assertions.assertTrue(new LogicalExpressionParser("0<1 and -1<2", log).evaluate());
        Assertions.assertFalse(new LogicalExpressionParser("0>1 and -1<2", log).evaluate());
        String h1 = new LogicalExpressionParser("true and true", log).getHelp();
        //custom
        Exception ex = null;
        try {
            Assertions.assertTrue(new LogicalExpressionParser("0>1 and -1<2", log, new HogwartsMemberFactory()).evaluate());
        } catch (Exception ex1) {
            ex = ex1;
        }
        Assertions.assertTrue(new LogicalExpressionParser("harry() and ron()", log, new HogwartsMemberFactory()).evaluate());
        Assertions.assertFalse(new LogicalExpressionParser("tom() and ron()", log, new HogwartsMemberFactory()).evaluate());
        Assertions.assertNotNull(ex);
        String h2 = new LogicalExpressionParser("0>1 and -1<2", log, new HogwartsMemberFactory()).getHelp();
        //not affected
        Assertions.assertTrue(new LogicalExpressionParser("true and true", log).evaluate());
        Assertions.assertFalse(new LogicalExpressionParser("false and true", log).evaluate());
        String h3 = new LogicalExpressionParser("true and true", log).getHelp();
        Assertions.assertEquals(h1, h2); // jsut the logical part
        Assertions.assertEquals(h3, h2);
    }

    @Test
    void replacableLogicalExpressionMemberBracketsWithException() {
        //correct
        Assertions.assertEquals("true", new LogicalExpression("[ 0<1 and -1<2 ] or [ 0<1 and -1<2 ] ", log).solve());
        Assertions.assertEquals("false", new LogicalExpression("[ 0>1 and -1<2] or [0>1 and -1<2]", log).solve());
        String h1 = new LogicalExpression("help", log).solve();
        //custom
        Exception ex = null;
        try {
            Assertions.assertEquals("true", new LogicalExpression("[ 0>1 and -1<2] or [0>1 and -1<2]",
                    log, new HogwartsMemberFactory()).solve());
        } catch (Exception ex1) {
            ex = ex1;
        }
        Assertions.assertNotNull(ex);
        String h2 = new LogicalExpression("help",
                log, new HogwartsMemberFactory()).solve();
        Assertions.assertEquals("true", new LogicalExpression("[ harry() and ron()] or [ron() and tom()]",
                log, new HogwartsMemberFactory()).solve());
        //not affected
        Assertions.assertEquals("true", new LogicalExpression("[ 0<1 and -1<2 ] or [ 0<1 and -1<2 ] ", log).solve());
        Assertions.assertEquals("false", new LogicalExpression("[ 0>1 and -1<2] or [0>1 and -1<2]", log).solve());
        String h3 = new LogicalExpression("help", log).solve();
        Assertions.assertEquals(h3, h1);
        Assertions.assertNotEquals(h3, h2); //combined with factory
    }
}