package io.jenkins.plugins.genericchart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class PresetEquationsManager {

    private static final Object lock = new Object();
    private static List<PresetEquationDefinition> internals;

    public PresetEquationsManager() throws IOException, URISyntaxException {
        this(null);
    }

    public PresetEquationsManager(String anotherUrlOrBody) throws IOException, URISyntaxException {
        synchronized (lock) {
            if (internals == null) {
                internals = readInternals();
                if (anotherUrlOrBody != null && !anotherUrlOrBody.trim().isEmpty()) {
                    internals.addAll(readExternals(anotherUrlOrBody));
                }
            }
        }
    }

    public static void resetCached() {
        synchronized (lock) {
            internals = null;
        }
    }

    private List<PresetEquationDefinition> readExternals(String bodyOrUrl) throws IOException, URISyntaxException {
        synchronized (lock) {
            String trimmed = bodyOrUrl.trim();
            // Check if it's JSON (starts with [ or {) or multi-line text
            if (trimmed.startsWith("[") || trimmed.startsWith("{") || bodyOrUrl.split("\n").length > 1) {
                return readFromStream(new ByteArrayInputStream(bodyOrUrl.getBytes(StandardCharsets.UTF_8)));
            } else {
                // It's a URL
                return readFromStream(new URI(bodyOrUrl).toURL().openStream());
            }
        }
    }

    private List<PresetEquationDefinition> readInternals() throws IOException {
        synchronized (lock) {
            return readFromStream(this.getClass().getResourceAsStream("presetEquations.json"));
        }
    }

    private static List<PresetEquationDefinition> readFromStream(InputStream in) throws IOException {
        synchronized (lock) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<PresetEquationDefinitionJson>>(){}.getType();
                List<PresetEquationDefinitionJson> jsonList = gson.fromJson(br, listType);
                
                List<PresetEquationDefinition> parsed = new ArrayList<>();
                for (PresetEquationDefinitionJson json : jsonList) {
                    parsed.add(new PresetEquationDefinition(json.id, json.comments, json.body));
                }
                return parsed;
            }
        }
    }

    @SuppressFBWarnings(value = {"UWF_UNWRITTEN_FIELD"}, justification = "written to by gson builder")
    private static class PresetEquationDefinitionJson {
        String id;
        List<String> comments;
        List<String> body;
    }

    public void print(PrintStream logger) {
        for (PresetEquationDefinition def : internals) {
            logger.println("***" + def.getId() + "***");
            logger.println(def.getComment());
            logger.println(def.getExpression());
            PresetEquation expanded = new PresetEquation(def.getExpression(), "99 88 77 66 55 44 33 22 11".split(" "));
            logger.println("eg: " + expanded.getExpression());
            logger.println(" -- ");
        }
        logger.println("summary: " + getIds().stream().collect(Collectors.joining(", ")));
    }

    public List<String> getIds() {
        return internals.stream().map( a->a.getId()).sorted().collect(Collectors.toList());
    }

    public PresetEquation get(String idWithParams) {
        String[] fullSplit = idWithParams.split("\\s+");
        String id = fullSplit[0];
        String[] params = idWithParams.replaceFirst(id+"\\s+","").split("\\s+");
        for (PresetEquationDefinition def : internals) {
            if (def.getId().equals(id)) {
                return new PresetEquation(def.getExpression(), params);
            }
        }
        return null;
    }

    public static class PresetEquationDefinition {
        private final List<String> comments;
        private final List<String> body;
        private final String id;

        public PresetEquationDefinition(String id, List<String> comments, List<String> body) {
            this.id = id;
            this.comments = Collections.unmodifiableList(comments);
            this.body = Collections.unmodifiableList(body);
        }

        public String getId() {
            return id;
        }

        public String getExpression() {
            return body.stream().collect(Collectors.joining(" "));
        }

        public String getComment() {
            return comments.stream().collect(Collectors.joining("\n"));
        }
    }

    public static class PresetEquation {
        private final String original;
        private final String expression;

        private PresetEquation(String original, String... params) {
            this.original = original;
            expression = expand(original, params);
        }

        private String expand(String original, String[] params) {
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

}
