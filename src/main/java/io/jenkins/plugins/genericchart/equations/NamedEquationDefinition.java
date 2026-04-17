package io.jenkins.plugins.genericchart.equations;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NamedEquationDefinition {
    private final String name;
    private final List<String> equation;

    public NamedEquationDefinition(String name, List<String> equation) {
        this.name = name;
        this.equation = Collections.unmodifiableList(equation);
    }

    public String getName() {
        return name;
    }

    public List<String> getEquation() {
        return equation;
    }

    public String getEquationAsString() {
        return equation.stream().collect(Collectors.joining(" "));
    }
}
