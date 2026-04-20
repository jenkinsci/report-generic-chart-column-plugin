package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for SHORT_DOWN_BIG_CUT_OK preset equation.
 * 
 * Equation: cut=param1; threshold=param2; (geomN(cut,..L1)/(L0/100)-100) < -threshold
 */
class ShortDownBigCutOkTest {

    private static final String EQUATION_ID = "SHORT_DOWN_BIG_CUT_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testWithNoCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 85, 80);
        assertTrue(evaluateEquation(EQUATION_ID, "0 5", data));
    }

    @Test
    void testWithCuttingExtremes() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 150, 90, 85, 80, 10);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testGeometricMeanWithCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 200, 95, 94, 93, 50);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testCuttingTwoExtremes() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 200, 180, 95, 94, 93, 40, 30);
        assertTrue(evaluateEquation(EQUATION_ID, "2 5", data));
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 150, 95, 95, 10);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testWithConstantValues() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 90, 90, 90, 90, 90);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testCuttingImprovesGeometricMean() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 200, 85, 84, 83, 82);
        assertTrue(evaluateEquation(EQUATION_ID, "1 10", data));
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 102, 99.5, 99, 98);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0.5", data));
    }

    @Test
    void testLargeDataSet() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 200, 190, 90, 89, 88, 87, 86, 85, 10, 20);
        assertTrue(evaluateEquation(EQUATION_ID, "2 5", data));
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        List<String> data = createDataList(50.5, 70, 45.5, 44.5, 43.5, 30);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testHighCutValue() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 170, 160, 150, 90, 89, 88, 50, 40, 30);
        assertTrue(evaluateEquation(EQUATION_ID, "3 5", data));
    }

    @Test
    void testGeometricSensitivityWithCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 200, 90, 90, 90, 5);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 200, 90, 85, 80, 10);
        EvaluationResult result = evaluateWithLog(EQUATION_ID, "1 5", data);
        assertTrue(result.getResult());
        assertFalse(result.getLog().isEmpty());
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 150, 94.99, 94.99, 10);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 150, 95.01, 95.01, 10);
        assertFalse(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testSymmetricCutting() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 140, 95, 90, 85, 60);
        assertTrue(evaluateEquation(EQUATION_ID, "1 5", data));
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(100, 150, 99.98, 99.99, 10);
        assertTrue(evaluateEquation(EQUATION_ID, "1 0", data));
    }
}

// Made with Bob
