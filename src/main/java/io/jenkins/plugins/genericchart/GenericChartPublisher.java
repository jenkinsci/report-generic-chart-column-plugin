/*
 * The MIT License
 *
 * Copyright 2016 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.genericchart;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import parser.expanding.ExpandingExpressionParser;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import parser.logical.ExpressionLogger;

public class GenericChartPublisher extends Publisher {

    private List<ChartModel> charts;

    @DataBoundConstructor
    public GenericChartPublisher(List<ChartModel> charts) {
        this.charts = charts;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        for (ReportChart chart : new GenericChartProjectAction(build.getProject(), charts).getCharts()) {
            try {
                if (chart.getUnstableCondition() != null && !chart.getUnstableCondition().trim().isEmpty()) {
                    String equation = chart.getUnstableCondition().trim();
                    PresetEquationsManager presets = new PresetEquationsManager(GenericChartGlobalConfig.getInstance().getCustomEmbeddedFunctions());
                    if (equation.trim().equals("LIST_INTERNALS")) {
                        presets.print(listener.getLogger());
                        equation = "Internal expressions printed";
                    } else {
                        PresetEquationsManager.PresetEquation isPreset = presets.get(equation);
                        if (isPreset != null) {
                            listener.getLogger().println(equation + " found as preset queue:");
                            listener.getLogger().println(isPreset.getOriginal());
                            equation = isPreset.getExpression();
                        }
                    }
                    List<ChartPoint> points = chart.getPoints();
                    List<String> pointsValues = points.stream().map(a -> a.getValue()).collect(Collectors.toList());
                    //the points are returned as first = oldest = 0, last == current == newest == N.
                    //to prevent constant recalculations, lets revert it, so 0 is latest (as notations of L in help-unstableCondition.html says
                    Collections.reverse(pointsValues);
                    ExpandingExpressionParser lep = new ExpandingExpressionParser(equation, pointsValues, new ExpressionLogger() {
                        @Override
                        public void log(String s) {
                            listener.getLogger().println(s);
                        }
                    });
                    if (lep.evaluate()) {
                        build.setResult(Result.UNSTABLE);
                        return true; //you can not go back, nothing is going worse here, so lets quit
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        if (/* getAction(Class) produces a StackOverflowError */!Util.filter(
                        project.getActions(), GenericChartProjectAction.class).isEmpty()) {
            // JENKINS-26077: someone like XUnitPublisher already added one
            return Collections.emptySet();
        }
        return Collections.singleton(new GenericChartProjectAction(project, charts));
    }

    public List<ChartModel> getCharts() {
        return charts;
    }

    @DataBoundSetter
    public void setCharts(List<ChartModel> charts) {
        this.charts = charts;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public List<ChartModel.ChartDescriptor> getItemDescriptors() {
            return Jenkins.getInstance().getDescriptorList(ChartModel.class);
        }

        @Override
        public String getDisplayName() {
            return "Charts from properties";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }

}
