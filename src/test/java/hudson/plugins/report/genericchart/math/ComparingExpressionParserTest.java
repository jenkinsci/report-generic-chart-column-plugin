package hudson.plugins.report.genericchart.math;

import org.junit.jupiter.api.Assertions;

import java.util.List;

class ComparingExpressionParserTest {


    @org.junit.jupiter.api.Test
    void splitTest1() {
        List<String> s;
        ComparingExpressionParser comp;
        s = new ComparingExpressionParser("not important now").split("1+2+3 < 5");
        Assertions.assertEquals(3, s.size());
        Assertions.assertEquals("1+2+3", s.get(0));
        Assertions.assertEquals("<", s.get(1));
        Assertions.assertEquals("5", s.get(2));
        s = new ComparingExpressionParser("not important now").split("1+2+3 > 5");
        Assertions.assertEquals(3, s.size());
        Assertions.assertEquals("1+2+3", s.get(0));
        Assertions.assertEquals(">", s.get(1));
        Assertions.assertEquals("5", s.get(2));
        s = new ComparingExpressionParser("not important now").split("1+2+3 == 5");
        Assertions.assertEquals(3, s.size());
        Assertions.assertEquals("1+2+3", s.get(0));
        Assertions.assertEquals("==", s.get(1));
        Assertions.assertEquals("5", s.get(2));
        s = new ComparingExpressionParser("not important now").split("1+2+3 != 5");
        Assertions.assertEquals(3, s.size());
        Assertions.assertEquals("1+2+3", s.get(0));
        Assertions.assertEquals("!=", s.get(1));
        Assertions.assertEquals("5", s.get(2));
        s = new ComparingExpressionParser("not important now").split("1+2+3 >= 5");
        Assertions.assertEquals(3, s.size());
        Assertions.assertEquals("1+2+3", s.get(0));
        Assertions.assertEquals(">=", s.get(1));
        Assertions.assertEquals("5", s.get(2));
        s = new ComparingExpressionParser("not important now").split("1+2+3 <= 5");
        Assertions.assertEquals(3, s.size());
        Assertions.assertEquals("1+2+3", s.get(0));
        Assertions.assertEquals("<=", s.get(1));
        Assertions.assertEquals("5", s.get(2));

    }

    @org.junit.jupiter.api.Test
    void evalTest() {
        ComparingExpressionParser comp;
        comp = new ComparingExpressionParser("1+2+3 < 2*2");
        Assertions.assertFalse(comp.evaluate());
        comp = new ComparingExpressionParser("1+2+3 > 2*2");
        Assertions.assertTrue(comp.evaluate());
        comp = new ComparingExpressionParser("1+2 <= 3*2");
        Assertions.assertTrue(comp.evaluate());
        comp = new ComparingExpressionParser("1+2+3 <= 3*2");
        Assertions.assertTrue(comp.evaluate());
        comp = new ComparingExpressionParser("30 <= 3*2");
        Assertions.assertFalse(comp.evaluate());
        comp = new ComparingExpressionParser("1+2+3 == 3*2");
        Assertions.assertTrue(comp.evaluate());
        comp = new ComparingExpressionParser("1+2+3 == 3*2+1");
        Assertions.assertFalse(comp.evaluate());
        comp = new ComparingExpressionParser("1+2+3 != 3*2+1");
        Assertions.assertTrue(comp.evaluate());
        comp = new ComparingExpressionParser("10 >= 6");
        Assertions.assertTrue(comp.evaluate());
        comp = new ComparingExpressionParser("1+2+3 >= 6");
        Assertions.assertTrue(comp.evaluate());
        comp = new ComparingExpressionParser("1+2+3 >= 7");
        Assertions.assertFalse(comp.evaluate());

        comp = new ComparingExpressionParser("  true");
        Assertions.assertTrue(comp.evaluate());
        comp = new ComparingExpressionParser("false  ");
        Assertions.assertFalse(comp.evaluate());
    }

}