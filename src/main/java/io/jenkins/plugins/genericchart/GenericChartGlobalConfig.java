package io.jenkins.plugins.genericchart;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;

@Extension
public class GenericChartGlobalConfig extends GlobalConfiguration {
    private static Logger logger = Logger.getLogger(GenericChartGlobalConfig.class.getName());

    String customEmbeddedFunctions;

    public static GenericChartGlobalConfig getInstance() {
        return GlobalConfiguration.all().get(GenericChartGlobalConfig.class);
    }

    public boolean isDiffToolUrlSet() {
        return customEmbeddedFunctions != null && !customEmbeddedFunctions.trim().isEmpty();
    }

    public String getCustomEmbeddedFunctions() {
        return customEmbeddedFunctions;
    }

    @DataBoundSetter
    public void setCustomEmbeddedFunctions(String customEmbeddedFunctions) {
        PresetEquationsManager.resetCached();
        this.customEmbeddedFunctions = customEmbeddedFunctions;
    }

    @DataBoundConstructor
    public GenericChartGlobalConfig(String diffToolUrl) {
        this.customEmbeddedFunctions = diffToolUrl;
    }

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