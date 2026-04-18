package io.jenkins.plugins.genericchart.equations;

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
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                Type listType = new TypeToken<List<PresetEquationDefinitionJson>>() {
                }.getType();
                List<PresetEquationDefinitionJson> jsonList = gson.fromJson(br, listType);

                List<PresetEquationDefinition> parsed = new ArrayList<>();
                for (PresetEquationDefinitionJson json : jsonList) {
                    parsed.add(new PresetEquationDefinition(json.id, json.comments, json.equations));
                }
                return parsed;
            }
        }
    }

    public void print(PrintStream logger) {
        for (PresetEquationDefinition def : internals) {
            logger.println("***" + def.getId() + "***");
            logger.println(def.getComment());
            logger.println(def.getConnectedSingleExpression());
            PresetEquation expanded = new PresetEquation(def.getConnectedSingleExpression(), "99 88 77 66 55 44 33 22 11".split(" "));
            logger.println("eg: " + expanded.getExpression());
            logger.println(" -- ");
        }
        logger.println("summary: " + getIds().stream().collect(Collectors.joining(", ")));
    }

    public List<String> getIds() {
        return internals.stream().map(a -> a.getId()).sorted().collect(Collectors.toList());
    }

    //the params are not used. They are just filtered out
    public PresetEquationDefinition getFromCommandString(String idWithParams) {
        String id = getIdFromParams(idWithParams);
        for (PresetEquationDefinition def : internals) {
            if (def.getId().equals(id)) {
                return def;
            }
        }
        return null;
    }

    public static String getIdFromParams(String idWithParams) {
        String[] fullSplit = idWithParams.split("\\s+");
        String id = fullSplit[0];
        return id;
    }

    public static String[] getParamsFromParams(String idWithParams) {
        String id = getIdFromParams(idWithParams);
        return idWithParams.replaceFirst(Pattern.quote(id) + "\\s+", "").split("\\s+");
    }

}
