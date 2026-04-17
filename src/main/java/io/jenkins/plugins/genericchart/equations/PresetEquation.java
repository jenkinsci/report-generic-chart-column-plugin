package io.jenkins.plugins.genericchart.equations;

public class PresetEquation {
    private final String original;
    private final String expression;

    PresetEquation(String original, String... params) {
        this.original = original;
        expression = expand(original, params);
    }

    static String expand(String original, String[] params) {
        String fex = original;
        for (int i = 0; i < params.length; i++) {
            fex = fex.replaceAll("/\\*" + (i + 1) + "\\*/", params[i]);
        }
        return fex;
    }

    public String getOriginal() {
        return original;
    }

    public String getExpression() {
        return expression;
    }
}
