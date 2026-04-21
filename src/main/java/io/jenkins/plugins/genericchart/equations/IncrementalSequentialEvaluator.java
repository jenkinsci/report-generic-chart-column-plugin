package io.jenkins.plugins.genericchart.equations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.expanding.ExpandingExpressionParser;
import parser.logical.ExpressionLogger;

public class IncrementalSequentialEvaluator {

    private final List<NamedEquationDefinition> equations;
    private final List<String> commetns;
    private final Map<String, String> results = new HashMap<>();

    public IncrementalSequentialEvaluator(List<NamedEquationDefinition> equations, List<String> comments) {
        this.equations = equations;
        this.commetns = comments;
    }

    public IncrementalSequentialEvaluator(List<NamedEquationDefinition> equations, Map<String, String> alreadyKnownResults, List<String> comments) {
        this.equations = equations;
        this.results.putAll(alreadyKnownResults);
        this.commetns = comments;
    }

    public String solve(List<String> dataValues, String[] params, ExpressionLogger logger, ExpressionLogger descriptionReader, PresetEquationsManager manager) {
        if (commetns != null) {
            for(String comment : commetns){
                descriptionReader.log(comment);
            }
        }
        String lastResult = "NaN";
        Map<String, String> allEquationsDefs = new HashMap<>();
        for (NamedEquationDefinition def : equations) {
            allEquationsDefs.put(def.getName() + "_orig", def.getEquationAsString());
        }
        Map<String, String> allEquationsExp = new HashMap<>();
        for (NamedEquationDefinition def : equations) {
            String thisExpanded = "N/A";
            //this is really just for calling embed, otherwise would be completely wrong
            String tmpDefForPreset = PresetEquation.expand(def.getEquationAsString(), params);
            PresetEquationDefinition isPreset = null;
            if (manager != null) {
                isPreset = manager.getFromCommandString(tmpDefForPreset);
            }
            if (isPreset != null) {
                logger.log("Subcall: " + tmpDefForPreset);
                String subResult = isPreset.getExpressions().solve(dataValues, PresetEquationsManager.getParamsFromParams(tmpDefForPreset), new ExpressionLogger.InheritingExpressionLogger(logger), descriptionReader, manager);
                logger.log("Subcall end: " + tmpDefForPreset);
                thisExpanded = tmpDefForPreset;
                lastResult = subResult;
            } else {
                ExpandingExpressionParser ex = new ExpandingExpressionParser(new PresetEquation(def.getEquationAsString(),params).getExpression(results), dataValues, logger);
                thisExpanded = ex.getExpanded();
                lastResult = ex.solve();
            }
            allEquationsExp.put(def.getName()+"_ex", thisExpanded);
            results.put(def.getName(), lastResult);
            for (NamedEquationDescriptionDefinition desc : def.getDescriptions()) {
                final Map<String, String> additionalVariablesForDescriptions = new HashMap<>();
                additionalVariablesForDescriptions.putAll(results);
                additionalVariablesForDescriptions.putAll(allEquationsExp);
                additionalVariablesForDescriptions.put("RESULT", lastResult);
                additionalVariablesForDescriptions.put("EXEQ", thisExpanded);
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
                                    //here we are expanding CONDO and ORIGEQ and others which we do not want to see expanded
                                    String finalMessage = PresetEquation.repalceVariable("CONDO", desc.getCondition(), dex.getExpanded());
                                    finalMessage = PresetEquation.repalceVariable("ORIGEQ", def.getEquationAsString(), finalMessage);
                                    for (Map.Entry<String, String> origFunctions : allEquationsDefs.entrySet()) {
                                        finalMessage = PresetEquation.repalceVariable(origFunctions.getKey(), origFunctions.getValue(), finalMessage);
                                    }
                                    descriptionReader.log(finalMessage);
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

    public boolean evaluate(List<String> dataValues, String[] params, ExpressionLogger logger, ExpressionLogger descriptionsReader, PresetEquationsManager manager) {
        return Boolean.parseBoolean(solve(dataValues, params, logger, descriptionsReader, manager));
    }

    public static IncrementalSequentialEvaluator getUserDefIncrementalSequentialEvaluator(String presetName) {
        return new IncrementalSequentialEvaluator(Arrays.asList(new NamedEquationDefinition("userDef", Arrays.asList(presetName), null)), null);
    }
}
