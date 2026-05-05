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
 * Test class for FINAL_UP_OK preset equation.
 * 
 * Combination: LONG_UP_OK or SHORT_UP_OK or IMMEDIATE_UP_OK
 * Parameters: immediate threshold, short time threshold, long time threshold
 */
class FinalUpOkTest {

    private static final String EQUATION_ID = "FINAL_UP_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testImmediateConditionPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "5 20 20", data));
    }

    @Test
    void testShortConditionPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "20 5 20", data));
    }

    @Test
    void testLongConditionPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(80, 90, 100, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "20 20 5", data));
    }

    @Test
    void testAllConditionsFail() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 101, 102, 103);
        assertFalse(evaluateEquation(EQUATION_ID, "50 50 50", data));
    }

    @Test
    void testMultipleConditionsPass() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "5 5 50", data));
    }

    @Test
    void testImmediateOnlyPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "5 50 50", data));
    }

    @Test
    void testShortOnlyPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 102, 110, 115);
        assertTrue(evaluateEquation(EQUATION_ID, "50 5 50", data));
    }

    @Test
    void testLongOnlyPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(80, 85, 90, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "50 50 5", data));
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110, 115, 120);
        EvaluationResult result = solveWithLogs(EQUATION_ID, "5 5 5", data);
        assertTrue(result.getResultAsBool());
        assertFalse(result.getLog().isEmpty());
    }

    @Test
    void testAllThresholdsLow() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 101, 102, 103);
        assertTrue(evaluateEquation(EQUATION_ID, "0.5 0.5 0.5", data));
    }

    @Test
    void testAllThresholdsHigh() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110, 115, 120);
        assertFalse(evaluateEquation(EQUATION_ID, "50 50 50", data));
    }

    @Test
    void testDecreasingTrend() throws IOException, URISyntaxException {
        List<String> data = createDecreasingDataList(100, 5, 6);
        assertFalse(evaluateEquation(EQUATION_ID, "5 5 5", data));
    }

    @Test
    void testConstantValues() throws IOException, URISyntaxException {
        List<String> data = createConstantDataList(100, 6);
        assertFalse(evaluateEquation(EQUATION_ID, "5 5 5", data));
    }

    @Test
    void testZeroThresholds() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 100.1, 100.2, 100.3);
        assertTrue(evaluateEquation(EQUATION_ID, "0 0 0", data));
    }
}

// Made with Bob
