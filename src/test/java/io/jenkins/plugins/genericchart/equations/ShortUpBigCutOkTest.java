package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for SHORT_UP_BIG_CUT_OK preset equation.
 * 
 * Equation: cut=param1; threshold=param2; (geomN(cut,..L1)/(L0/100)-100) > threshold
 * 
 * Parameters:
 * - cut: Number of extreme values to cut from each end
 * - threshold: The maximum allowed drop percentage
 */
class ShortUpBigCutOkTest {

    private static final String EQUATION_ID = "SHORT_UP_BIG_CUT_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testWithNoCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "0 5", data));
    }

    @Test
    void testWithCuttingExtremes() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 110, 115, 120, 150);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testGeometricMeanWithCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 50, 105, 106, 107, 200);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testCuttingTwoExtremes() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 50, 60, 105, 106, 107, 180, 200);
        assertTrue(evaluateEquation(EQUATION_ID, "2 5", data));
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 105, 105, 150);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testWithConstantValues() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110, 110, 110, 110, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testCuttingImprovesGeometricMean() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 10, 115, 116, 117, 118);
        assertTrue(evaluateEquation(EQUATION_ID, "1 10", data));
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 99, 100.5, 101, 102);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0.5", data));
    }

    @Test
    void testLargeDataSet() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 10, 20, 110, 111, 112, 113, 114, 115, 190, 200);
        assertTrue(evaluateEquation(EQUATION_ID, "2 5", data));
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        List<String> data = createDataList(50.5, 40, 55.5, 56.5, 57.5, 70);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testHighCutValue() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 50, 60, 70, 110, 111, 112, 150, 160, 170);
        assertTrue(evaluateEquation(EQUATION_ID, "3 5", data));
    }

    @Test
    void testGeometricSensitivityWithCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 5, 110, 110, 110, 200);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 50, 110, 115, 120, 200);
        EvaluationResult result = solveWithLogs(EQUATION_ID, "1 5", data);
        assertTrue(result.getResultAsBool());
        assertFalse(result.getLog().isEmpty());
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 105.01, 105.01, 150);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 104.99, 104.99, 150);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testSymmetricCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 80, 105, 110, 115, 140);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 100.01, 100.02, 150);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0", data));
    }
}

// Made with Bob
