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

    public String solve(List<String> dataValues, String[] params, ExpressionLogger logger, ExpressionLogger descriptionReader) {
        String lastResult = "NaN";
        for (NamedEquationDefinition def : equations) {
            //TODO check if there is maybe preddefined equation?
            //then calc it, and only use reuslt
            ExpandingExpressionParser ex = new ExpandingExpressionParser(new PresetEquation(def.getEquationAsString(),params).getExpression(results), dataValues, logger);
            lastResult = ex.solve();
            results.put(def.getName(), lastResult);
            for (NamedEquationDescriptionDefinition desc : def.getDescriptions()) {
                final Map<String, String> additionalVariablesForDescriptions = new HashMap<>();
                additionalVariablesForDescriptions.putAll(results);
                additionalVariablesForDescriptions.put("RESULT", lastResult);
                //additionalVariablesForDescriptions.put("ORIGEQ",  def.getEquationAsString()); /*We can not include original descriptions, they would get expanded*/
                additionalVariablesForDescriptions.put("EXEQ", ex.getExpanded());
                //additionalVariablesForDescriptions.put("CONDO", desc.getCondition()); /*We can not include original descriptions, they would get expanded*/
                ExpandingExpressionParser cex = new ExpandingExpressionParser(new PresetEquation(desc.getCondition(), params).getExpression(additionalVariablesForDescriptions), dataValues, s -> {
                });
                additionalVariablesForDescriptions.put("CONDE", cex.getExpanded());
                try {
                    boolean conditionResult = cex.evaluate();
                    if (conditionResult) {
                        additionalVariablesForDescriptions.put("CONDR", conditionResult + "");
                        for (String descLine : desc.getDescriptionLines()) {
                            try {
                                if (descLine.startsWith("~")) {
                                    descriptionReader.log(descLine.replaceFirst("~", ""));
                                } else {
                                    ExpandingExpressionParser dex = new ExpandingExpressionParser(new PresetEquation(descLine, params).getExpression(additionalVariablesForDescriptions), dataValues, s -> {
                                    });
                                    //here we are expanding CONDO and ORIGEQ
                                    descriptionReader.log(PresetEquation.repalceVariable("ORIGEQ", def.getEquationAsString(), PresetEquation.repalceVariable("CONDO", desc.getCondition(), dex.getExpanded())));
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                                descriptionReader.log("Failed to expand description: " + descLine);
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    descriptionReader.log("Failed to parse condition " + desc.getCondition() + ". message ommited");
                }
            }
        }
        return lastResult;
    }

    public boolean evaluate(List<String> dataValues, String[] params, ExpressionLogger logger, ExpressionLogger descriptionsReader) {
        return Boolean.parseBoolean(solve(dataValues, params, logger, descriptionsReader));
    }

    public static IncrementalSequentialEvaluator getUserDefIncrementalSequentialEvaluator(String presetName) {
        return new IncrementalSequentialEvaluator(Arrays.asList(new NamedEquationDefinition("userDef", Arrays.asList(presetName), null)));
    }
}
