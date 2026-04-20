package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for FINAL_UP_CUTTING_OK preset equation.
 * 
 * Combination: LONG_UP_CUT_OK or SHORT_UP_CUT_OK or IMMEDIATE_UP_OK
 * Parameters: cut, immediate threshold, short time threshold, long time threshold
 */
class FinalUpCuttingOkTest {

    private static final String EQUATION_ID = "FINAL_UP_CUTTING_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testImmediateConditionPasses() throws IOException, URISyntaxException {
        // L0=100, L1=110, immediate threshold=5
        // (110/(100/100)-100) = 10 > 5, passes immediate
        List<String> data = createDataList(100, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5 20 20", data));
    }

    @Test
    void testShortConditionPasses() throws IOException, URISyntaxException {
        // Short condition passes with avg of previous points
        List<String> data = createDataList(100, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "1 20 5 20", data));
    }

    @Test
    void testLongConditionPasses() throws IOException, URISyntaxException {
        // Long condition passes comparing halves
        List<String> data = createDataList(80, 90, 100, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "1 20 20 5", data));
    }

    @Test
    void testAllConditionsFail() throws IOException, URISyntaxException {
        // None of the conditions pass
        List<String> data = createDataList(100, 101, 102, 103);
        assertFalse(evaluateEquation(EQUATION_ID, "1 50 50 50", data));
    }

    @Test
    void testMultipleConditionsPass() throws IOException, URISyntaxException {
        // Both immediate and short pass
        List<String> data = createDataList(100, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5 5 50", data));
    }

    @Test
    void testWithCuttingEffect() throws IOException, URISyntaxException {
        // Cutting improves results
        List<String> data = createDataList(50, 100, 105, 150, 110, 115);
        assertTrue(evaluateEquation(EQUATION_ID, "1 20 20 5", data));
    }

    @Test
    void testImmediateOnlyPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5 50 50", data));
    }

    @Test
    void testShortOnlyPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 102, 110, 115);
        assertTrue(evaluateEquation(EQUATION_ID, "1 50 5 50", data));
    }

    @Test
    void testLongOnlyPasses() throws IOException, URISyntaxException {
        List<String> data = createDataList(80, 85, 90, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "1 50 50 5", data));
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110, 115, 120);
        EvaluationResult result = evaluateWithLog(EQUATION_ID, "1 5 5 5", data);
        assertTrue(result.getResult());
        assertFalse(result.getLog().isEmpty());
    }

    @Test
    void testAllThresholdsLow() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 101, 102, 103);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0.5 0.5 0.5", data));
    }

    @Test
    void testAllThresholdsHigh() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110, 115, 120);
        assertFalse(evaluateEquation(EQUATION_ID, "1 50 50 50", data));
    }

    @Test
    void testZeroCut() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "0 5 5 5", data));
    }

    @Test
    void testHighCut() throws IOException, URISyntaxException {
        List<String> data = createDataList(10, 20, 100, 105, 110, 180, 190);
        assertTrue(evaluateEquation(EQUATION_ID, "2 50 50 5", data));
    }

    @Test
    void testDecreasingTrend() throws IOException, URISyntaxException {
        List<String> data = createDecreasingDataList(100, 5, 6);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5 5 5", data));
    }
}

// Made with Bob
