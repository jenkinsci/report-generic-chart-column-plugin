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
 * Test class for FINAL_DOWN_BIG_CUTTING_OK preset equation.
 * 
 * Combination: LONG_DOWN_BIG_CUT_OK or SHORT_DOWN_BIG_CUT_OK or IMMEDIATE_DOWN_OK
 */
class FinalDownBigCuttingOkTest {

    private static final String EQUATION_ID = "FINAL_DOWN_BIG_CUTTING_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testImmediateConditionPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5 20 20", data));
    }

    @Test
    void testShortConditionPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 200, 90, 85, 80, 10);
        assertTrue(evaluateEquation(EQUATION_ID, "1 20 5 20", data));
    }

    @Test
    void testLongConditionPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(200, 110, 120, 10, 85, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "1 20 20 5", data));
    }

    @Test
    void testAllConditionsFail() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 99, 98, 97);
        assertFalse(evaluateEquation(EQUATION_ID, "1 50 50 50", data));
    }

    @Test
    void testGeometricMeanWithCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(200, 100, 95, 90, 5);
        assertTrue(evaluateEquation(EQUATION_ID, "1 20 5 20", data));
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 200, 90, 85, 10);
        EvaluationResult result = solveWithLogs(EQUATION_ID, "1 5 5 5", data);
        assertTrue(result.getResultAsBool());
        assertFalse(result.getLog().isEmpty());
    }

    @Test
    void testAllThresholdsLow() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 99, 98, 97);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0.5 0.5 0.5", data));
    }

    @Test
    void testZeroCut() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 85, 80);
        assertTrue(evaluateEquation(EQUATION_ID, "0 5 5 5", data));
    }

    @Test
    void testHighCut() throws IOException, URISyntaxException {
        List<String> data = createDataList(190, 180, 100, 95, 90, 20, 10);
        assertTrue(evaluateEquation(EQUATION_ID, "2 50 50 5", data));
    }

    @Test
    void testIncreasingTrend() throws IOException, URISyntaxException {
        List<String> data = createIncreasingDataList(100, 5, 6);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5 5 5", data));
    }
}

// Made with Bob
