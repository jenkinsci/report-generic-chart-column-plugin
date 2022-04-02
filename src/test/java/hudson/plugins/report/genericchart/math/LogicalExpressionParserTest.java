package hudson.plugins.report.genericchart.math;

import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogicalExpressionParserTest {


    @org.junit.jupiter.api.Test
    void splitTest1() {
        List<String> s;
        s = new LogicalExpressionParser("not important now").split("1+2+3 < 5");
        Assertions.assertEquals(1, s.size());
        Assertions.assertEquals("1+2+3 < 5", s.get(0));
        s = new LogicalExpressionParser("not important now").split("1+2+3<5 & 2+5>7");
        Assertions.assertEquals(3, s.size());
        Assertions.assertEquals("1+2+3<5", s.get(0));
        Assertions.assertEquals("&", s.get(1));
        Assertions.assertEquals("2+5>7", s.get(2));
        s = new LogicalExpressionParser("not important now").split("1+2+3<5    &&&&2+5>7");
        Assertions.assertEquals(3, s.size());
        Assertions.assertEquals("1+2+3<5", s.get(0));
        Assertions.assertEquals("&", s.get(1));
        Assertions.assertEquals("2+5>7", s.get(2));
        s = new LogicalExpressionParser("not important now").split("1+2+3<5 | 2+5>7");
        Assertions.assertEquals(3, s.size());
        Assertions.assertEquals("1+2+3<5", s.get(0));
        Assertions.assertEquals("|", s.get(1));
        Assertions.assertEquals("2+5>7", s.get(2));
        s = new LogicalExpressionParser("not important now").split("1+2+3<5||||    2+5>7");
        Assertions.assertEquals(3, s.size());
        Assertions.assertEquals("1+2+3<5", s.get(0));
        Assertions.assertEquals("|", s.get(1));
        Assertions.assertEquals("2+5>7", s.get(2));
    }

    @org.junit.jupiter.api.Test
    void splitTest2() {
        List<String> s;
        s = new LogicalExpressionParser("not important now").split("1+2+3 < 5");
        Assertions.assertEquals(1, s.size());
        Assertions.assertEquals("1+2+3 < 5", s.get(0));
        s = new LogicalExpressionParser("not important now").split("1+2+3<5 & 2+5>7 | 5<6");
        Assertions.assertEquals(5, s.size());
        Assertions.assertEquals("1+2+3<5", s.get(0));
        Assertions.assertEquals("&", s.get(1));
        Assertions.assertEquals("2+5>7", s.get(2));
        Assertions.assertEquals("|", s.get(3));
        Assertions.assertEquals("5<6", s.get(4));
        s = new LogicalExpressionParser("not important now").split("1+2+3<5 & 2+5>7 & 5<6");
        Assertions.assertEquals(5, s.size());
        Assertions.assertEquals("1+2+3<5", s.get(0));
        Assertions.assertEquals("&", s.get(1));
        Assertions.assertEquals("2+5>7", s.get(2));
        Assertions.assertEquals("&", s.get(3));
        Assertions.assertEquals("5<6", s.get(4));
        s = new LogicalExpressionParser("not important now").split("1+2+3<5 | 2+5>7 & 5<6");
        Assertions.assertEquals(5, s.size());
        Assertions.assertEquals("1+2+3<5", s.get(0));
        Assertions.assertEquals("|", s.get(1));
        Assertions.assertEquals("2+5>7", s.get(2));
        Assertions.assertEquals("&", s.get(3));
        Assertions.assertEquals("5<6", s.get(4));
        s = new LogicalExpressionParser("not important now").split("1+2+3<5 | 2+5>7 | 5<6");
        Assertions.assertEquals(5, s.size());
        Assertions.assertEquals("1+2+3<5", s.get(0));
        Assertions.assertEquals("|", s.get(1));
        Assertions.assertEquals("2+5>7", s.get(2));
        Assertions.assertEquals("|", s.get(3));
        Assertions.assertEquals("5<6", s.get(4));
        s = new LogicalExpressionParser("not important now").split("1+2+3<5    &&&&2+5>7 & 5<7 & 1+2+3<5||||    2+5>7");
        Assertions.assertEquals(9, s.size());
        Assertions.assertEquals("1+2+3<5", s.get(0));
        Assertions.assertEquals("&", s.get(1));
        Assertions.assertEquals("2+5>7", s.get(2));
        Assertions.assertEquals("&", s.get(3));
        Assertions.assertEquals("5<7", s.get(4));
        Assertions.assertEquals("&", s.get(5));
        Assertions.assertEquals("1+2+3<5", s.get(6));
        Assertions.assertEquals("|", s.get(7));
        Assertions.assertEquals("2+5>7", s.get(8));
    }

    @org.junit.jupiter.api.Test
    void evalTest() {
        LogicalExpressionParser comp;
        comp = new LogicalExpressionParser("1+2+3 >= 7");
        Assertions.assertFalse(comp.evaluate());
        comp = new LogicalExpressionParser("1+2+3 >= 5");
        Assertions.assertTrue(comp.evaluate());
        comp = new LogicalExpressionParser("1+2+3 >= 7 | 1+2+3 >= 5");
        Assertions.assertTrue(comp.evaluate());
        comp = new LogicalExpressionParser("6 >= 7 & 6 >= 5");
        Assertions.assertFalse(comp.evaluate());
    }
}