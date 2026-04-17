package io.jenkins.plugins.genericchart.equations;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = {"UWF_UNWRITTEN_FIELD"}, justification = "written to by gson builder")
class PresetEquationDefinitionJson {
    String id;
    List<String> comments;
    List<NamedEquation> equations;
}
