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
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.jenkins.plugins.genericchart.regenerate.DirArgs;
import io.jenkins.plugins.genericchart.regenerate.PlaintextWriter;
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
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException {
        // Print global configuration values
        GenericChartGlobalConfig globalConfig = GenericChartGlobalConfig.getInstance();
        if (globalConfig != null) {
            listener.getLogger().println("=== Generic Chart Global Configuration ===");
            String additionalFiles = globalConfig.getAdditionalFilesToCopy();
            String targetFolders = globalConfig.getTargetFolders();
            String additionalPresetEquations = globalConfig.getAdditionalPresetEquationsJsonUrl();
            
            listener.getLogger().println("Additional files to copy: " +
                (additionalFiles != null && !additionalFiles.trim().isEmpty() ? additionalFiles : "(not set)"));
            listener.getLogger().println("Target folders: " +
                (targetFolders != null && !targetFolders.trim().isEmpty() ? targetFolders : "(not set)"));
            listener.getLogger().println("Additional preset equations JSON/URL: " +
                (additionalPresetEquations != null && !additionalPresetEquations.trim().isEmpty() ? additionalPresetEquations : "(not set)"));
            listener.getLogger().println("==========================================");
        }
        
        GenericChartProjectAction chrs = new GenericChartProjectAction(build.getProject(), charts);
        List<ReportChart> chartsWithEquations = new ArrayList<>();
        for (ReportChart chart : chrs.getCharts()) {
            if (chart.getUnstableCondition() != null && !chart.getUnstableCondition().trim().isBlank()) {
                chartsWithEquations.add(chart);
            }
        }
        if  (chartsWithEquations.isEmpty()) {
            listener.getLogger().println("No equation definitions found. Not touching result from generic chart report plugin.");
            return true;
        }
        listener.getLogger().println("Performance Report by generic chart report plugin:");
        //job.getDuration() is set once job finishes (so does getTime...)
        long duration =  System.currentTimeMillis() - build.getStartTimeInMillis();
        int failures = 0;
        int chartCounter = 0;
        try(PlaintextWriter out = new PlaintextWriter(build.getRootDir())) {
            out.writeHeader(build.getProject().getName(), build.getDisplayName(), build.getNumber(), Jenkins.get().getRootUrl(), build.getStartTimeInMillis(), duration);
            out.introductionChartsCount(chartsWithEquations.size(),  build.getNumber(), build.getDisplayName(), build.getProject().getName());
            for (ReportChart chart : chartsWithEquations) {
                chartCounter++;
                try {
                    out.singleChartTitle(chart.toLoadedChart(), chartCounter, chartsWithEquations.size());
                    List<ChartPoint> points = chart.getPoints();
                    //the points are returned as first = oldest = 0, last == current == newest == N.
                    //to prevent constant recalculations, lets revert it, so 0 is latest (as notations of L in help-unstableCondition.html says
                    //we revert already ehre to sync nice outputs with values
                    Collections.reverse(points);
                    ExpressionLogger dualOutputController = s -> {
                        listener.getLogger().println(s);
                        out.println(s);
                    };
                    out.allUsedPastBuilds(points, dualOutputController, true, chart.getKey(), chart.getFileGlob());
                    if (out.calcSingleChartAndResolve(chart.toLoadedChart(), points, dualOutputController)) {
                        build.setResult(Result.UNSTABLE);
                        failures++;
                    }

                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            out.closeAllCharts(failures, build.getDisplayName(), build.getNumber(), build.getProject().getName(), s -> listener.getLogger().println(s));
            out.footer(build.getProject().getName(), build.getDisplayName(), build.getNumber(), build.getStartTimeInMillis(), Jenkins.get().getRootUrl());
        }
        DirArgs.export(build.getRootDir().toPath(), new DirArgs(/*fixme, repalce by implementation reading jenkins config*/), build.getDisplayName(), build.getNumber(), build.getProject().getName());
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
            return Jenkins.get().getDescriptorList(ChartModel.class);
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
