package io.jenkins.plugins.genericchart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy loader for old text-based preset equations format.
 * Supports the old format where equations were defined with # comments followed by body lines.
 */
public class PresetEquationsLegacyLoader {

    public static List<PresetEquationsManager.PresetEquationDefinition> readFromStream(InputStream in) throws IOException {
        List<String> futureComments = new ArrayList<>();
        List<String> futureBody = new ArrayList<>();
        List<PresetEquationsManager.PresetEquationDefinition> parsed = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    if (!futureBody.isEmpty()) {
                        parsed.add(new PresetEquationsManager.PresetEquationDefinition(futureComments, futureBody));
                    }
                    break;
                }
                if (s.trim().isEmpty()) {
                    continue;
                }
                if (s.trim().startsWith("#")) {
                    if (!futureBody.isEmpty()) {
                        parsed.add(new PresetEquationsManager.PresetEquationDefinition(futureComments, futureBody));
                        futureComments = new ArrayList<>();
                        futureBody = new ArrayList<>();
                    }
                    futureComments.add(s.trim());
                } else {
                    futureBody.add(s.trim());
                }
            }
        }
        return parsed;
    }
}
