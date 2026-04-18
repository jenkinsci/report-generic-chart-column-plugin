package io.jenkins.plugins.genericchart.equations;

import java.util.Map;

public class PresetEquation {
    private final String original;
    private final String expression;

    public PresetEquation(String original, String... params) {
        this.original = original;
        expression = expand(original, params);
    }

    static String expand(String original, String[] params) {
        String fex = original;
        for (int i = 0; i < params.length; i++) {
            fex = repalceVariable("" + (i + 1), params[i], fex);
        }
        return fex;
    }

    static String expand(String original, Map<String, String> namedParams) {
        String fex = original;
        for (Map.Entry<String, String> entry : namedParams.entrySet()) {
            fex = repalceVariable(entry.getKey(), entry.getValue(), fex);
        }
        return fex;
    }

    private static String repalceVariable(String key, String value, String fex) {
        return fex.replaceAll("/\\*" + key + "\\*/", value);
    }

    public String getOriginal() {
        return original;
    }

    public String getExpression() {
        return expression;
    }

    public String getExpression(Map<String, String> namedParams) {
        return expand(expression, namedParams);
    }
}
