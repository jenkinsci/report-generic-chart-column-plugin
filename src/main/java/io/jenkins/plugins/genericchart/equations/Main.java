/*
 * The MIT License
 *
 * Copyright 2026 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.genericchart.equations;

import parser.logical.ExpressionLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class for launching parser-ng extended parser from fat jar.
 * Executes preset equations from presetEquations.json file.
 *
 * Usage: java -jar fat.jar <PRESET_NAME> <param1> <param2> ... <value1> <value2> ...
 * 
 * Example: java -jar fat.jar IMMEDIATE_UP_OK 5 100 95 90 85
 *   - IMMEDIATE_UP_OK is the preset equation name
 *   - 5 is the threshold parameter
 *   - 100 95 90 85 are the data values (L0=100, L1=95, L2=90, L3=85)
 */
public class Main {

    //FIXME when exeuted from job dir, the expressions must be read from config.xml, and data to the past jobs...
    public static void main(String[] args) throws Exception {
        System.err.println("jenkins-report-generic-chart-column Preset Equation Evaluator");
        System.err.println("==============================================================");
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }
        String presetName = args[0];
        if (presetName.equalsIgnoreCase("LIST") || presetName.equalsIgnoreCase("--list") || presetName.equalsIgnoreCase("-l")) {
            listPresets();
            System.exit(0);
        }
        if (presetName.equalsIgnoreCase("--help") || presetName.equalsIgnoreCase("-h")) {
            printUsage();
            System.exit(0);
        }
        PresetEquationsManager manager = new PresetEquationsManager();
        if (presetName.equalsIgnoreCase("--readme")) {
            System.err.println(manager.readReadme());
            System.exit(0);
        }
        PresetEquationDefinition preset = manager.getFromCommandString(presetName);
        List<String> dataValues = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            dataValues.add(args[i]);
        }
        System.err.println("Evaluating preset equation...");
        System.err.println("Data values: " + dataValues);
        System.err.println("params: " + String.join(", ",PresetEquationsManager.getParamsFromParams(presetName)));
        System.err.println("name: " + PresetEquationsManager.getIdFromParams(presetName));
        System.err.println();
        ExpressionLogger logger = new ExpressionLogger() {
            @Override
            public void log(String s) {
                System.err.println(s);
            }
        };
        ExpressionLogger descriptionReader = new ExpressionLogger() {
            @Override
            public void log(String s) {
                System.out.println(s);
            }
        };
        IncrementalSequentialEvaluator expresion;
        if (preset == null) {
            System.err.println("Error: Preset equation '" + presetName + "' not found. Run --help or --list for more. Now evaluating.");
            expresion = IncrementalSequentialEvaluator.getUserDefIncrementalSequentialEvaluator(presetName);
        } else {
            expresion = preset.getExpressions();
        }
        String result = expresion.solve(dataValues, PresetEquationsManager.getParamsFromParams(presetName), logger, descriptionReader, manager);

        System.err.println("Evaluation result: " + result);
        System.err.println();
    }

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("  java -jar fat.jar \"<PRESET_NAME_OR_EQUATION> PRESET_PARAM1 PRESET_PARAM2 ... PRESET_PARAMN\" <datavalue1> <datavalue2> ...");
        System.err.println();
        System.err.println("Options:");
        System.err.println("  LIST, --list, -l    List all available preset equations");
        System.err.println("  --help, -h          Show this help message");
        System.err.println("  --readme            shows in-tree readme");
        System.err.println();
        System.err.println("Examples:");
        System.err.println("  java -jar fat.jar \"IMMEDIATE_UP_OK 5\" 100 95 90 85");
        System.err.println("    - IMMEDIATE_UP_OK is the preset name");
        System.err.println("    - 5 is the  preset equation parameter");
        System.err.println("    - 100 95 90 85 are data values (L0=100, L1=95, L2=90, L3=85)");
        System.err.println();
        System.err.println("  java -jar fat.jar \"SHORT_UP_CUT_OK 1 5\" 100 95 90 85 80");
        System.err.println("    - cut=1, threshold=5, data values follow");
        System.err.println();
        System.err.println("The preset equation will be expanded with parameters and evaluated with data.");
        System.err.println("All arguments after preset name are used as data values for evaluation.");
        System.err.println("If preset equation is not found, the first argument is evaluated as equation (with others as params) as expected.");
    }
    
    private static void listPresets() {
        try {
            PresetEquationsManager manager = new PresetEquationsManager();
            System.err.println("Available Preset Equations:");
            System.err.println("===========================");
            System.err.println();
            manager.print(System.err);
        } catch (Exception e) {
            System.err.println("Error loading presets: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
