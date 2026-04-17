package io.jenkins.plugins.genericchart.equations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PresetEquationDefinition {
    private final List<String> comments;
    private final List<NamedEquationDefinition> equations;
    private final String id;

    public PresetEquationDefinition(String id, List<String> comments, List<NamedEquation> equations) {
        this.id = id;
        this.comments = Collections.unmodifiableList(comments);
        // Convert NamedEquation (JSON) to NamedEquationDefinition (immutable)
        List<NamedEquationDefinition> eqList = new ArrayList<>();
        for (NamedEquation eq : equations) {
            eqList.add(new NamedEquationDefinition(eq.name, eq.equation));
        }
        this.equations = Collections.unmodifiableList(eqList);
    }

    public String getId() {
        return id;
    }

    public String getExpression() {
        StringBuilder sb = new StringBuilder();
        for (NamedEquationDefinition eq : equations) {
            for (String line : eq.getEquation()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(line);
            }
        }
        return sb.toString();
    }

    public String getComment() {
        return comments.stream().collect(Collectors.joining("\n"));
    }

    public List<NamedEquationDefinition> getEquations() {
        return equations;
    }
}
