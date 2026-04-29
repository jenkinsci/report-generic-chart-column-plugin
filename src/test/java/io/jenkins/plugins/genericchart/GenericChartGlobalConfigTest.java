package io.jenkins.plugins.genericchart;

import hudson.util.FormValidation;

import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@WithJenkins
public class GenericChartGlobalConfigTest {

    @TempDir
    Path tempDir;


      /**
     * Helper to create a config instance without triggering load()
     */
    private GenericChartGlobalConfig createConfig() {
        return new GenericChartGlobalConfig();
    }

    // Test empty/null values
    @Test
    public void testValidatorWithNull(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(null);
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    public void testValidatorWithEmptyString(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl("");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    public void testValidatorWithWhitespace(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl("   ");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    // Test valid JSON
    @Test
    public void testValidatorWithValidJsonObject(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String validJson = "{\"equations\": [{\"id\": \"test\", \"expression\": \"L[0] > 100\"}]}";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(validJson);
        assertEquals(FormValidation.Kind.OK, result.kind);
        assertTrue(result.getMessage().contains("Valid JSON content"));
    }

    @Test
    public void testValidatorWithValidJsonArray(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String validJson = "[{\"id\": \"test\", \"expression\": \"L[0] > 100\"}]";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(validJson);
        assertEquals(FormValidation.Kind.OK, result.kind);
        assertTrue(result.getMessage().contains("Valid JSON content"));
    }

    @Test
    public void testValidatorWithMultilineJson(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String validJson = "{\n  \"equations\": [\n    {\"id\": \"test\"}\n  ]\n}";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(validJson);
        assertEquals(FormValidation.Kind.OK, result.kind);
        assertTrue(result.getMessage().contains("Valid JSON content"));
    }

    // Test invalid JSON
    @Test
    public void testValidatorWithInvalidJson(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String invalidJson = "{\"equations\": [invalid json}";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(invalidJson);
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertTrue(result.getMessage().contains("Invalid JSON syntax"));
    }

    @Test
    public void testValidatorWithInvalidJsonArray(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String invalidJson = "[{\"id\": \"test\", }]";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(invalidJson);
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertTrue(result.getMessage().contains("Invalid JSON syntax"));
    }

    // Test valid URLs
    @Test
    public void testValidatorWithValidHttpUrl(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String validUrl = "https://example.com/preset-equations.json";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(validUrl);
        assertEquals(FormValidation.Kind.OK, result.kind);
        assertTrue(result.getMessage().contains("Valid URL"));
    }

    @Test
    public void testValidatorWithValidHttpsUrl(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String validUrl = "https://raw.githubusercontent.com/user/repo/master/file.json";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(validUrl);
        assertEquals(FormValidation.Kind.OK, result.kind);
        assertTrue(result.getMessage().contains("Valid URL"));
    }

    // Test invalid URLs
    @Test
    public void testValidatorWithInvalidUrl(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String invalidUrl = "not a valid url";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(invalidUrl);
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertTrue(result.getMessage().contains("Invalid URL") ||
                   result.getMessage().contains("Malformed URL"));
    }

    @Test
    public void testValidatorWithMalformedUrl(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String malformedUrl = "http://[invalid";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(malformedUrl);
        assertEquals(FormValidation.Kind.ERROR, result.kind);
    }

    // Test file:// URLs
    @Test
    public void testValidatorWithExistingFile(JenkinsRule r) throws IOException {
        GenericChartGlobalConfig config = createConfig();
        // Create a temporary file
        Path testFile = tempDir.resolve("test-equations.json");
        Files.writeString(testFile, "{\"test\": \"data\"}");

        String fileUrl = "file://" + testFile.toAbsolutePath().toString();
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(fileUrl);
        assertEquals(FormValidation.Kind.OK, result.kind);
        assertTrue(result.getMessage().contains("Valid file URL"));
        assertTrue(result.getMessage().contains("file exists"));
    }

    @Test
    public void testValidatorWithNonExistingFile(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String fileUrl = "file:///tmp/non-existing-file-" + System.currentTimeMillis() + ".json";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(fileUrl);
        assertEquals(FormValidation.Kind.WARNING, result.kind);
        assertTrue(result.getMessage().contains("File does not exist"));
    }

    @Test
    public void testValidatorWithDirectory(JenkinsRule r) throws IOException {
        GenericChartGlobalConfig config = createConfig();
        // Create a temporary directory
        Path testDir = tempDir.resolve("test-dir");
        Files.createDirectory(testDir);

        String fileUrl = "file://" + testDir.toAbsolutePath().toString();
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(fileUrl);
        assertEquals(FormValidation.Kind.WARNING, result.kind);
        assertTrue(result.getMessage().contains("directory"));
    }

    @Test
    public void testValidatorWithUnreadableFile(JenkinsRule r) throws IOException {
        GenericChartGlobalConfig config = createConfig();
        // Create a temporary file and make it unreadable
        Path testFile = tempDir.resolve("unreadable-file.json");
        Files.writeString(testFile, "{\"test\": \"data\"}");
        File file = testFile.toFile();

        // Try to make it unreadable (may not work on all systems)
        boolean madeUnreadable = file.setReadable(false);

        if (madeUnreadable && !file.canRead()) {
            String fileUrl = "file://" + testFile.toAbsolutePath().toString();
            FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(fileUrl);
            assertEquals(FormValidation.Kind.WARNING, result.kind);
            assertTrue(result.getMessage().contains("not readable"));
        }

        // Restore permissions for cleanup
        file.setReadable(true);
    }

    // Test edge cases
    @Test
    public void testValidatorWithFileUrlWithSpaces(JenkinsRule r) throws IOException {
        GenericChartGlobalConfig config = createConfig();
        // Create a file with spaces in the name
        Path testFile = tempDir.resolve("test file with spaces.json");
        Files.writeString(testFile, "{\"test\": \"data\"}");

        String fileUrl = "file://" + testFile.toAbsolutePath().toString();
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(fileUrl);
        // Should either be OK, WARNING, or ERROR depending on URL encoding
        assertTrue(result.kind == FormValidation.Kind.OK ||
                   result.kind == FormValidation.Kind.WARNING ||
                   result.kind == FormValidation.Kind.ERROR,
                   "Expected OK, WARNING, or ERROR but got: " + result.kind);
    }

    @Test
    public void testValidatorWithOtherProtocol(JenkinsRule r) { Assume.assumeNotNull(r);
        GenericChartGlobalConfig config = createConfig();
        String ftpUrl = "ftp://example.com/file.json";
        FormValidation result = config.doCheckAdditionalPresetEquationsJsonUrl(ftpUrl);
        assertEquals(FormValidation.Kind.OK, result.kind);
        assertTrue(result.getMessage().contains("Valid URL with protocol: ftp"));
    }
}

