package hudson.plugins.report.genericchart.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class AlgebraExpressionParserTest {

    @Test
    void evaluateTest() {
        Assertions.assertEquals(0, new AlgebraExpressionParser("0").evaluate().compareTo(new BigDecimal(0)));
        Assertions.assertEquals(0, new AlgebraExpressionParser("2+3").evaluate().compareTo(new BigDecimal(5)));
        Assertions.assertEquals(0, new AlgebraExpressionParser("2+3*4").evaluate().compareTo(new BigDecimal(14)));
        Assertions.assertEquals(0, new AlgebraExpressionParser("(2+3)*4").evaluate().compareTo(new BigDecimal(20)));
        Assertions.assertEquals(0, new AlgebraExpressionParser("cos(pi)").evaluate().compareTo(new BigDecimal(-1)));
        Assertions.assertEquals(0, new AlgebraExpressionParser("sum(1,2,3,4)").evaluate().compareTo(new BigDecimal(10)));
        Assertions.assertEquals(0, new AlgebraExpressionParser("med(0,5,4)").evaluate().compareTo(new BigDecimal(4)));
        Assertions.assertEquals(0, new AlgebraExpressionParser("avg(0,5,4)").evaluate().compareTo(new BigDecimal(3)));
    }
}