package io.jenkins.plugins.genericchart.equations;

import io.jenkins.plugins.genericchart.equations.IncrementalSequentialEvaluator;
import io.jenkins.plugins.genericchart.equations.PresetEquationDefinition;
import io.jenkins.plugins.genericchart.equations.PresetEquationsManager;
import parser.logical.ExpressionLogger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for testing preset equations.
 * Provides common helper methods for evaluating equations with various data scenarios.
 */
public class PresetEquationTestUtil {

    /**
     * Evaluates a preset equation with given parameters and data values.
     * 
     * @param equationId The ID of the preset equation
     * @param params Parameters for the equation (e.g., thresholds)
     * @param dataValues List of data values to evaluate against
     * @return true if equation evaluates to true, false otherwise
     * @throws IOException if there's an error reading equation definitions
     * @throws URISyntaxException if there's an error with URIs
     */
    public static boolean evaluateEquation(String equationId, String params, List<String> dataValues) 
            throws IOException, URISyntaxException {
        PresetEquationsManager manager = new PresetEquationsManager();
        String commandString = equationId + " " + params;
        PresetEquationDefinition preset = manager.getFromCommandString(commandString);
        
        if (preset == null) {
            throw new IllegalArgumentException("Equation not found: " + equationId);
        }
        
        IncrementalSequentialEvaluator evaluator = preset.getExpressions();
        String[] paramArray = PresetEquationsManager.getParamsFromParams(commandString);
        
        StringBuilder logBuilder = new StringBuilder();
        ExpressionLogger logger = s -> logBuilder.append(s).append("\n");
        
        return evaluator.evaluate(dataValues, paramArray, logger, logger, manager);
    }

    /**
     * Evaluates a preset equation and captures the log output.
     * 
     * @param equationId The ID of the preset equation
     * @param params Parameters for the equation
     * @param dataValues List of data values to evaluate against
     * @return EvaluationResult containing the result and log output
     * @throws IOException if there's an error reading equation definitions
     * @throws URISyntaxException if there's an error with URIs
     */
    public static EvaluationResult evaluateWithLog(String equationId, String params, List<String> dataValues) 
            throws IOException, URISyntaxException {
        PresetEquationsManager manager = new PresetEquationsManager();
        String commandString = equationId + " " + params;
        PresetEquationDefinition preset = manager.getFromCommandString(commandString);
        
        if (preset == null) {
            throw new IllegalArgumentException("Equation not found: " + equationId);
        }
        
        IncrementalSequentialEvaluator evaluator = preset.getExpressions();
        String[] paramArray = PresetEquationsManager.getParamsFromParams(commandString);
        
        StringBuilder logBuilder = new StringBuilder();
        ExpressionLogger logger = s -> logBuilder.append(s).append("\n");
        
        boolean result = evaluator.evaluate(dataValues, paramArray, logger, logger, manager);
        
        return new EvaluationResult(result, logBuilder.toString());
    }

    /**
     * Creates a list of string values from doubles.
     * 
     * @param values Variable number of double values
     * @return List of string representations of the values
     */
    public static List<String> createDataList(double... values) {
        List<String> dataList = new ArrayList<>();
        for (double value : values) {
            dataList.add(String.valueOf(value));
        }
        return dataList;
    }

    /**
     * Creates a list of string values from integers.
     * 
     * @param values Variable number of integer values
     * @return List of string representations of the values
     */
    public static List<String> createDataList(int... values) {
        List<String> dataList = new ArrayList<>();
        for (int value : values) {
            dataList.add(String.valueOf(value));
        }
        return dataList;
    }

    /**
     * Creates a list with a constant value repeated n times.
     * 
     * @param value The value to repeat
     * @param count Number of times to repeat
     * @return List of string representations of the value
     */
    public static List<String> createConstantDataList(double value, int count) {
        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dataList.add(String.valueOf(value));
        }
        return dataList;
    }

    /**
     * Creates a list with an increasing sequence.
     * 
     * @param start Starting value
     * @param increment Increment between values
     * @param count Number of values
     * @return List of string representations of the sequence
     */
    public static List<String> createIncreasingDataList(double start, double increment, int count) {
        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dataList.add(String.valueOf(start + i * increment));
        }
        return dataList;
    }

    /**
     * Creates a list with a decreasing sequence.
     * 
     * @param start Starting value
     * @param decrement Decrement between values
     * @param count Number of values
     * @return List of string representations of the sequence
     */
    public static List<String> createDecreasingDataList(double start, double decrement, int count) {
        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dataList.add(String.valueOf(start - i * decrement));
        }
        return dataList;
    }

    /**
     * Result of an equation evaluation including the boolean result and log output.
     */
    public static class EvaluationResult {
        private final boolean result;
        private final String log;

        public EvaluationResult(boolean result, String log) {
            this.result = result;
            this.log = log;
        }

        public boolean getResult() {
            return result;
        }

        public String getLog() {
            return log;
        }

        public boolean logContains(String text) {
            return log.contains(text);
        }

        public boolean logContainsIgnoreCase(String text) {
            return log.toLowerCase().contains(text.toLowerCase());
        }
    }
}

// Made with Bob
