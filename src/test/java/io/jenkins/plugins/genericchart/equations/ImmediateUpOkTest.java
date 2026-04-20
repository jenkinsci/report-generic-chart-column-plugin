package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for IMMEDIATE_UP_OK preset equation.
 * 
 * Equation: threshold=param1; (L1/(L0/100)-100) > threshold
 * 
 * This equation checks whether the drop (in %) against the previous run 
 * was not bigger than the threshold. It compares the current value (L0) 
 * with the previous value (L1).
 * 
 * Parameters:
 * - threshold: The maximum allowed drop percentage
 * 
 * Returns true if: (L1/(L0/100)-100) > threshold
 * Which means: the percentage change from L0 to L1 is greater than the threshold
 */
class ImmediateUpOkTest {

    private static final String EQUATION_ID = "IMMEDIATE_UP_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        // L0=100, L1=105, threshold=5
        // (105/(100/100)-100) = 5, which is NOT > 5
        List<String> data = createDataList(100, 105);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when change equals threshold exactly");
    }

    @Test
    void testAboveThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=106, threshold=5
        // (106/(100/100)-100) = 6, which is > 5
        List<String> data = createDataList(100, 106);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when change is above threshold");
    }

    @Test
    void testBelowThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=104, threshold=5
        // (104/(100/100)-100) = 4, which is NOT > 5
        List<String> data = createDataList(100, 104);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when change is below threshold");
    }

    @Test
    void testNoChange() throws IOException, URISyntaxException {
        // L0=100, L1=100, threshold=5
        // (100/(100/100)-100) = 0, which is NOT > 5
        List<String> data = createDataList(100, 100);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when there is no change");
    }

    @Test
    void testNegativeChange() throws IOException, URISyntaxException {
        // L0=100, L1=95, threshold=5
        // (95/(100/100)-100) = -5, which is NOT > 5
        List<String> data = createDataList(100, 95);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when value decreases");
    }

    @Test
    void testLargeIncrease() throws IOException, URISyntaxException {
        // L0=100, L1=150, threshold=5
        // (150/(100/100)-100) = 50, which is > 5
        List<String> data = createDataList(100, 150);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true for large increase");
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=101, threshold=0.5
        // (101/(100/100)-100) = 1, which is > 0.5
        List<String> data = createDataList(100, 101);
        assertTrue(evaluateEquation(EQUATION_ID, "0.5", data), 
            "Should return true with small threshold");
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=100.1, threshold=0
        // (100.1/(100/100)-100) = 0.1, which is > 0
        List<String> data = createDataList(100, 100.1);
        assertTrue(evaluateEquation(EQUATION_ID, "0", data), 
            "Should return true for any increase with zero threshold");
    }

    @Test
    void testNegativeThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=95, threshold=-10
        // (95/(100/100)-100) = -5, which is > -10
        List<String> data = createDataList(100, 95);
        assertTrue(evaluateEquation(EQUATION_ID, "-10", data), 
            "Should handle negative threshold correctly");
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        // L0=50.5, L1=55.5, threshold=5
        // (55.5/(50.5/100)-100) ≈ 9.9, which is > 5
        List<String> data = createDataList(50.5, 55.5);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle decimal values correctly");
    }

    @Test
    void testWithSmallValues() throws IOException, URISyntaxException {
        // L0=1, L1=1.1, threshold=5
        // (1.1/(1/100)-100) = 10, which is > 5
        List<String> data = createDataList(1, 1.1);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle small values correctly");
    }

    @Test
    void testWithLargeValues() throws IOException, URISyntaxException {
        // L0=10000, L1=10500, threshold=5
        // (10500/(10000/100)-100) = 5, which is NOT > 5
        List<String> data = createDataList(10000, 10500);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle large values correctly");
    }

    @Test
    void testMultipleDataPoints() throws IOException, URISyntaxException {
        // Only L0 and L1 matter for IMMEDIATE_UP_OK
        // L0=100, L1=110, threshold=5
        List<String> data = createDataList(100, 110, 120, 130);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should only consider L0 and L1 even with more data points");
    }

    @Test
    void testDoubleThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=120, threshold=10
        // (120/(100/100)-100) = 20, which is > 10
        List<String> data = createDataList(100, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "10", data), 
            "Should return true when change is double the threshold");
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=105.01, threshold=5
        // (105.01/(100/100)-100) = 5.01, which is > 5
        List<String> data = createDataList(100, 105.01);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when barely above threshold");
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=104.99, threshold=5
        // (104.99/(100/100)-100) = 4.99, which is NOT > 5
        List<String> data = createDataList(100, 104.99);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when barely below threshold");
    }

    @Test
    void testIncreasingSequence() throws IOException, URISyntaxException {
        // Testing with an increasing sequence
        // L0=100, L1=110, threshold=5
        List<String> data = createIncreasingDataList(100, 10, 5);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should work correctly with increasing sequence");
    }

    @Test
    void testDecreasingSequence() throws IOException, URISyntaxException {
        // Testing with a decreasing sequence
        // L0=100, L1=90, threshold=5
        List<String> data = createDecreasingDataList(100, 10, 5);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false with decreasing sequence");
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        // Test that evaluation produces log output
        List<String> data = createDataList(100, 110);
        EvaluationResult result = evaluateWithLog(EQUATION_ID, "5", data);
        
        assertTrue(result.getResult(), "Should return true");
        assertFalse(result.getLog().isEmpty(), "Should produce log output");
    }

    @Test
    void testHighThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=110, threshold=50
        // (110/(100/100)-100) = 10, which is NOT > 50
        List<String> data = createDataList(100, 110);
        assertFalse(evaluateEquation(EQUATION_ID, "50", data), 
            "Should return false when threshold is very high");
    }

    @Test
    void testPercentageCalculation() throws IOException, URISyntaxException {
        // Verify the percentage calculation is correct
        // L0=200, L1=220, threshold=5
        // (220/(200/100)-100) = 10, which is > 5
        List<String> data = createDataList(200, 220);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should calculate percentage correctly for different base values");
    }
}

// Made with Bob
