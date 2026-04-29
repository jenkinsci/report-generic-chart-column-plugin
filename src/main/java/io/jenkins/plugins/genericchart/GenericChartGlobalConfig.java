package io.jenkins.plugins.genericchart;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.util.FormValidation;
import io.jenkins.plugins.genericchart.equations.PresetEquationsManager;
import jenkins.model.GlobalConfiguration;

@Extension
public class GenericChartGlobalConfig extends GlobalConfiguration {
    private static Logger logger = Logger.getLogger(GenericChartGlobalConfig.class.getName());

    String additionalFilesToCopy;
    String targetFolders;
    String additionalPresetEquationsJsonUrl;

    public static GenericChartGlobalConfig getInstance() {
        return GlobalConfiguration.all().get(GenericChartGlobalConfig.class);
    }

    public String getAdditionalFilesToCopy() {
        return additionalFilesToCopy;
    }

    @DataBoundSetter
    public void setAdditionalFilesToCopy(String additionalFilesToCopy) {
        this.additionalFilesToCopy = additionalFilesToCopy;
    }

    public String getTargetFolders() {
        return targetFolders;
    }

    @DataBoundSetter
    public void setTargetFolders(String targetFolders) {
        this.targetFolders = targetFolders;
    }

    public String getAdditionalPresetEquationsJsonUrl() {
        return additionalPresetEquationsJsonUrl;
    }

    @DataBoundSetter
    public void setAdditionalPresetEquationsJsonUrl(String additionalPresetEquationsJsonUrl) {
        PresetEquationsManager.resetCached();
        this.additionalPresetEquationsJsonUrl = additionalPresetEquationsJsonUrl;
    }

    public FormValidation doCheckAdditionalPresetEquationsJsonUrl(@QueryParameter String value) {
        if (value == null || value.trim().isEmpty()) {
            return FormValidation.ok();
        }

        String trimmed = value.trim();
        
        // Check if it's JSON (starts with [ or {) or multi-line text
        if (trimmed.startsWith("[") || trimmed.startsWith("{") || value.split("\n").length > 1) {
            // Validate JSON syntax
            try {
                JsonParser.parseString(value);
                return FormValidation.ok("Valid JSON content");
            } catch (JsonSyntaxException e) {
                return FormValidation.error("Invalid JSON syntax: " + e.getMessage());
            }
        } else {
            // It's a URL - validate it
            try {
                URI uri = new URI(trimmed);
                URL url = uri.toURL();
                
                // Special handling for file:// URLs
                if ("file".equalsIgnoreCase(url.getProtocol())) {
                    File file = new File(uri.getPath());
                    if (!file.exists()) {
                        return FormValidation.warning("File does not exist: " + file.getAbsolutePath());
                    }
                    if (!file.canRead()) {
                        return FormValidation.warning("File is not readable: " + file.getAbsolutePath());
                    }
                    if (file.isDirectory()) {
                        return FormValidation.warning("File is directory: " + file.getAbsolutePath());
                    }
                    return FormValidation.ok("Valid file URL (file exists)");
                }
                
                // For http/https URLs, just validate the URL format
                if ("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol())) {
                    return FormValidation.ok("Valid URL");
                }
                
                return FormValidation.ok("Valid URL with protocol: " + url.getProtocol());
                
            } catch (URISyntaxException e) {
                return FormValidation.error("Invalid URL syntax: " + e.getMessage());
            } catch (MalformedURLException e) {
                return FormValidation.error("Malformed URL: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                return FormValidation.error("Invalid URL: " + e.getMessage());
            }
        }
    }

    @DataBoundConstructor
    public GenericChartGlobalConfig() {
        load();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return super.configure(req, json);
    }
}