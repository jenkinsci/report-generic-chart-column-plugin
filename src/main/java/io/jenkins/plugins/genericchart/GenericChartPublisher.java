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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.jenkins.plugins.genericchart.regenerate.DirArgs;
import io.jenkins.plugins.genericchart.regenerate.PlaintextWriter;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import parser.logical.ExpressionLogger;

public class GenericChartPublisher extends Recorder implements SimpleBuildStep {

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
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {
        performInternal(run, listener);
    }

    @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException {
        performInternal(build, listener);
        return true;
    }

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"}, justification = " npe of spotbugs sucks")
    private void performInternal(Run<?, ?> run, TaskListener listener) throws IOException {
        Job<?, ?> job = run.getParent();
        
        // Add or update the GenericChartProjectAction to the job
        // This ensures the chart is visible on the job's main page for Pipeline jobs
        // Do this FIRST before any early returns
        synchronized (job) {
            //Mandatory for pipeline-like not working in freestyle-like ones
            try {
                // Always add a new action with the current charts configuration.
                job.replaceAction(new GenericChartProjectAction(job, charts));
            } catch (Throwable e){
                listener.getLogger().println("[Generic Chart Plugin] Failed to register chart action: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        GenericChartGlobalConfig globalConfig = GenericChartGlobalConfig.getInstance();
        String additionalFiles = null;
        String targetFolders = null;
        String additionalPresetEquations = null;
        if (globalConfig != null) {
            additionalFiles = globalConfig.getAdditionalFilesToCopy();
            targetFolders = globalConfig.getTargetFolders();
            additionalPresetEquations = globalConfig.getAdditionalPresetEquationsJsonUrl();
        }
        
        GenericChartProjectAction chrs = new GenericChartProjectAction(job, charts);
        List<ReportChart> chartsWithEquations = new ArrayList<>();
        for (ReportChart chart : chrs.getCharts()) {
            if (chart.getUnstableCondition() != null && !chart.getUnstableCondition().trim().isBlank()) {
                chartsWithEquations.add(chart);
            }
        }
        if  (chartsWithEquations.isEmpty()) {
            listener.getLogger().println("No equation definitions found. Not touching result from generic chart report plugin.");
            return;
        }
        listener.getLogger().println("Performance Report by generic chart report plugin:");
        //job.getDuration() is set once job finishes (so does getTime...)
        long duration =  System.currentTimeMillis() - run.getStartTimeInMillis();
        int failures = 0;
        int chartCounter = 0;
        try(PlaintextWriter out = new PlaintextWriter(run.getRootDir())) {
            out.writeHeader(job.getName(), run.getDisplayName(), run.getNumber(), Jenkins.get().getRootUrl(), run.getStartTimeInMillis(), duration);
            out.introductionChartsCount(chartsWithEquations.size(),  run.getNumber(), run.getDisplayName(), job.getName());
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
                    if (out.calcSingleChartAndResolve(chart.toLoadedChart(), points, dualOutputController, additionalPresetEquations )) {
                        run.setResult(Result.UNSTABLE);
                        failures++;
                    }

                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            out.closeAllCharts(failures, run.getDisplayName(), run.getNumber(), job.getName(), s -> listener.getLogger().println(s));
            out.footer(job.getName(), run.getDisplayName(), run.getNumber(), run.getStartTimeInMillis(), Jenkins.get().getRootUrl());
        }
        DirArgs.export(
                run.getRootDir().toPath(),
                new GenericChartPublisherDirArgs(targetFolders, additionalFiles),
                run.getDisplayName(),
                run.getNumber(),
                job.getName(),
                run.getResult() == null?"UNKNOWN":run.getResult().toString());
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return getProjectActions((Job<?, ?>) project);
    }

    // For pipeline support - SimpleBuildStep.getProjectActions
    public Collection<? extends Action> getProjectActions(Job<?, ?> job) {
        synchronized (job) {
            if (/* getAction(Class) produces a StackOverflowError */!Util.filter(job.getActions(), GenericChartProjectAction.class).isEmpty()) {
                // JENKINS-26077: someone like XUnitPublisher already added one
                return Collections.emptySet();
            }
            return Collections.singleton(new GenericChartProjectAction(job, charts));
        }
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

    @Symbol("genericChartPublisher")
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
