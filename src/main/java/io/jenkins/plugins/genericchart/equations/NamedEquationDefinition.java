package io.jenkins.plugins.genericchart.equations;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NamedEquationDefinition {
    private final String name;
    private final List<String> equation;
    private final List<NamedEquationDescriptionDefinition> descriptions;

    public NamedEquationDefinition(String name, List<String> equation, List<NamedEquationDescription> descriptions) {
        this.name = name;
        this.equation = Collections.unmodifiableList(equation);
        if (descriptions == null) {
            this.descriptions = Collections.emptyList();
        } else {
            this.descriptions = Collections.unmodifiableList(
                    descriptions.stream()
                            .map(it -> new NamedEquationDescriptionDefinition(it.condition, it.description))
                            .collect(Collectors.toList()));
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getEquation() {
        return equation;
    }

    public List<NamedEquationDescriptionDefinition> getDescriptions() {
        return descriptions;
    }

    public String getEquationAsString() {
        return equation.stream().collect(Collectors.joining(" "));
    }
}
