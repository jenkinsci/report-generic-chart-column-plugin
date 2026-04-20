package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for IMMEDIATE_DOWN_OK preset equation.
 * 
 * Equation: threshold=param1; (L1/(L0/100)-100) < -threshold
 * 
 * This equation checks whether the rise (in %) against the previous run 
 * was not bigger than the threshold. It compares the current value (L0) 
 * with the previous value (L1).
 * 
 * Parameters:
 * - threshold: The maximum allowed rise percentage (as a positive number)
 * 
 * Returns true if: (L1/(L0/100)-100) < -threshold
 * Which means: the percentage change from L0 to L1 is less than negative threshold (a decrease)
 */
class ImmediateDownOkTest {

    private static final String EQUATION_ID = "IMMEDIATE_DOWN_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        // L0=100, L1=95, threshold=5
        // (95/(100/100)-100) = -5, which is NOT < -5
        List<String> data = createDataList(100, 95);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when change equals negative threshold exactly");
    }

    @Test
    void testAboveThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=94, threshold=5
        // (94/(100/100)-100) = -6, which is < -5
        List<String> data = createDataList(100, 94);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when decrease is above threshold");
    }

    @Test
    void testBelowThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=96, threshold=5
        // (96/(100/100)-100) = -4, which is NOT < -5
        List<String> data = createDataList(100, 96);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when decrease is below threshold");
    }

    @Test
    void testNoChange() throws IOException, URISyntaxException {
        // L0=100, L1=100, threshold=5
        // (100/(100/100)-100) = 0, which is NOT < -5
        List<String> data = createDataList(100, 100);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when there is no change");
    }

    @Test
    void testPositiveChange() throws IOException, URISyntaxException {
        // L0=100, L1=105, threshold=5
        // (105/(100/100)-100) = 5, which is NOT < -5
        List<String> data = createDataList(100, 105);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when value increases");
    }

    @Test
    void testLargeDecrease() throws IOException, URISyntaxException {
        // L0=100, L1=50, threshold=5
        // (50/(100/100)-100) = -50, which is < -5
        List<String> data = createDataList(100, 50);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true for large decrease");
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=99, threshold=0.5
        // (99/(100/100)-100) = -1, which is < -0.5
        List<String> data = createDataList(100, 99);
        assertTrue(evaluateEquation(EQUATION_ID, "0.5", data), 
            "Should return true with small threshold");
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=99.9, threshold=0
        // (99.9/(100/100)-100) = -0.1, which is < 0
        List<String> data = createDataList(100, 99.9);
        assertTrue(evaluateEquation(EQUATION_ID, "0", data), 
            "Should return true for any decrease with zero threshold");
    }

    @Test
    void testNegativeThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=105, threshold=-10
        // (105/(100/100)-100) = 5, which is < -(-10) = 10
        List<String> data = createDataList(100, 105);
        assertTrue(evaluateEquation(EQUATION_ID, "-10", data), 
            "Should handle negative threshold correctly");
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        // L0=50.5, L1=45.5, threshold=5
        // (45.5/(50.5/100)-100) ≈ -9.9, which is < -5
        List<String> data = createDataList(50.5, 45.5);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle decimal values correctly");
    }

    @Test
    void testWithSmallValues() throws IOException, URISyntaxException {
        // L0=1, L1=0.9, threshold=5
        // (0.9/(1/100)-100) = -10, which is < -5
        List<String> data = createDataList(1, 0.9);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle small values correctly");
    }

    @Test
    void testWithLargeValues() throws IOException, URISyntaxException {
        // L0=10000, L1=9500, threshold=5
        // (9500/(10000/100)-100) = -5, which is NOT < -5
        List<String> data = createDataList(10000, 9500);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should handle large values correctly");
    }

    @Test
    void testMultipleDataPoints() throws IOException, URISyntaxException {
        // Only L0 and L1 matter for IMMEDIATE_DOWN_OK
        // L0=100, L1=90, threshold=5
        List<String> data = createDataList(100, 90, 80, 70);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should only consider L0 and L1 even with more data points");
    }

    @Test
    void testDoubleThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=80, threshold=10
        // (80/(100/100)-100) = -20, which is < -10
        List<String> data = createDataList(100, 80);
        assertTrue(evaluateEquation(EQUATION_ID, "10", data), 
            "Should return true when decrease is double the threshold");
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=94.99, threshold=5
        // (94.99/(100/100)-100) = -5.01, which is < -5
        List<String> data = createDataList(100, 94.99);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return true when barely above threshold");
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=95.01, threshold=5
        // (95.01/(100/100)-100) = -4.99, which is NOT < -5
        List<String> data = createDataList(100, 95.01);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false when barely below threshold");
    }

    @Test
    void testDecreasingSequence() throws IOException, URISyntaxException {
        // Testing with a decreasing sequence
        // L0=100, L1=90, threshold=5
        List<String> data = createDecreasingDataList(100, 10, 5);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should work correctly with decreasing sequence");
    }

    @Test
    void testIncreasingSequence() throws IOException, URISyntaxException {
        // Testing with an increasing sequence
        // L0=100, L1=110, threshold=5
        List<String> data = createIncreasingDataList(100, 10, 5);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data), 
            "Should return false with increasing sequence");
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        // Test that evaluation produces log output
        List<String> data = createDataList(100, 90);
        EvaluationResult result = evaluateWithLog(EQUATION_ID, "5", data);
        
        assertTrue(result.getResult(), "Should return true");
        assertFalse(result.getLog().isEmpty(), "Should produce log output");
    }

    @Test
    void testHighThreshold() throws IOException, URISyntaxException {
        // L0=100, L1=90, threshold=50
        // (90/(100/100)-100) = -10, which is NOT < -50
        List<String> data = createDataList(100, 90);
        assertFalse(evaluateEquation(EQUATION_ID, "50", data), 
            "Should return false when threshold is very high");
    }

    @Test
    void testPercentageCalculation() throws IOException, URISyntaxException {
        // Verify the percentage calculation is correct
        // L0=200, L1=180, threshold=5
        // (180/(200/100)-100) = -10, which is < -5
        List<String> data = createDataList(200, 180);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data), 
            "Should calculate percentage correctly for different base values");
    }

    @Test
    void testSlightDecrease() throws IOException, URISyntaxException {
        // L0=100, L1=99.5, threshold=1
        // (99.5/(100/100)-100) = -0.5, which is NOT < -1
        List<String> data = createDataList(100, 99.5);
        assertFalse(evaluateEquation(EQUATION_ID, "1", data), 
            "Should return false for slight decrease below threshold");
    }

    @Test
    void testModerateDecrease() throws IOException, URISyntaxException {
        // L0=100, L1=97, threshold=2
        // (97/(100/100)-100) = -3, which is < -2
        List<String> data = createDataList(100, 97);
        assertTrue(evaluateEquation(EQUATION_ID, "2", data), 
            "Should return true for moderate decrease above threshold");
    }
}

// Made with Bob
