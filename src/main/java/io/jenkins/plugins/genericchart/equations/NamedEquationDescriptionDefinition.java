package io.jenkins.plugins.genericchart.equations;

import java.util.Collections;
import java.util.List;

public class NamedEquationDescriptionDefinition {
    private final String condition;
    private final List<String> description;

    public NamedEquationDescriptionDefinition(String condition, List<String> description) {
        this.condition = condition;
        this.description = description == null ? Collections.emptyList() : Collections.unmodifiableList(description);
    }

    public String getCondition() {
        return condition;
    }

    public List<String> getDescriptionLines() {
        return description;
    }
}
