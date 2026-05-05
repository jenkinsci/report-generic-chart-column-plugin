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
 * Test class for SHORT_UP_BIG_OK preset equation.
 * 
 * Equation: threshold=param1; (geom(..L1)/(L0/100)-100) > threshold
 * 
 * This equation checks whether the drop (in %) against previous runs 
 * was not bigger than the threshold. It uses the geometric mean of all 
 * previous points (..L1) compared to the current value (L0).
 * 
 * Geometric mean is more sensitive to outliers and extreme values than arithmetic mean.
 * 
 * Parameters:
 * - threshold: The maximum allowed drop percentage
 * 
 * Returns true if: (geom(..L1)/(L0/100)-100) > threshold
 */
class ShortUpBigOkTest {

    private static final String EQUATION_ID = "SHORT_UP_BIG_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testWithTwoPoints() throws IOException, URISyntaxException {
        // L0=100, L1=110, geom(L1)=110
        // (110/(100/100)-100) = 10, which is > 5
        List<String> data = createDataList(100, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when geometric mean is above threshold");
    }

    @Test
    void testWithThreePointsIncreasing() throws IOException, URISyntaxException {
        // L0=100, L1=110, L2=120, geom(110,120) ≈ 114.89
        // (114.89/(100/100)-100) ≈ 14.89, which is > 5
        List<String> data = createDataList(100, 110, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true with increasing sequence");
    }

    @Test
    void testGeometricMeanSensitivity() throws IOException, URISyntaxException {
        // Geometric mean is more sensitive to lower values
        // L0=100, previous: 105, 105, 105, 50
        // geom ≈ 88.88 (pulled down by the 50)
        // (88.88/(100/100)-100) ≈ -11.12, which is NOT > 5
        List<String> data = createDataList(100, 105, 105, 105, 50);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Geometric mean should be sensitive to low outliers");
    }

    @Test
    void testWithConstantValues() throws IOException, URISyntaxException {
        // L0=100, previous: 110, 110, 110
        // geom(110,110,110) = 110
        // (110/(100/100)-100) = 10, which is > 5
        List<String> data = createDataList(100, 110, 110, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle constant values correctly");
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        // L0=100, previous: 105, 105
        // geom(105,105) = 105
        // (105/(100/100)-100) = 5, which is NOT > 5
        List<String> data = createDataList(100, 105, 105);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when geometric mean equals threshold exactly");
    }

    @Test
    void testNoChange() throws IOException, URISyntaxException {
        // L0=100, previous: 100, 100, 100
        // geom(100,100,100) = 100
        // (100/(100/100)-100) = 0, which is NOT > 5
        List<String> data = createConstantDataList(100, 4);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when all values are the same");
    }

    @Test
    void testDecreasingSequence() throws IOException, URISyntaxException {
        // L0=100, previous: 95, 90, 85
        // geom(95,90,85) ≈ 89.93
        // (89.93/(100/100)-100) ≈ -10.07, which is NOT > 5
        List<String> data = createDecreasingDataList(100, 5, 4);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false with decreasing sequence");
    }

    @Test
    void testMixedValuesHighVariance() throws IOException, URISyntaxException {
        // L0=100, previous: 90, 150
        // geom(90,150) ≈ 116.19
        // (116.19/(100/100)-100) ≈ 16.19, which is > 5
        List<String> data = createDataList(100, 90, 150);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle high variance correctly");
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        // L0=100, previous: 100.5, 101
        // geom(100.5,101) ≈ 100.75
        // (100.75/(100/100)-100) ≈ 0.75, which is > 0.5
        List<String> data = createDataList(100, 100.5, 101);
        assertTrue(evaluateEquation(EQUATION_ID, "0.5", data), 
            "Should work with small threshold");
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        // L0=100, previous: 100.01, 100.02
        // geom(100.01,100.02) ≈ 100.015
        // (100.015/(100/100)-100) ≈ 0.015, which is > 0
        List<String> data = createDataList(100, 100.01, 100.02);
        assertTrue(evaluateEquation(EQUATION_ID, "0", data), 
            "Should return true for any positive geometric mean with zero threshold");
    }

    @Test
    void testHighThreshold() throws IOException, URISyntaxException {
        // L0=100, previous: 110, 115, 120
        // geom(110,115,120) ≈ 114.85
        // (114.85/(100/100)-100) ≈ 14.85, which is NOT > 50
        List<String> data = createDataList(100, 110, 115, 120);
        assertFalse(evaluateEquation(EQUATION_ID, "50", data), 
            "Should return false when threshold is very high");
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        // L0=50.5, previous: 55.5, 56.5
        // geom(55.5,56.5) ≈ 55.99
        // (55.99/(50.5/100)-100) ≈ 10.87, which is > 5
        List<String> data = createDataList(50.5, 55.5, 56.5);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle decimal values correctly");
    }

    @Test
    void testLargeDataSet() throws IOException, URISyntaxException {
        // L0=100, previous 10 points all at 120
        List<String> data = createConstantDataList(100, 1);
        for (int i = 0; i < 10; i++) {
            data.add("120");
        }
        // geom of 10 values of 120 = 120
        // (120/(100/100)-100) = 20, which is > 5
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle large data sets correctly");
    }

    @Test
    void testGeometricVsArithmeticMean() throws IOException, URISyntaxException {
        // Geometric mean is always <= arithmetic mean
        // L0=100, previous: 80, 125
        // arithmetic mean = 102.5, geometric mean ≈ 100
        // geom(80,125) = 100
        // (100/(100/100)-100) = 0, which is NOT > 5
        List<String> data = createDataList(100, 80, 125);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Geometric mean should be lower than arithmetic mean for varied values");
    }

    @Test
    void testExtremeOutlier() throws IOException, URISyntaxException {
        // L0=100, previous: 110, 110, 110, 10
        // geom ≈ 68.13 (heavily influenced by the 10)
        // (68.13/(100/100)-100) ≈ -31.87, which is NOT > 5
        List<String> data = createDataList(100, 110, 110, 110, 10);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should be heavily influenced by extreme low outlier");
    }

    @Test
    void testGradualIncrease() throws IOException, URISyntaxException {
        // L0=100, previous: 102, 104, 106, 108, 110
        // geom ≈ 105.94
        // (105.94/(100/100)-100) ≈ 5.94, which is > 5
        List<String> data = createDataList(100, 102, 104, 106, 108, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle gradual increase correctly");
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110, 115);
        EvaluationResult result = solveWithLogs(EQUATION_ID, "5", data);
        
        assertTrue(result.getResultAsBool(), "Should return true");
        assertFalse(result.getLog().isEmpty(), "Should produce log output");
    }

    @Test
    void testLargeValues() throws IOException, URISyntaxException {
        // L0=10000, previous: 10500, 10600, 10700
        // geom ≈ 10599.33
        // (10599.33/(10000/100)-100) ≈ 5.99, which is > 5
        List<String> data = createDataList(10000, 10500, 10600, 10700);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle large values correctly");
    }

    @Test
    void testSmallValues() throws IOException, URISyntaxException {
        // L0=1, previous: 1.1, 1.15
        // geom(1.1,1.15) ≈ 1.124
        // (1.124/(1/100)-100) ≈ 12.4, which is > 5
        List<String> data = createDataList(1, 1.1, 1.15);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle small values correctly");
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        // L0=100, previous: 105.01, 105.01
        // geom(105.01,105.01) = 105.01
        // (105.01/(100/100)-100) = 5.01, which is > 5
        List<String> data = createDataList(100, 105.01, 105.01);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when barely above threshold");
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        // L0=100, previous: 104.99, 104.99
        // geom(104.99,104.99) = 104.99
        // (104.99/(100/100)-100) = 4.99, which is NOT > 5
        List<String> data = createDataList(100, 104.99, 104.99);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when barely below threshold");
    }
}

// Made with Bob
