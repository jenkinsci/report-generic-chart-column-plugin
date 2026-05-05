package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for SHORT_UP_OK preset equation.
 * 
 * Equation: threshold=param1; (avg(..L1)/(L0/100)-100) > threshold
 * 
 * This equation checks whether the drop (in %) against previous runs 
 * was not bigger than the threshold. It uses the average of all previous 
 * points (..L1) compared to the current value (L0).
 * 
 * Parameters:
 * - threshold: The maximum allowed drop percentage
 * 
 * Returns true if: (avg(..L1)/(L0/100)-100) > threshold
 * Which means: the percentage change from L0 to the average of previous values is greater than threshold
 */
class ShortUpOkTest {

    private static final String EQUATION_ID = "SHORT_UP_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testWithTwoPoints() throws IOException, URISyntaxException {
        // L0=100, L1=110, avg(L1)=110, threshold=5
        // (110/(100/100)-100) = 10, which is > 5
        List<String> data = createDataList(100, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when average of previous points is above threshold");
    }

    @Test
    void testWithThreePointsIncreasing() throws IOException, URISyntaxException {
        // L0=100, L1=110, L2=120, avg(L1,L2)=115, threshold=5
        // (115/(100/100)-100) = 15, which is > 5
        List<String> data = createDataList(100, 110, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true with increasing sequence");
    }

    @Test
    void testWithMultiplePointsAverageAboveThreshold() throws IOException, URISyntaxException {
        // L0=100, previous points: 105, 110, 115, 120
        // avg(105,110,115,120) = 112.5
        // (112.5/(100/100)-100) = 12.5, which is > 5
        List<String> data = createDataList(100, 105, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when average is significantly above threshold");
    }

    @Test
    void testWithMultiplePointsAverageBelowThreshold() throws IOException, URISyntaxException {
        // L0=100, previous points: 101, 102, 103, 104
        // avg(101,102,103,104) = 102.5
        // (102.5/(100/100)-100) = 2.5, which is NOT > 5
        List<String> data = createDataList(100, 101, 102, 103, 104);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when average is below threshold");
    }

    @Test
    void testWithMixedValues() throws IOException, URISyntaxException {
        // L0=100, previous points: 90, 110, 95, 115
        // avg(90,110,95,115) = 102.5
        // (102.5/(100/100)-100) = 2.5, which is NOT > 5
        List<String> data = createDataList(100, 90, 110, 95, 115);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle mixed values correctly");
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        // L0=100, previous points: 105, 105
        // avg(105,105) = 105
        // (105/(100/100)-100) = 5, which is NOT > 5
        List<String> data = createDataList(100, 105, 105);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when average equals threshold exactly");
    }

    @Test
    void testNoChange() throws IOException, URISyntaxException {
        // L0=100, previous points: 100, 100, 100
        // avg(100,100,100) = 100
        // (100/(100/100)-100) = 0, which is NOT > 5
        List<String> data = createConstantDataList(100, 4);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when all values are the same");
    }

    @Test
    void testDecreasingSequence() throws IOException, URISyntaxException {
        // L0=100, previous points: 95, 90, 85
        // avg(95,90,85) = 90
        // (90/(100/100)-100) = -10, which is NOT > 5
        List<String> data = createDecreasingDataList(100, 5, 4);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false with decreasing sequence");
    }

    @Test
    void testLargeDataSet() throws IOException, URISyntaxException {
        // L0=100, previous 10 points averaging to 120
        List<String> data = createConstantDataList(100, 1);
        for (int i = 0; i < 10; i++) {
            data.add("120");
        }
        // avg of 10 values of 120 = 120
        // (120/(100/100)-100) = 20, which is > 5
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle large data sets correctly");
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        // L0=100, previous points: 100.5, 101
        // avg(100.5,101) = 100.75
        // (100.75/(100/100)-100) = 0.75, which is > 0.5
        List<String> data = createDataList(100, 100.5, 101);
        assertTrue(evaluateEquation(EQUATION_ID, "0.5", data), 
            "Should work with small threshold");
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        // L0=100, previous points: 100.01, 100.02
        // avg(100.01,100.02) = 100.015
        // (100.015/(100/100)-100) = 0.015, which is > 0
        List<String> data = createDataList(100, 100.01, 100.02);
        assertTrue(evaluateEquation(EQUATION_ID, "0", data), 
            "Should return true for any positive average with zero threshold");
    }

    @Test
    void testHighThreshold() throws IOException, URISyntaxException {
        // L0=100, previous points: 110, 115, 120
        // avg(110,115,120) = 115
        // (115/(100/100)-100) = 15, which is NOT > 50
        List<String> data = createDataList(100, 110, 115, 120);
        assertFalse(evaluateEquation(EQUATION_ID, "50", data), 
            "Should return false when threshold is very high");
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        // L0=50.5, previous points: 55.5, 56.5
        // avg(55.5,56.5) = 56
        // (56/(50.5/100)-100) ≈ 10.89, which is > 5
        List<String> data = createDataList(50.5, 55.5, 56.5);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle decimal values correctly");
    }

    @Test
    void testOneHighOnelow() throws IOException, URISyntaxException {
        // L0=100, previous points: 80, 130
        // avg(80,130) = 105
        // (105/(100/100)-100) = 5, which is NOT > 5
        List<String> data = createDataList(100, 80, 130);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should average high and low values correctly");
    }

    @Test
    void testManyPointsSlightlyAbove() throws IOException, URISyntaxException {
        // L0=100, previous 5 points all at 106
        // avg(106,106,106,106,106) = 106
        // (106/(100/100)-100) = 6, which is > 5
        List<String> data = createDataList(100, 106, 106, 106, 106, 106);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when all previous values are slightly above threshold");
    }

    @Test
    void testGradualIncrease() throws IOException, URISyntaxException {
        // L0=100, previous points: 102, 104, 106, 108, 110
        // avg(102,104,106,108,110) = 106
        // (106/(100/100)-100) = 6, which is > 5
        List<String> data = createDataList(100, 102, 104, 106, 108, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle gradual increase correctly");
    }

    @Test
    void testVolatileData() throws IOException, URISyntaxException {
        // L0=100, previous points: 90, 120, 85, 125, 95, 115
        // avg(90,120,85,125,95,115) = 105
        // (105/(100/100)-100) = 5, which is NOT > 5
        List<String> data = createDataList(100, 90, 120, 85, 125, 95, 115);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle volatile data correctly");
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        // Test that evaluation produces log output
        List<String> data = createDataList(100, 110, 115);
        EvaluationResult result = solveWithLogs(EQUATION_ID, "5", data);
        
        assertTrue(result.getResultAsBool(), "Should return true");
        assertFalse(result.getLog().isEmpty(), "Should produce log output");
    }

    @Test
    void testLargeValues() throws IOException, URISyntaxException {
        // L0=10000, previous points: 10500, 10600, 10700
        // avg(10500,10600,10700) = 10600
        // (10600/(10000/100)-100) = 6, which is > 5
        List<String> data = createDataList(10000, 10500, 10600, 10700);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle large values correctly");
    }

    @Test
    void testSmallValues() throws IOException, URISyntaxException {
        // L0=1, previous points: 1.1, 1.15
        // avg(1.1,1.15) = 1.125
        // (1.125/(1/100)-100) = 12.5, which is > 5
        List<String> data = createDataList(1, 1.1, 1.15);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle small values correctly");
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        // L0=100, previous points: 105.01, 105.01
        // avg(105.01,105.01) = 105.01
        // (105.01/(100/100)-100) = 5.01, which is > 5
        List<String> data = createDataList(100, 105.01, 105.01);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when barely above threshold");
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        // L0=100, previous points: 104.99, 104.99
        // avg(104.99,104.99) = 104.99
        // (104.99/(100/100)-100) = 4.99, which is NOT > 5
        List<String> data = createDataList(100, 104.99, 104.99);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when barely below threshold");
    }
}

// Made with Bob
