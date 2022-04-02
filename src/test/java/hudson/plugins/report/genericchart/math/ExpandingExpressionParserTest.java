package hudson.plugins.report.genericchart.math;

import org.junit.jupiter.api.Assertions;

import java.util.Arrays;

class ExpandingExpressionParserTest {

    @org.junit.jupiter.api.Test
    void expandLLsTest() {
        String s;
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLL("L1..L2");
        Assertions.assertEquals("2,3", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLL("L2..L1");
        Assertions.assertEquals("3,2", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLL("L0..L0");
        Assertions.assertEquals("1", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLL("L1..L125");
        Assertions.assertEquals("2,3", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLL("L123..L125");
        Assertions.assertEquals("3", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLL("L5 L0..L3  L1   L3..L0  L2 L2..L2 L1..L1");
        Assertions.assertEquals("L5 1,2,3  L1   3,2,1  L2 3 2", s);
    }


    @org.junit.jupiter.api.Test
    void expandLdTest() {
        String s;
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLd("..L5");
        Assertions.assertEquals("1,2,3", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLd("..L2");
        Assertions.assertEquals("1,2,3", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLd("..L1");
        Assertions.assertEquals("1,2", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLd("..L0");
        Assertions.assertEquals("1", s);
    }

    @org.junit.jupiter.api.Test
    void expandLuTest() {
        String s;
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLu("L5..");
        Assertions.assertEquals("3", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLu("L2..");
        Assertions.assertEquals("3", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLu("L1..");
        Assertions.assertEquals("2,3", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandLu("L0..");
        Assertions.assertEquals("1,2,3", s);
    }

    @org.junit.jupiter.api.Test
    void expandLTest() {
        String s;
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandL("L5");
        Assertions.assertEquals("3", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandL("L0");
        Assertions.assertEquals("1", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandL("L1");
        Assertions.assertEquals("2", s);
        s = new ExpandingExpressionParser("not important now", Arrays.asList("1", "2", "3")).expandL("L2");
        Assertions.assertEquals("3", s);
    }

    @org.junit.jupiter.api.Test
    void testEval() {
        ExpandingExpressionParser comp;
        comp = new ExpandingExpressionParser("max(L0..) == 3", Arrays.asList("1", "2", "3"));
        Assertions.assertTrue(comp.evaluate());
        comp = new ExpandingExpressionParser("min(L0..L3)<= 1", Arrays.asList("1", "2", "3"));
        Assertions.assertTrue(comp.evaluate());
        comp = new ExpandingExpressionParser("avg(..L1) <  L2/2+1", Arrays.asList("1", "2", "3"));
        Assertions.assertTrue(comp.evaluate());
        comp = new ExpandingExpressionParser("sum(..L1) ==  L2 | false ", Arrays.asList("1", "2", "3"));
        Assertions.assertTrue(comp.evaluate());
        comp = new ExpandingExpressionParser("sum(..L1) ==  L2 & false ", Arrays.asList("1", "2", "3"));
        Assertions.assertFalse(comp.evaluate());

    }

    @org.junit.jupiter.api.Test
    void testRealLive() {
        ExpandingExpressionParser comp;
        comp = new ExpandingExpressionParser("L1 < L0", Arrays.asList("545", "453", "628"));
        Assertions.assertTrue(comp.evaluate());
        comp = new ExpandingExpressionParser("avg(..L1) < L0", Arrays.asList("545", "453", "628, 5"));
        Assertions.assertTrue(comp.evaluate());
        comp = new ExpandingExpressionParser("max(..L1) < L0 || 500 < L0", Arrays.asList("545", "453", "628, 5"));
        Assertions.assertTrue(comp.evaluate());
        comp = new ExpandingExpressionParser("500 < L0 && 600 > L0", Arrays.asList("545", "453", "628, 5"));
        Assertions.assertTrue(comp.evaluate());
    }
}