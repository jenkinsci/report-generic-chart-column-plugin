package io.jenkins.plugins.genericchart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.genericchart.equations.IncrementalSequentialEvaluator;
import io.jenkins.plugins.genericchart.equations.PresetEquation;
import io.jenkins.plugins.genericchart.equations.PresetEquationDefinition;
import io.jenkins.plugins.genericchart.equations.PresetEquationsManager;
import parser.expanding.ExpandingExpressionParser;
import parser.logical.ExpressionLogger;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

class PresetEquationsManagerTest {

    @BeforeEach
    void cleanCaches(){
        PresetEquationsManager.resetCached();
    }

    @Test
    void listTest() throws IOException, URISyntaxException {
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
        assertEquals(listing1, listing2); //cahe was not reload

        PresetEquationsManager.resetCached();
        baos2 = new ByteArrayOutputStream();
        p2 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"1+1\"]}]}]");
        try (PrintStream ps = new PrintStream(baos2, true, StandardCharsets.UTF_8)) {
            p2.print(ps);
        }
         listing2 = baos2.toString(StandardCharsets.UTF_8);
         assertNotEquals(listing1, listing2);
    }

    @Test
    void getTest() throws IOException, URISyntaxException {
        final PresetEquationsManager p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"1+1\"]}]}]");
        PresetEquation e0 = legacyWrapper(p1, "weird_id weird_params");
        assertNull(e0);
        PresetEquation e1 = legacyWrapper(p1, "someID");
        assertEquals("1+1", e1.getExpression());
        assertEquals("1+1", e1.getOriginal());
        PresetEquation e2 = legacyWrapper(p1, "someID uselessParam");
        assertEquals("1+1", e2.getExpression());
        assertEquals("1+1", e2.getOriginal());
        PresetEquation e3 = legacyWrapper(p1, "IMMEDIATE_UP_OK 1 ");
        assertNotEquals(e3.getExpression(), e3.getOriginal());
        PresetEquation e4 = legacyWrapper(p1, "IMMEDIATE_UP_OK 1 2 3");
        assertNotEquals(e4.getExpression(), e4.getOriginal());
        assertEquals(e3.getOriginal(), e4.getOriginal());
        //also params are now evaluated alter
        assertEquals(e3.getExpression(), e4.getExpression());
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
    void noDupes() throws IOException, URISyntaxException {
        final PresetEquationsManager p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"1+1\"]}]}]");
        List<String> ids = p1.getIds();
        assertEquals(ids.size(), new HashSet<>(ids).size());
        assertTrue(ids.size() > 5);
        for (String id1 : ids) {
            PresetEquation e1 = legacyWrapper(p1, id1 + " 2 5 5 5 5");
            for (String id2 : ids) {
                PresetEquation e2 = legacyWrapper(p1, id2 + " 2 5 5 5 5");
                if (!id1.equals(id2)) {
                    assertNotEquals(e1.getExpression(), e2.getExpression(), id1+ " and " + id2 + " have same equation!");
                    assertNotEquals(e1.getOriginal(), e2.getOriginal(), id1+ " and " + id2 + " have same equation!");
                } else {
                    assertEquals(e1.getExpression(), e2.getExpression(), id1+ " and " + id2 + " have NOT same equation!");
                    assertEquals(e1.getOriginal(), e2.getOriginal(), id1+ " and " + id2 + " have NOT same equation!");
                }
            }
        }
    }

    @Test
    void buggyIsCought() throws IOException, URISyntaxException {
        PresetEquationsManager p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"blah=/*1*/; avg(blah\"]}]}]"); //missing bracket
        StringBuilder sbOne = new StringBuilder();
        PresetEquation e = legacyWrapper(p1, "someID 10");
        assertNotNull(e);
        evaluate(null, sbOne, e);
        checkError(sbOne);

        p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"/*1*/+/*2*/\"]}]}]"); //unexpanded /*2*/
        sbOne = new StringBuilder();
        e = legacyWrapper(p1, "someID 10");
        assertNotNull(e);
        evaluate(null, sbOne, e);
        checkError(sbOne);

        p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"blah=/*1*/; avg(blah)\"]}]}]"); //unexpanded /**/ in variable
        sbOne = new StringBuilder();
        e = legacyWrapper(p1, "someID");
        assertNotNull(e);
        Exception ex = null;
        try {
            evaluate(null, sbOne, e);
        } catch (Exception eex) {
            ex = eex;
        }
        assertNotNull(ex);
    }

    @Test
    void descriptionsAreParsed() throws IOException, URISyntaxException {
        final PresetEquationsManager p1 = new PresetEquationsManager("""
                [
                  {
                    "id": "withDescriptions",
                    "comments": ["some comment"],
                    "equations": [
                      {
                        "name": "main1",
                        "equation": ["/*1*/+1"],
                        "descriptions": [
                        {
                            "condition": "true",
                            "description": ["this is title"]
                          },
                          {
                            "condition": "/*RESULT*/ == 2",
                            "description": ["ok"]
                          },
                          {
                            "condition": "/*RESULT*/ != 2",
                            "description": ["fail"]
                          }
                        ]
                      },
                      {
                        "name": "main2",
                        "equation": ["2+2"]
                      }
                    ]
                  }
                ]
                """);
        PresetEquationDefinition preset = p1.getFromCommandString("withDescriptions 1");
        assertNotNull(preset);
        assertEquals(2, preset.getEquations().size());

        assertEquals("main1", preset.getEquations().get(0).getName());
        assertEquals(3, preset.getEquations().get(0).getDescriptions().size());
        assertEquals("true", preset.getEquations().get(0).getDescriptions().get(0).getCondition());
        assertEquals(Arrays.asList("this is title"), preset.getEquations().get(0).getDescriptions().get(0).getDescriptionLines());
        assertEquals("/*RESULT*/ == 2", preset.getEquations().get(0).getDescriptions().get(1).getCondition());
        assertEquals(Arrays.asList("ok"), preset.getEquations().get(0).getDescriptions().get(1).getDescriptionLines());
        assertEquals("/*RESULT*/ != 2", preset.getEquations().get(0).getDescriptions().get(2).getCondition());
        assertEquals(Arrays.asList("fail"), preset.getEquations().get(0).getDescriptions().get(2).getDescriptionLines());

        assertEquals("main2", preset.getEquations().get(1).getName());
        assertTrue(preset.getEquations().get(1).getDescriptions().isEmpty());
    }

    @Test
    void allValuates() throws IOException, URISyntaxException {
        final PresetEquationsManager p1 = new PresetEquationsManager("[{\"id\":\"someID\",\"comments\":[\"some comment\"],\"equations\":[{\"name\":\"main\",\"equation\":[\"1+1\"]}]}]");
        List<String> ids = p1.getIds();
        assertTrue(ids.size() > 5);
        StringBuilder sbAllCalcs = new StringBuilder();
        StringBuilder sbAllAnswers = new StringBuilder();
        for (String id : ids) {
            System.out.println(id);
            String setup = id + " 2 5 5 5 5 7";
            StringBuilder sbCalcs = new StringBuilder();
            StringBuilder sbAnswers = new StringBuilder();
            PresetEquationDefinition preset = p1.getFromCommandString(setup);
            IncrementalSequentialEvaluator exs = preset.getExpressions();
            evaluateNw(sbCalcs, sbAnswers, exs, PresetEquationsManager.getParamsFromParams(setup));
            checkNoError(sbCalcs);
            sbAllCalcs.append(sbCalcs);
            sbAllAnswers.append(sbAnswers);
        }
        checkNoError(sbAllCalcs);
    }

    private boolean evaluateNw(StringBuilder sbAllCalcs, StringBuilder sbAllAnswers,  IncrementalSequentialEvaluator e, String[] params) throws IOException, URISyntaxException {
        ExpressionLogger els = new ExpressionLogger() {
            @Override
            public void log(String s) {
                    sbAllCalcs.append(s).append("\n");
            }
        };
        ExpressionLogger ans = new ExpressionLogger() {
            @Override
            public void log(String s) {
                    sbAllAnswers.append(s).append("\n");
            }
        };
        return e.evaluate(Arrays.asList("10", "10", "10", "10", "10", "10"), params, els, ans, new PresetEquationsManager(null));
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
        assertFalse(sbOne.toString().toLowerCase().contains("error"));
        assertFalse(sbOne.toString().toLowerCase().contains("fail"));
        assertFalse(sbOne.toString().toLowerCase().contains("exception"));
    }

    private void checkError(StringBuilder sbOne) {
        assertTrue(
                sbOne.toString().toLowerCase().contains("error")
                        || sbOne.toString().toLowerCase().contains("fail")
                        || sbOne.toString().toLowerCase().contains("exception"));
    }
}