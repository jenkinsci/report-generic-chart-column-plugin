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
 * Test class for LONG_DOWN_CUT_OK preset equation.
 * 
 * Equation: cut=param1; threshold=param2; (avgN(cut,L{MN/2}..L{MN})/(avgN(cut,L0..L{MN/2})/100)-100) < -threshold
 */
class LongDownCutOkTest {

    private static final String EQUATION_ID = "LONG_DOWN_CUT_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testWithNoCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(110, 115, 120, 85, 90, 95);
        assertTrue(evaluateEquation(EQUATION_ID, "0 5", data));
    }

    @Test
    void testWithCuttingExtremes() throws IOException, URISyntaxException {
        List<String> data = createDataList(150, 110, 120, 50, 85, 95);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testCuttingImprovesResult() throws IOException, URISyntaxException {
        List<String> data = createDataList(200, 110, 115, 120, 10, 85, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        List<String> data = createDataList(150, 105, 110, 50, 95, 100);
        assertFalse(evaluateEquation(EQUATION_ID, "1 25", data));
    }

    @Test
    void testNoChange() throws IOException, URISyntaxException {
        List<String> data = createConstantDataList(100, 6);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testNewerHalfHigher() throws IOException, URISyntaxException {
        List<String> data = createDataList(80, 85, 90, 110, 115, 120);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testLargeDataSet() throws IOException, URISyntaxException {
        List<String> data = createDataList(200, 105, 110, 115, 120, 10, 85, 90, 95, 100);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(102, 100.5, 101, 101.5, 98, 99.5, 100);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0.5", data));
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        List<String> data = createDataList(70, 58.5, 60.5, 40, 48.5, 50.5);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testHighCutValue() throws IOException, URISyntaxException {
        List<String> data = createDataList(190, 180, 105, 110, 115, 10, 20, 85, 90, 95);
        assertTrue(evaluateEquation(EQUATION_ID, "2 5", data));
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(150, 110, 120, 50, 90, 100);
        EvaluationResult result = solveWithLogs(EQUATION_ID, "1 5", data);
        assertTrue(result.getResultAsBool());
        assertFalse(result.getLog().isEmpty());
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(150, 105, 110, 50, 94.99, 99.98);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(150, 105, 110, 50, 95.01, 100.02);
        assertFalse(evaluateEquation(EQUATION_ID, "1 25", data));
    }

    @Test
    void testSymmetricCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(140, 110, 120, 60, 90, 100);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(150, 100.2, 100.3, 50, 100, 100.1);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0", data));
    }
}

// Made with Bob
