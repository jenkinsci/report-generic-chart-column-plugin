package io.jenkins.plugins.genericchart.equations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.expanding.ExpandingExpressionParser;
import parser.logical.ExpressionLogger;

public class IncrementalSequentialEvaluator {

    private final List<NamedEquationDefinition> equations;
    private final Map<String, String> results = new HashMap<>();

    public IncrementalSequentialEvaluator(List<NamedEquationDefinition> equations) {
        this.equations = equations;
    }

    public IncrementalSequentialEvaluator(List<NamedEquationDefinition> equations, Map<String, String> alreadyKnownResults) {
        this.equations = equations;
        results.putAll(alreadyKnownResults);
    }

    public String solve(List<String> dataValues, String[] params,  ExpressionLogger logger) {
        String lastResult = "NaN";
        for (NamedEquationDefinition def : equations) {
            logger.log(def.getEquationAsString());
            ExpandingExpressionParser ex = new ExpandingExpressionParser(new PresetEquation(def.getEquationAsString(),params).getExpression(results), dataValues, logger);
            lastResult = ex.solve();
            results.put(def.getName(), lastResult);
        }
        return lastResult;
    }

    public boolean evaluate(List<String> dataValues, String[] params, ExpressionLogger logger) {
        return Boolean.parseBoolean(solve(dataValues, params, logger));
    }

    public static IncrementalSequentialEvaluator getUserDefIncrementalSequentialEvaluator(String presetName) {
        return new IncrementalSequentialEvaluator(Arrays.asList(new NamedEquationDefinition("userDef", Arrays.asList(presetName))));
    }
}
