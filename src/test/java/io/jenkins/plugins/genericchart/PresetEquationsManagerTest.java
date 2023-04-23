package io.jenkins.plugins.genericchart;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.expanding.ExpandingExpressionParser;
import parser.logical.ExpressionLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
    public void listTest() throws IOException {
        final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        final PresetEquationsManager p1 = new PresetEquationsManager();
        try (PrintStream ps = new PrintStream(baos1, true, StandardCharsets.UTF_8)) {
            p1.print(ps);
        }
        String listing1 = baos1.toString(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PresetEquationsManager p2 = new PresetEquationsManager("# someID\n# some comment\n1+1");
        try (PrintStream ps = new PrintStream(baos2, true, StandardCharsets.UTF_8)) {
            p2.print(ps);
        }
        String listing2 = baos2.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals(listing1, listing2); //cahe was not reload

        PresetEquationsManager.resetCached();
        baos2 = new ByteArrayOutputStream();
        p2 = new PresetEquationsManager("# someID\n# some comment\n1+1");
        try (PrintStream ps = new PrintStream(baos2, true, StandardCharsets.UTF_8)) {
            p2.print(ps);
        }
         listing2 = baos2.toString(StandardCharsets.UTF_8);
         Assertions.assertNotEquals(listing1, listing2);
    }

    @Test
    public void getTest() throws IOException {
        final PresetEquationsManager p1 = new PresetEquationsManager("# someID\n# some comment\n1+1");
        PresetEquationsManager.PresetEquation e0 = p1.get("weird_id weird_params");
        Assertions.assertNull(e0);
        PresetEquationsManager.PresetEquation e1 = p1.get("someID");
        Assertions.assertEquals("1+1", e1.getExpression());
        Assertions.assertEquals("1+1", e1.getOriginal());
        PresetEquationsManager.PresetEquation e2 = p1.get("someID uselessParam");
        Assertions.assertEquals("1+1", e2.getExpression());
        Assertions.assertEquals("1+1", e2.getOriginal());
        PresetEquationsManager.PresetEquation e3 = p1.get("IMMEDIATE_UP_OK");
        Assertions.assertNotEquals(e3.getExpression(), e3.getOriginal());
        PresetEquationsManager.PresetEquation e4 = p1.get("IMMEDIATE_UP_OK 1 2 3");
        Assertions.assertNotEquals(e4.getExpression(), e4.getOriginal());
        Assertions.assertEquals(e3.getOriginal(), e4.getOriginal());
        Assertions.assertNotEquals(e3.getExpression(), e4.getExpression());
    }

    @Test
    public void noDupes() throws IOException {
        final PresetEquationsManager p1 = new PresetEquationsManager("# someID\n# some comment\n1+1");
        List<String> ids = p1.getIds();
        Assertions.assertTrue(ids.size() == new HashSet<>(ids).size());
        Assertions.assertTrue(ids.size() > 5);
        for (String id1 : ids) {
            PresetEquationsManager.PresetEquation e1 = p1.get(id1 + " 2 5 5 5 5");
            for (String id2 : ids) {
                PresetEquationsManager.PresetEquation e2 = p1.get(id2 + " 2 5 5 5 5");
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
    public void buggyIsCought() throws IOException {
        PresetEquationsManager p1 = new PresetEquationsManager("# someID\n# some comment\nblah=/*1*/; avg(blah"); //missing bracket
        StringBuilder sbOne = new StringBuilder();
        PresetEquationsManager.PresetEquation e = p1.get("someID 10");
        Assertions.assertNotNull(e);
        evaluate(null, sbOne, e);
        checkError(sbOne);

        p1 = new PresetEquationsManager("# someID\n# some comment\n/*1*/+/*2*/"); //unexpanded /*2*/
        sbOne = new StringBuilder();
        e = p1.get("someID 10");
        Assertions.assertNotNull(e);
        evaluate(null, sbOne, e);
        checkError(sbOne);

        p1 = new PresetEquationsManager("# someID\n# some comment\nblah=/*1*/; avg(blah)"); //unexpanded /**/ in variable
        sbOne = new StringBuilder();
        e = p1.get("someID");
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
    public void allValuates() throws IOException {
        final PresetEquationsManager p1 = new PresetEquationsManager("# someID\n# some comment\n1+1");
        List<String> ids = p1.getIds();
        Assertions.assertTrue(ids.size() > 5);
        StringBuilder sbAll = new StringBuilder();
        for (String id : ids) {
            StringBuilder sbOne = new StringBuilder();
            PresetEquationsManager.PresetEquation e = p1.get(id + " 2 5 5 5 5");
            Assertions.assertNotNull(e);
            evaluate(sbAll, sbOne, e);
            checkNoError(sbOne);
        }
        checkNoError(sbAll);
    }

    private boolean evaluate(StringBuilder sbAll, StringBuilder sbOne, PresetEquationsManager.PresetEquation e) {
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