package io.jenkins.plugins.genericchart;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

class PresetEquationsManagerTest {

    @Test
    public void listTest() throws IOException {
        final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        final PresetEquationsManager p1 = new PresetEquationsManager();
        try (PrintStream ps = new PrintStream(baos1, true, StandardCharsets.UTF_8)) {
            p1.print(ps);
        }
        String listing1 = baos1.toString(StandardCharsets.UTF_8);

        final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        final PresetEquationsManager p2 = new PresetEquationsManager("# someID\n# some comment\n1+1");
        try (PrintStream ps = new PrintStream(baos2, true, StandardCharsets.UTF_8)) {
            p2.print(ps);
        }
        String listing2 = baos2.toString(StandardCharsets.UTF_8);
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
}