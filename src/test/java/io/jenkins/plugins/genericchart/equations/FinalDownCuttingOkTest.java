package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for FINAL_DOWN_CUTTING_OK preset equation.
 * 
 * Combination: LONG_DOWN_CUT_OK or SHORT_DOWN_CUT_OK or IMMEDIATE_DOWN_OK
 */
class FinalDownCuttingOkTest {

    private static final String EQUATION_ID = "FINAL_DOWN_CUTTING_OK";

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
        List<String> data = createDataList(100, 90, 85, 80);
        assertTrue(evaluateEquation(EQUATION_ID, "1 20 5 20", data));
    }

    @Test
    void testLongConditionPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(110, 115, 120, 80, 85, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "1 20 20 5", data));
    }

    @Test
    void testAllConditionsFail() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 99, 98, 97);
        assertFalse(evaluateEquation(EQUATION_ID, "1 50 50 50", data));
    }

    @Test
    void testMultipleConditionsPass() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 85, 80);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5 5 50", data));
    }

    @Test
    void testWithCuttingEffect() throws IOException, URISyntaxException {
        List<String> data = createDataList(150, 100, 95, 50, 85, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "1 20 20 5", data));
    }

    @Test
    void testImmediateOnlyPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5 50 50", data));
    }

    @Test
    void testShortOnlyPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 98, 90, 85);
        assertTrue(evaluateEquation(EQUATION_ID, "1 50 5 50", data));
    }

    @Test
    void testLongOnlyPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(110, 115, 120, 80, 85, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "1 50 50 5", data));
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 85, 80);
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
    void testAllThresholdsHigh() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 85, 80);
        assertFalse(evaluateEquation(EQUATION_ID, "1 50 50 50", data));
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
