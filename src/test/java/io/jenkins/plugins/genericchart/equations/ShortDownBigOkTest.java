package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for SHORT_DOWN_BIG_OK preset equation.
 * 
 * Equation: threshold=param1; (geom(..L1)/(L0/100)-100) < -threshold
 * 
 * This equation checks whether the rise (in %) against previous runs 
 * was not bigger than the threshold. It uses the geometric mean of all 
 * previous points (..L1) compared to the current value (L0).
 * 
 * Parameters:
 * - threshold: The maximum allowed rise percentage (as a positive number)
 * 
 * Returns true if: (geom(..L1)/(L0/100)-100) < -threshold
 */
class ShortDownBigOkTest {

    private static final String EQUATION_ID = "SHORT_DOWN_BIG_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testWithTwoPoints() throws IOException, URISyntaxException {
        // L0=100, L1=90, geom(L1)=90
        // (90/(100/100)-100) = -10, which is < -5
        List<String> data = createDataList(100, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when geometric mean is below negative threshold");
    }

    @Test
    void testWithThreePointsDecreasing() throws IOException, URISyntaxException {
        // L0=100, L1=90, L2=80, geom(90,80) ≈ 84.85
        // (84.85/(100/100)-100) ≈ -15.15, which is < -5
        List<String> data = createDataList(100, 90, 80);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true with decreasing sequence");
    }

    @Test
    void testGeometricMeanSensitivity() throws IOException, URISyntaxException {
        // Geometric mean is more sensitive to lower values
        // L0=100, previous: 95, 95, 95, 50
        // geom ≈ 81.23 (pulled down significantly by the 50)
        // (81.23/(100/100)-100) ≈ -18.77, which is < -5
        List<String> data = createDataList(100, 95, 95, 95, 50);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Geometric mean should be sensitive to low outliers");
    }

