package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for LONG_UP_BIG_OK preset equation.
 * 
 * Equation: threshold=param1; (geom(L{MN/2}..L{MN})/(geom(L0..L{MN/2})/100)-100) > threshold
 */
class LongUpBigOkTest {

    private static final String EQUATION_ID = "LONG_UP_BIG_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testWithEvenNumberOfPoints() throws IOException, URISyntaxException {
        List<String> data = createDataList(80, 90, 100, 105, 110, 115);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testWithOddNumberOfPoints() throws IOException, URISyntaxException {
        List<String> data = createDataList(80, 85, 90, 95, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testGeometricMeanSensitivity() throws IOException, URISyntaxException {
        List<String> data = createDataList(50, 90, 100, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        List<String> data = createDataList(95, 100, 105, 100, 105, 110);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testNoChange() throws IOException, URISyntaxException {
        List<String> data = createConstantDataList(100, 6);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testNewerHalfLower() throws IOException, URISyntaxException {
        List<String> data = createDataList(110, 115, 120, 80, 85, 90);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testLargeDataSet() throws IOException, URISyntaxException {
        List<String> data = createDataList(85, 88, 90, 92, 95, 105, 108, 110, 112, 115);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testSmallThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(99, 99.5, 100, 100.5, 101, 101.5);
        assertTrue(evaluateEquation(EQUATION_ID, "0.5", data));
    }

    @Test
    void testZeroThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(99.9, 100, 100.1, 100.2, 100.3, 100.4);
        assertTrue(evaluateEquation(EQUATION_ID, "0", data));
    }

    @Test
    void testHighThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(90, 95, 100, 105, 110, 115);
        assertFalse(evaluateEquation(EQUATION_ID, "50", data));
    }

    @Test
    void testWithDecimalValues() throws IOException, URISyntaxException {
        List<String> data = createDataList(45.5, 48.5, 50.5, 55.5, 58.5, 60.5);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(80, 90, 100, 110, 115, 120);
        EvaluationResult result = evaluateWithLog(EQUATION_ID, "5", data);
        assertTrue(result.getResult());
        assertFalse(result.getLog().isEmpty());
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(95, 100, 105, 100, 105.01, 110.02);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        List<String> data = createDataList(95, 100, 105, 100, 104.99, 109.98);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testLargeValues() throws IOException, URISyntaxException {
        List<String> data = createDataList(9000, 9500, 10000, 10500, 11000, 11500);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testSmallValues() throws IOException, URISyntaxException {
        List<String> data = createDataList(0.9, 0.95, 1.0, 1.05, 1.1, 1.15);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }
}

// Made with Bob
