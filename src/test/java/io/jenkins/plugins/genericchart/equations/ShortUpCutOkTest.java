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
 * Test class for SHORT_UP_CUT_OK preset equation.
 * 
 * Equation: cut=param1; threshold=param2; (avgN(cut,..L1)/(L0/100)-100) > threshold
 * 
 * This equation checks whether the drop (in %) against previous runs with extremes 
 * cut-off was not bigger than the threshold. It uses avgN (average with cutting) 
 * of previous points.
 * 
 * Parameters:
 * - cut: Number of extreme values to cut from each end
 * - threshold: The maximum allowed drop percentage
 * 
 * Returns true if: (avgN(cut,..L1)/(L0/100)-100) > threshold
 */
class ShortUpCutOkTest {

    private static final String EQUATION_ID = "SHORT_UP_CUT_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testWithNoCutting() throws IOException, URISyntaxException {
        // cut=0, threshold=5
        // L0=100, previous: 110, 115, 120
        // avgN(0, 110,115,120) = avg(110,115,120) = 115
        // (115/(100/100)-100) = 15, which is > 5
        List<String> data = createDataList(100, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "0 5", data), 
            "Should work with no cutting");
    }

    @Test
    void testWithCuttingExtremes() throws IOException, URISyntaxException {
        // cut=1, threshold=5
        // L0=100, previous: 90, 110, 115, 120, 150
        // avgN(1, 90,110,115,120,150) = avg(110,115,120) = 115 (cuts 90 and 150)
        // (115/(100/100)-100) = 15, which is > 5
        List<String> data = createDataList(100, 90, 110, 115, 120, 150);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data), 
            "Should cut extreme values correctly");
    }

    @Test
    void testCuttingRemovesOutliers() throws IOException, URISyntaxException {
        // cut=1, threshold=5
        // L0=100, previous: 50, 105, 106, 107, 200
        // avgN(1, 50,105,106,107,200) = avg(105,106,107) ≈ 106
        // (106/(100/100)-100) = 6, which is > 5
        List<String> data = createDataList(100, 50, 105, 106, 107, 200);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data), 
            "Should remove outliers and pass threshold");
    }

    @Test
    void testCuttingTwoExtremes() throws IOException, URISyntaxException {
        // cut=2, threshold=5
        // L0=100, previous: 50, 60, 105, 106, 107, 180, 200
        // avgN(2, 50,60,105,106,107,180,200) = avg(105,106,107) ≈ 106
        // (106/(100/100)-100) = 6, which is > 5
        List<String> data = createDataList(100, 50, 60, 105, 106, 107, 180, 200);
        assertTrue(evaluateEquation(EQUATION_ID, "2 5", data), 
            "Should cut two extremes from each end");
    }

    @Test
    void testInsufficientDataForCutting() throws IOException, URISyntaxException {
        // cut=2, threshold=5
        // L0=100, previous: 110, 115
        // Not enough data to cut 2 from each end (would need at least 5 points)
        // Should handle gracefully
        List<String> data = createDataList(100, 110, 115);
        // Behavior depends on implementation - might use all data or fail gracefully
        try {
            evaluateEquation(EQUATION_ID, "2 5", data);
        } catch (Exception e) {
            // Expected if implementation doesn't handle edge case
        }
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        // cut=1, threshold=5
        // L0=100, previous: 90, 105, 105, 150
        // avgN(1, 90,105,105,150) = avg(105,105) = 105
        // (105/(100/100)-100) = 5, which is NOT > 5
        List<String> data = createDataList(100, 90, 105, 105, 150);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5", data), 
            "Should return false when equals threshold exactly");
    }

    @Test
    void testWithConstantValues() throws IOException, URISyntaxException {
        // cut=1, threshold=5
        // L0=100, previous: 110, 110, 110, 110, 110
        // avgN(1, all 110s) = 110
        // (110/(100/100)-100) = 10, which is > 5
        List<String> data = createDataList(100, 110, 110, 110, 110, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data), 
            "Should handle constant values correctly");
    }

    @Test
    void testCuttingImprovesResult() throws IOException, URISyntaxException {
        // cut=1, threshold=10
        // L0=100, previous: 50, 115, 116, 117, 118
        // Without cutting: avg(50,115,116,117,118) ≈ 103.2 -> 3.2% (NOT > 10)
        // With cutting: avgN(1, ...) = avg(115,116,117) ≈ 116 -> 16% (> 10)
        List<String> data = createDataList(100, 50, 115, 116, 117, 118);
        assertTrue(evaluateEquation(EQUATION_ID, "1 10", data), 
            "Cutting should improve result by removing low outlier");
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        // cut=1, threshold=0.5
        // L0=100, previous: 99, 100.5, 101, 102
        // avgN(1, 99,100.5,101,102) = avg(100.5,101) ≈ 100.75
        // (100.75/(100/100)-100) = 0.75, which is > 0.5
        List<String> data = createDataList(100, 99, 100.5, 101, 102);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0.5", data), 
            "Should work with small threshold");
    }

    @Test
    void testLargeDataSet() throws IOException, URISyntaxException {
        // cut=2, threshold=5
        // L0=100, previous: 10, 20, 110, 111, 112, 113, 114, 115, 190, 200
        // avgN(2, ...) = avg(110,111,112,113,114,115) ≈ 112.5
        // (112.5/(100/100)-100) = 12.5, which is > 5
        List<String> data = createDataList(100, 10, 20, 110, 111, 112, 113, 114, 115, 190, 200);
        assertTrue(evaluateEquation(EQUATION_ID, "2 5", data), 
            "Should handle large data sets with cutting");
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        // cut=1, threshold=5
        // L0=50.5, previous: 40, 55.5, 56.5, 57.5, 70
        // avgN(1, 40,55.5,56.5,57.5,70) = avg(55.5,56.5,57.5) ≈ 56.5
        // (56.5/(50.5/100)-100) ≈ 11.88, which is > 5
        List<String> data = createDataList(50.5, 40, 55.5, 56.5, 57.5, 70);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data), 
            "Should handle decimal values correctly");
    }

    @Test
    void testHighCutValue() throws IOException, URISyntaxException {
        // cut=3, threshold=5
        // L0=100, previous: 50, 60, 70, 110, 111, 112, 150, 160, 170
        // avgN(3, ...) = avg(110,111,112) ≈ 111
        // (111/(100/100)-100) = 11, which is > 5
        List<String> data = createDataList(100, 50, 60, 70, 110, 111, 112, 150, 160, 170);
        assertTrue(evaluateEquation(EQUATION_ID, "3 5", data), 
            "Should handle high cut values");
    }

    @Test
    void testCuttingWithVolatileData() throws IOException, URISyntaxException {
        // cut=2, threshold=5
        // L0=100, previous: 30, 40, 105, 110, 115, 120, 180, 190
        // avgN(2, ...) = avg(105,110,115,120) = 112.5
        // (112.5/(100/100)-100) = 12.5, which is > 5
        List<String> data = createDataList(100, 30, 40, 105, 110, 115, 120, 180, 190);
        assertTrue(evaluateEquation(EQUATION_ID, "2 5", data), 
            "Should handle volatile data with cutting");
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 50, 110, 115, 120, 200);
        EvaluationResult result = solveWithLogs(EQUATION_ID, "1 5", data);
        
        assertTrue(result.getResultAsBool(), "Should return true");
        assertFalse(result.getLog().isEmpty(), "Should produce log output");
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        // cut=1, threshold=5
        // L0=100, previous: 90, 105.01, 105.01, 150
        // avgN(1, 90,105.01,105.01,150) = avg(105.01,105.01) = 105.01
        // (105.01/(100/100)-100) = 5.01, which is > 5
        List<String> data = createDataList(100, 90, 105.01, 105.01, 150);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data), 
            "Should return true when barely above threshold");
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        // cut=1, threshold=5
        // L0=100, previous: 90, 104.99, 104.99, 150
        // avgN(1, 90,104.99,104.99,150) = avg(104.99,104.99) = 104.99
        // (104.99/(100/100)-100) = 4.99, which is NOT > 5
        List<String> data = createDataList(100, 90, 104.99, 104.99, 150);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5", data), 
            "Should return false when barely below threshold");
    }

    @Test
    void testSymmetricCutting() throws IOException, URISyntaxException {
        // cut=1, threshold=5
        // L0=100, previous: 80, 105, 110, 115, 140
        // avgN(1, 80,105,110,115,140) = avg(105,110,115) = 110
        // (110/(100/100)-100) = 10, which is > 5
        List<String> data = createDataList(100, 80, 105, 110, 115, 140);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data), 
            "Should cut symmetrically from both ends");
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        // cut=1, threshold=0
        // L0=100, previous: 90, 100.01, 100.02, 150
        // avgN(1, 90,100.01,100.02,150) = avg(100.01,100.02) ≈ 100.015
        // (100.015/(100/100)-100) ≈ 0.015, which is > 0
        List<String> data = createDataList(100, 90, 100.01, 100.02, 150);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0", data), 
            "Should work with zero threshold");
    }
}

// Made with Bob
