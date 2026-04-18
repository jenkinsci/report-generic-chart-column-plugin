package io.jenkins.plugins.genericchart;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.genericchart.equations.IncrementalSequentialEvaluator;
import io.jenkins.plugins.genericchart.equations.PresetEquation;
import io.jenkins.plugins.genericchart.equations.PresetEquationDefinition;
import io.jenkins.plugins.genericchart.equations.PresetEquationsManager;
import parser.expanding.ExpandingExpressionParser;
import parser.logical.ExpressionLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

class PresetEquationsManagerTest {

    @BeforeEach
    public void cleanCaches(){
        PresetEquationsManager.resetCached();
    }

    @Test
    public void listTest() throws IOException, URISyntaxException {
        final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        final PresetEquationsManager p1 = new PresetEquationsManager();
        try (PrintStream ps = new PrintStream(baos1, true, StandardCharsets.UTF_8)) {
            p1.print(ps);
        }
        String listing1 = baos1.toString(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PresetEquationsManager p2 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"1+1\"]}]}]");
        try (PrintStream ps = new PrintStream(baos2, true, StandardCharsets.UTF_8)) {
            p2.print(ps);
        }
        String listing2 = baos2.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals(listing1, listing2); //cahe was not reload

        PresetEquationsManager.resetCached();
        baos2 = new ByteArrayOutputStream();
        p2 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"1+1\"]}]}]");
        try (PrintStream ps = new PrintStream(baos2, true, StandardCharsets.UTF_8)) {
            p2.print(ps);
        }
         listing2 = baos2.toString(StandardCharsets.UTF_8);
         Assertions.assertNotEquals(listing1, listing2);
    }

    @Test
    public void getTest() throws IOException, URISyntaxException {
        final PresetEquationsManager p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"1+1\"]}]}]");
        PresetEquation e0 = legacyWrapper(p1, "weird_id weird_params");
        Assertions.assertNull(e0);
        PresetEquation e1 = legacyWrapper(p1, "someID");
        Assertions.assertEquals("1+1", e1.getExpression());
        Assertions.assertEquals("1+1", e1.getOriginal());
        PresetEquation e2 = legacyWrapper(p1, "someID uselessParam");
        Assertions.assertEquals("1+1", e2.getExpression());
        Assertions.assertEquals("1+1", e2.getOriginal());
        PresetEquation e3 = legacyWrapper(p1, "IMMEDIATE_UP_OK 1 ");
        Assertions.assertNotEquals(e3.getExpression(), e3.getOriginal());
        PresetEquation e4 = legacyWrapper(p1, "IMMEDIATE_UP_OK 1 2 3");
        Assertions.assertNotEquals(e4.getExpression(), e4.getOriginal());
        Assertions.assertEquals(e3.getOriginal(), e4.getOriginal());
        //also params are now evaluated alter
        Assertions.assertEquals(e3.getExpression(), e4.getExpression());
    }

    private static PresetEquation legacyWrapper(PresetEquationsManager p, String params) {
        PresetEquationDefinition pp = p.getFromCommandString(params);
        if (pp == null) {
            return null;
        }
        String[] sparams = params.trim().split("\\s+");
        return new PresetEquation(pp.getConnectedSingleExpression(), Arrays.copyOfRange(sparams, 1, sparams.length));
    }

    @Test
    public void noDupes() throws IOException, URISyntaxException {
        final PresetEquationsManager p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"1+1\"]}]}]");
        List<String> ids = p1.getIds();
        Assertions.assertTrue(ids.size() == new HashSet<>(ids).size());
        Assertions.assertTrue(ids.size() > 5);
        for (String id1 : ids) {
            PresetEquation e1 = legacyWrapper(p1, id1 + " 2 5 5 5 5");
            for (String id2 : ids) {
                PresetEquation e2 = legacyWrapper(p1, id2 + " 2 5 5 5 5");
                if (!id1.equals(id2)) {
                    Assertions.assertNotEquals(e1.getExpression(), e2.getExpression(), id1+ " and " + id2 + " have same equation!");
                    Assertions.assertNotEquals(e1.getOriginal(), e2.getOriginal(), id1+ " and " + id2 + " have same equation!");
                } else {
                    Assertions.assertEquals(e1.getExpression(), e2.getExpression(), id1+ " and " + id2 + " have NOT same equation!");
                    Assertions.assertEquals(e1.getOriginal(), e2.getOriginal(), id1+ " and " + id2 + " have NOT same equation!");
                }
            }
        }
    }

    @Test
    public void buggyIsCought() throws IOException, URISyntaxException {
        PresetEquationsManager p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"blah=/*1*/; avg(blah\"]}]}]"); //missing bracket
        StringBuilder sbOne = new StringBuilder();
        PresetEquation e = legacyWrapper(p1, "someID 10");
        Assertions.assertNotNull(e);
        evaluate(null, sbOne, e);
        checkError(sbOne);

        p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"/*1*/+/*2*/\"]}]}]"); //unexpanded /*2*/
        sbOne = new StringBuilder();
        e = legacyWrapper(p1, "someID 10");
        Assertions.assertNotNull(e);
        evaluate(null, sbOne, e);
        checkError(sbOne);

        p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"blah=/*1*/; avg(blah)\"]}]}]"); //unexpanded /**/ in variable
        sbOne = new StringBuilder();
        e = legacyWrapper(p1, "someID");
        Assertions.assertNotNull(e);
        Exception ex = null;
        try {
            evaluate(null, sbOne, e);
        } catch (Exception eex) {
            ex = eex;
        }
        Assertions.assertNotNull(ex);
    }

    @Test
    public void allValuates() throws IOException, URISyntaxException {
        final PresetEquationsManager p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"1+1\"]}]}]");
        List<String> ids = p1.getIds();
        Assertions.assertTrue(ids.size() > 5);
        StringBuilder sbAll = new StringBuilder();
        for (String id : ids) {
            System.out.println(id);
            String setup = id + " 2 5 5 5 5 7";
            StringBuilder sbOne = new StringBuilder();
            PresetEquationDefinition preset = p1.getFromCommandString(setup);
            IncrementalSequentialEvaluator exs = preset.getExpressions();
            evaluateNw(sbAll, sbOne, exs, PresetEquationsManager.getParamsFromParams(setup));
            checkNoError(sbOne);
        }
        checkNoError(sbAll);
    }

    private boolean evaluateNw(StringBuilder sbAll, StringBuilder sbOne, IncrementalSequentialEvaluator e, String[] params) {
        return e.evaluate(Arrays.asList("10", "10", "10", "10", "10", "10"), params, new ExpressionLogger() {
            @Override
            public void log(String s) {
                if (sbAll != null) {
                    sbAll.append(s).append("\n");
                }
                sbOne.append(s).append("\n");
            }
        });
    }

    private boolean evaluate(StringBuilder sbAll, StringBuilder sbOne, PresetEquation e) {
        ExpandingExpressionParser lep = new ExpandingExpressionParser(e.getExpression(), Arrays.asList("10", "10", "10", "10"), new ExpressionLogger() {
            @Override
            public void log(String s) {
                if (sbAll != null) {
                    sbAll.append(s).append("\n");
                }
                sbOne.append(s).append("\n");
            }
        });
        return lep.evaluate();
    }

    private void checkNoError(StringBuilder sbOne) {
        Assertions.assertFalse(sbOne.toString().toLowerCase().contains("error"));
        Assertions.assertFalse(sbOne.toString().toLowerCase().contains("fail"));
        Assertions.assertFalse(sbOne.toString().toLowerCase().contains("exception"));
    }

    private void checkError(StringBuilder sbOne) {
        Assertions.assertTrue(
                sbOne.toString().toLowerCase().contains("error")
                        || sbOne.toString().toLowerCase().contains("fail")
                        || sbOne.toString().toLowerCase().contains("exception"));
    }
}