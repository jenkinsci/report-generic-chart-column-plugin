package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for LONG_UP_BIG_CUT_OK preset equation.
 * 
 * Equation: cut=param1; threshold=param2; (geomN(cut,L{MN/2}..L{MN})/(geomN(cut,L0..L{MN/2})/100)-100) > threshold
 */
class LongUpBigCutOkTest {

    private static final String EQUATION_ID = "LONG_UP_BIG_CUT_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testWithNoCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(80, 90, 100, 105, 110, 115);
        assertTrue(evaluateEquation(EQUATION_ID, "0 5", data));
    }

    @Test
    void testWithCuttingExtremes() throws IOException, URISyntaxException {
        List<String> data = createDataList(50, 90, 100, 110, 105, 150);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testGeometricMeanWithCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(10, 90, 95, 100, 110, 115, 200);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        List<String> data = createDataList(50, 100, 105, 150, 105, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
        assertFalse(evaluateEquation(EQUATION_ID, "1 20", data));
    }

    @Test
    void testNoChange() throws IOException, URISyntaxException {
        List<String> data = createConstantDataList(100, 6);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testNewerHalfLower() throws IOException, URISyntaxException {
        List<String> data = createDataList(110, 115, 120, 80, 85, 90);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testLargeDataSet() throws IOException, URISyntaxException {
        List<String> data = createDataList(10, 85, 90, 95, 100, 105, 110, 115, 120, 200);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(98, 99.5, 100, 102, 100.5, 101, 101.5);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0.5", data));
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        List<String> data = createDataList(40, 48.5, 50.5, 70, 58.5, 60.5);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testHighCutValue() throws IOException, URISyntaxException {
        List<String> data = createDataList(10, 20, 85, 90, 95, 105, 110, 115, 180, 190);
        assertTrue(evaluateEquation(EQUATION_ID, "2 5", data));
    }

    @Test
    void testGeometricSensitivityWithCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(5, 90, 95, 110, 115, 200);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(50, 90, 100, 150, 110, 120);
        EvaluationResult result = solveWithLogs(EQUATION_ID, "1 5", data);
        assertTrue(result.getResultAsBool());
        assertFalse(result.getLog().isEmpty());
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(50, 100, 105, 150, 105.01, 110.02);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(50, 100, 105, 150, 104.99, 109.98);
        assertFalse(evaluateEquation(EQUATION_ID, "1 18", data));
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(50, 100, 100.1, 150, 100.2, 100.3);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0", data));
    }
}

// Made with Bob