    @Test
    void testWithConstantValues() throws IOException, URISyntaxException {
        // L0=100, previous: 90, 90, 90
        // geom(90,90,90) = 90
        // (90/(100/100)-100) = -10, which is < -5
        List<String> data = createDataList(100, 90, 90, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle constant values correctly");
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        // L0=100, previous: 95, 95
        // geom(95,95) = 95
        // (95/(100/100)-100) = -5, which is NOT < -5
        List<String> data = createDataList(100, 95, 95);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when geometric mean equals negative threshold exactly");
    }

    @Test
    void testNoChange() throws IOException, URISyntaxException {
        // L0=100, previous: 100, 100, 100
        // geom(100,100,100) = 100
        // (100/(100/100)-100) = 0, which is NOT < -5
        List<String> data = createConstantDataList(100, 4);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when all values are the same");
    }

    @Test
    void testIncreasingSequence() throws IOException, URISyntaxException {
        // L0=100, previous: 105, 110, 115
        // geom(105,110,115) ≈ 109.93
        // (109.93/(100/100)-100) ≈ 9.93, which is NOT < -5
        List<String> data = createIncreasingDataList(100, 5, 4);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false with increasing sequence");
    }

    @Test
    void testMixedValuesHighVariance() throws IOException, URISyntaxException {
        // L0=100, previous: 110, 50
        // geom(110,50) ≈ 74.16
        // (74.16/(100/100)-100) ≈ -25.84, which is < -5
        List<String> data = createDataList(100, 110, 50);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle high variance correctly");
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        // L0=100, previous: 99.5, 99
        // geom(99.5,99) ≈ 99.25
        // (99.25/(100/100)-100) ≈ -0.75, which is < -0.5
        List<String> data = createDataList(100, 99.5, 99);
        assertTrue(evaluateEquation(EQUATION_ID, "0.5", data), 
            "Should work with small threshold");
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        // L0=100, previous: 99.99, 99.98
        // geom(99.99,99.98) ≈ 99.985
        // (99.985/(100/100)-100) ≈ -0.015, which is < 0
        List<String> data = createDataList(100, 99.99, 99.98);
        assertTrue(evaluateEquation(EQUATION_ID, "0", data), 
            "Should return true for any negative geometric mean with zero threshold");
    }

    @Test
    void testHighThreshold() throws IOException, URISyntaxException {
        // L0=100, previous: 90, 85, 80
        // geom(90,85,80) ≈ 84.93
        // (84.93/(100/100)-100) ≈ -15.07, which is NOT < -50
        List<String> data = createDataList(100, 90, 85, 80);
        assertFalse(evaluateEquation(EQUATION_ID, "50", data), 
            "Should return false when threshold is very high");
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        // L0=50.5, previous: 45.5, 44.5
        // geom(45.5,44.5) ≈ 44.99
        // (44.99/(50.5/100)-100) ≈ -10.91, which is < -5
        List<String> data = createDataList(50.5, 45.5, 44.5);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle decimal values correctly");
    }

    @Test
    void testLargeDataSet() throws IOException, URISyntaxException {
        // L0=100, previous 10 points all at 80
        List<String> data = createConstantDataList(100, 1);
        for (int i = 0; i < 10; i++) {
            data.add("80");
        }
        // geom of 10 values of 80 = 80
        // (80/(100/100)-100) = -20, which is < -5
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle large data sets correctly");
    }

    @Test
    void testGeometricVsArithmeticMean() throws IOException, URISyntaxException {
        // L0=100, previous: 120, 75
        // arithmetic mean = 97.5, geometric mean ≈ 94.87
        // geom(120,75) ≈ 94.87
        // (94.87/(100/100)-100) ≈ -5.13, which is < -5
        List<String> data = createDataList(100, 120, 75);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Geometric mean should be lower than arithmetic mean for varied values");
    }

    @Test
    void testExtremeOutlier() throws IOException, URISyntaxException {
        // L0=100, previous: 90, 90, 90, 10
        // geom ≈ 57.69 (heavily influenced by the 10)
        // (57.69/(100/100)-100) ≈ -42.31, which is < -5
        List<String> data = createDataList(100, 90, 90, 90, 10);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should be heavily influenced by extreme low outlier");
    }

    @Test
    void testGradualDecrease() throws IOException, URISyntaxException {
        // L0=100, previous: 98, 96, 94, 92, 90
        // geom ≈ 93.94
        // (93.94/(100/100)-100) ≈ -6.06, which is < -5
        List<String> data = createDataList(100, 98, 96, 94, 92, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle gradual decrease correctly");
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 85);
        EvaluationResult result = solveWithLogs(EQUATION_ID, "5", data);
        
        assertTrue(result.getResultAsBool(), "Should return true");
        assertFalse(result.getLog().isEmpty(), "Should produce log output");
    }

    @Test
    void testLargeValues() throws IOException, URISyntaxException {
        // L0=10000, previous: 9500, 9400, 9300
        // geom ≈ 9399.33
        // (9399.33/(10000/100)-100) ≈ -6.01, which is < -5
        List<String> data = createDataList(10000, 9500, 9400, 9300);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle large values correctly");
    }

    @Test
    void testSmallValues() throws IOException, URISyntaxException {
        // L0=1, previous: 0.9, 0.85
        // geom(0.9,0.85) ≈ 0.874
        // (0.874/(1/100)-100) ≈ -12.6, which is < -5
        List<String> data = createDataList(1, 0.9, 0.85);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle small values correctly");
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        // L0=100, previous: 94.99, 94.99
        // geom(94.99,94.99) = 94.99
        // (94.99/(100/100)-100) = -5.01, which is < -5
        List<String> data = createDataList(100, 94.99, 94.99);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when barely below negative threshold");
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        // L0=100, previous: 95.01, 95.01
        // geom(95.01,95.01) = 95.01
        // (95.01/(100/100)-100) = -4.99, which is NOT < -5
        List<String> data = createDataList(100, 95.01, 95.01);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when barely above negative threshold");
    }
}

// Made with Bob
