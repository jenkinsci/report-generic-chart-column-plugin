package io.jenkins.plugins.genericchart.equations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.jenkins.plugins.genericchart.equations.PresetEquationTestUtil.*;

/**
 * Test class for LONG_UP_OK preset equation.
 * 
 * Equation: threshold=param1; (avg(L{MN/2}..L{MN})/(avg(L0..L{MN/2})/100)-100) > threshold
 * 
 * Compares newer half of builds against older half of builds using average.
 */
class LongUpOkTest {

    private static final String EQUATION_ID = "LONG_UP_OK";

    @BeforeEach
    void setUp() {
        PresetEquationsManager.resetCached();
    }

    @Test
    void testWithEvenNumberOfPoints() throws IOException, URISyntaxException {
        // 6 points: L0-L2 (older half avg=90), L3-L5 (newer half avg=110)
        // (110/(90/100)-100) ≈ 22.22, which is > 5
        List<String> data = createDataList(80, 90, 100, 105, 110, 115);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testWithOddNumberOfPoints() throws IOException, URISyntaxException {
        // 7 points: L0-L3 (older), L4-L6 (newer)
        List<String> data = createDataList(80, 85, 90, 95, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testNewerHalfSignificantlyHigher() throws IOException, URISyntaxException {
        // Older half: 80,85,90 (avg=85), Newer half: 110,115,120 (avg=115)
        // (115/(85/100)-100) ≈ 35.29, which is > 5
        List<String> data = createDataList(80, 85, 90, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testNewerHalfSlightlyHigher() throws IOException, URISyntaxException {
        // Older half: 95,98,100 (avg≈97.67), Newer half: 102,103,104 (avg=103)
        // (103/(97.67/100)-100) ≈ 5.46, which is > 5
        List<String> data = createDataList(95, 98, 100, 102, 103, 104);
        assertTrue(evaluateEquation(EQUATION_ID, "4.3", data));
    }

    @Test
    void testExactThresholdBoundary() throws IOException, URISyntaxException {
        // Older half avg=100, Newer half avg=105
        // (105/(100/100)-100) = 5, which is NOT > 5
        List<String> data = createDataList(95, 100, 105, 100, 105, 110);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testNoChange() throws IOException, URISyntaxException {
        // Both halves have same average
        List<String> data = createConstantDataList(100, 6);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testNewerHalfLower() throws IOException, URISyntaxException {
        // Older half higher than newer half
        List<String> data = createDataList(110, 115, 120, 80, 85, 90);
        assertFalse(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testLargeDataSet() throws IOException, URISyntaxException {
        // 10 points: older 5 avg around 90, newer 5 avg around 110
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
    void testVolatileOlderHalf() throws IOException, URISyntaxException {
        // Older half: 70,90,110 (avg=90), Newer half: 110,115,120 (avg=115)
        List<String> data = createDataList(70, 90, 110, 110, 115, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testVolatileNewerHalf() throws IOException, URISyntaxException {
        // Older half: 85,90,95 (avg=90), Newer half: 90,120,120 (avg=110)
        List<String> data = createDataList(85, 90, 95, 90, 120, 120);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testWithLogging() throws IOException, URISyntaxException {
        List<String> data = createDataList(80, 90, 100, 110, 115, 120);
        EvaluationResult result = solveWithLogs(EQUATION_ID, "5", data);
        assertTrue(result.getResultAsBool());
        assertFalse(result.getLog().isEmpty());
    }

    @Test
    void testBarelyAboveThreshold() throws IOException, URISyntaxException {
        // Older avg=100, Newer avg=105.01
        List<String> data = createDataList(95, 100, 105, 100, 105.01, 110.02);
        assertTrue(evaluateEquation(EQUATION_ID, "5", data));
    }

    @Test
    void testBarelyBelowThreshold() throws IOException, URISyntaxException {
        // Older avg=100, Newer avg=104.99
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
