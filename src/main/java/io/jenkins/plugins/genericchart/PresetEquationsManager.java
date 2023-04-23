package io.jenkins.plugins.genericchart;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PresetEquationsManager {

    private static final Object lock = new Object();
    private static List<PresetEquationDefinition> internals;

    public PresetEquationsManager() throws IOException {
        this(null);
    }

    public PresetEquationsManager(String anotherUrlOrBody) throws IOException {
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

    private List<PresetEquationDefinition> readExternals(String bodyOrUrl) throws IOException {
        synchronized (lock) {
            if (bodyOrUrl.split("\n").length > 1) {
                return readFromStream(new ByteArrayInputStream(bodyOrUrl.getBytes(StandardCharsets.UTF_8)));
            } else {
                return readFromStream(new URL(bodyOrUrl).openStream());
            }
        }
    }

    private List<PresetEquationDefinition> readInternals() throws IOException {
        synchronized (lock) {
            return readFromStream(this.getClass().getResourceAsStream("presetEquations"));
        }
    }

    private static List<PresetEquationDefinition> readFromStream(InputStream in) throws IOException {
        synchronized (lock) {
            List<String> futureComments = new ArrayList<>();
            List<String> futureBody = new ArrayList<>();
            List<PresetEquationDefinition> parsed = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                while (true) {
                    String s = br.readLine();
                    if (s == null) {
                        if (!futureBody.isEmpty()) {
                            parsed.add(new PresetEquationDefinition(futureComments, futureBody));
                        }
                        break;
                    }
                    if (s.trim().isEmpty()) {
                        continue;
                    }
                    if (s.trim().startsWith("#")) {
                        if (!futureBody.isEmpty()) {
                            parsed.add(new PresetEquationDefinition(futureComments, futureBody));
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
        private final String comment;
        private final String expression;
        private final String id;

        public PresetEquationDefinition(List<String> comments, List<String> body) {
            this.comments = Collections.unmodifiableList(comments);
            this.body = Collections.unmodifiableList(body);
            comment = comments.stream().collect(Collectors.joining("\n"));
            expression = body.stream().collect(Collectors.joining(" "));
            id = comments.get(0).replaceFirst("#*", "").trim();
        }

        public String getId() {
            return id;
        }

        public String getExpression() {
            return expression;
        }

        public String getComment() {
            return comment;
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
