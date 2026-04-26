/*
 * The MIT License
 *
 * Copyright 2026 user.
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
package io.jenkins.plugins.genericchart.regenerate;


import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.genericchart.ChartPoint;
import io.jenkins.plugins.genericchart.ChartUtil;
import io.jenkins.plugins.genericchart.equations.IncrementalSequentialEvaluator;
import io.jenkins.plugins.genericchart.equations.PresetEquationDefinition;
import io.jenkins.plugins.genericchart.equations.PresetEquationsManager;
import parser.logical.ExpressionLogger;

public class Main {

    public static void main(String[] args) throws Exception {
        //must be run in builds to list all files, or in build dir
        //main thing is job/config.xml, which contains the equations
        System.err.println("You can use " +  ChartUtil.log_comments.toLowerCase()+ " lower case property or uppercase variable " + ChartUtil.log_comments.toUpperCase() + " to print also equations definitions");
        System.err.println("You can use " +  ChartUtil.log_equation.toLowerCase()+ " lower case property or uppercase variable " + ChartUtil.log_equation.toUpperCase() + "to print also equation steps");
        System.err.println("You can use " +  ChartUtil.PRESET_DEFS.toLowerCase()+ " lower case property or uppercase variable " + ChartUtil.PRESET_DEFS.toUpperCase() + "to load another file/url or custom json definitions of preset definitions");
        new Main().work();
    }

    void work() throws Exception {
        if (ReportSummaryUtil.isBuildDir(new File("."))) {
            recreateOneJob(new File(".").getCanonicalFile().toPath());
        } else {
            Path cwd = Paths.get(".").toAbsolutePath().normalize();
            if (cwd.resolve("builds").toFile().exists()) {
                cwd = cwd.resolve("builds");
            }
            try (Stream<Path> dirsStream = Files.list(cwd)) {
                dirsStream.sequential().filter(d -> !Files.isSymbolicLink(d)).forEach(this::recreateOneJob);
            }
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "buildPath claims to have NPE, but I'm failing to see it")
    private void recreateOneJob(Path buildPath) {
        if (buildPath == null) {
            System.err.println("null build path");
            return;
        }
        if (!ReportSummaryUtil.isBuildDir(buildPath.toFile())) {
            System.err.println("Not a build dir: " + buildPath.toString());
            return;
        }
        File config = ReportSummaryUtil.getConfig(buildPath.toFile());
        if (config == null) {
            System.err.println("No a config file for: " + buildPath.toString());
            return;
        }
        System.err.println("Processing: " + buildPath);
        String status = ReportSummaryUtil.getBuildResult(buildPath.toFile());
        if (ReportSummaryUtil.SUCCESS_DUPLICATE.equals(status) || ReportSummaryUtil.UNSTABLE_DUPLICATE.equals(status)) {
            String mainJenkinsPkg = ChartPoint.class.getPackageName();
            //callig class directly from GenericChartPublisher  or ChartModel leads to need of jenkins on CP
            NodeList genericChartPublisherCharts = ReportSummaryUtil.findNodeListInXml(config, "/project/publishers/" + mainJenkinsPkg + ".GenericChartPublisher/charts");
            if (genericChartPublisherCharts == null || genericChartPublisherCharts.getLength() == 0) {
                System.err.println("No equations detected in  " + buildPath.toString());
                return;
            }
            List<LoadedChart> loadedCharts = new ArrayList<>();
            for (int i1 = 0; i1 < genericChartPublisherCharts.getLength(); i1++) {
                Node chartModel = genericChartPublisherCharts.item(i1);
                if (chartModel.getNodeType() == Node.ELEMENT_NODE) {
                    Map<String, String> currentChart = new HashMap<>();
                    NodeList records = chartModel.getChildNodes();
                    for (int i2 = 0; i2 < records.getLength(); i2++) {
                        Node record = records.item(i2);
                        if (record.getNodeType() == Node.ELEMENT_NODE) {
                            if (record.getFirstChild() != null) {
                                currentChart.put(record.getNodeName(), record.getFirstChild().getNodeValue());
                            }
                        }
                    }
                    if (currentChart != null && !currentChart.isEmpty()
                            && currentChart.get("unstableCondition") != null && !currentChart.get("unstableCondition").isBlank()
                            && currentChart.get("fileNameGlob") != null && !currentChart.get("fileNameGlob").isBlank()
                            && currentChart.get("key") != null && !currentChart.get("key").isBlank()) {
                        loadedCharts.add(new LoadedChart(currentChart));
                    }
                }
            }
            int buildId = Integer.parseInt(buildPath.getFileName().toString());
            String jobName = buildPath.getParent().getParent().getFileName().toString();
            String displayName = ReportSummaryUtil.getDisplayName(buildPath, buildId);
            long[] times = ReportSummaryUtil.getTimeAndDuration(buildPath);
            //there is all of them. Real limit is then set by individual charts
            List<Integer> dataHistory = new ArrayList<>();
            dataHistory.add(buildId);
            //this is later transformed to displayname=value to match the chart
            dataHistory.addAll(ReportSummaryUtil.getOldBuilds(buildPath, buildId));
            //and also include it to report
            //don't forget the header and footer as in jtreg
            System.out.println(loadedCharts.size() + " valid charts with guarding equation loaded for " + buildId + "/" + displayName + " " + jobName);
            if (loadedCharts.isEmpty()) {
                System.out.println("No performance data found");
                return;
            }
            System.out.println(status + " took " + times[1] + ", started at " + times[0]);
            System.out.println("     have found builds to past  " + dataHistory.size() + ": " + dataHistory.toString());
            int failures = 0;
            int chartCounter = 0;
            for (LoadedChart chart : loadedCharts) {
                chartCounter++;
                List<ChartPoint> oneChartAllData = new ArrayList<>();
                System.out.println("### " + chartCounter + "/" + loadedCharts.size() + " " + chart.getTitleLikeChart());
                int thisChartCounter = 0;
                for (Integer data : dataHistory) {
                    thisChartCounter++;
                    if (thisChartCounter > chart.getLimit()) {
                        break;
                    }
                    File currentHistoryDir = new File((buildPath.toFile().getParentFile()), "" + data);
                    String thisHistoryBuildDisplayName = ReportSummaryUtil.getDisplayName(currentHistoryDir.toPath(), data);
                    String thisHistoryBuildResult = ReportSummaryUtil.getBuildResult(currentHistoryDir);
                    List<ChartPoint> chartPoint = ChartUtil.findPropertiesValues(currentHistoryDir.toPath(), chart.getKey(), chart.getFileNameGlob(), thisHistoryBuildDisplayName, thisHistoryBuildDisplayName, data, chart.getChartColor(), thisHistoryBuildResult);
                    if (chartPoint.isEmpty()) {
                        System.err.println("No data found for " + chart.getKey() + " in " + chart.getFileNameGlob() + " of " + displayName + "/" + buildId);
                    } else {
                        if (chartPoint.size() > 1) {
                            System.err.println("To much data " + chartPoint.size() + " found for " + chart.getKey() + " in " + chart.getFileNameGlob() + " of " + displayName + "/" + buildId);
                            System.err.println("using first");
                        }
                        ChartPoint point = chartPoint.get(0);
                        oneChartAllData.add(point);
                    }
                }
                //the points in CHART logic are returned as first = oldest = 0, last == current == newest == N.
                //to prevent constant recalculations, lets revert it, so 0 is latest (as notations of L in help-unstableCondition.html says
                //however here, in reverse search, they are in correct, expected order
                //Collections.reverse(oneChartAllData) is thus not needed

                //the reverse is missing here just for pretty printing, although printed opposite, it later correctly matches Upon:... as printed in verbose mode
                int i = -1;
                for (ChartPoint chartPoint : oneChartAllData) {
                    i++;
                    System.out.println(chartPoint.getBuildName() + "/" + chartPoint.getBuildNumber() + ": " + chartPoint.getValue() + " " + chartPoint.getResult() + (i == 0 ? " (this)" : ""));
                }
                System.out.println("shortened values (shown reverted, newest->oldest): " + oneChartAllData.stream().map(s -> s.getValue()).collect(Collectors.joining(",")));
                try {
                    boolean thicChartResult = calc(chart, oneChartAllData);
                    if (thicChartResult) {
                        System.out.println("Result of " + chart.getTitleLikeChart() + " is true, that is regression.");
                        failures++;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //there is revert in the plugin before points ar eoeprated. revert?
                //"limit"  - number of points to show
                //"unstableCondition" = equation to solve and to process
                //calculate to file
                //add title  fileNameGlob key chartColor
                //prpend by header as in jtreg
                //prpend by header as in footer
                //limit - the builds must be read to past manually )see jtreg again)
                //maybe reuse jtreg lib?
                //additional files.. like the original data?
                //definitely build.xml? config.xml?
            }
            if (failures == 0) {
                System.out.println("Generic chart report from properties found no regression for " + displayName + "/" + buildId + " in " + jobName);
            } else {
                System.out.println("Generic chart report from properties found " + failures + " regression(s) for " + displayName + "/" + buildId + " in " + jobName);
            }
        } else {
            System.err.println(buildPath.toString() + " is " + status + ", skipping");
        }
    }

    private boolean calc(LoadedChart chartDef, List<ChartPoint> points) throws IOException, URISyntaxException {
        PresetEquationsManager presets = new PresetEquationsManager(ChartUtil.getVarOrProp(ChartUtil.PRESET_DEFS));
        String equationNameOrDef = chartDef.getUnstableCondition();
        PresetEquationDefinition isPreset = presets.getFromCommandString(equationNameOrDef);
        IncrementalSequentialEvaluator expresion;
        if (isPreset != null) {
            System.err.println(equationNameOrDef + " found as preset queue:");
            expresion = isPreset.getExpressions();
        } else {
            expresion = IncrementalSequentialEvaluator.getUserDefIncrementalSequentialEvaluator(equationNameOrDef);
        }
        List<String> replies = new ArrayList<>();
        List<String> pointsValues = points.stream().map(a -> a.getValue()).collect(Collectors.toList());
        ExpressionLogger eloger = s -> {
        };
        //fixme links to the 1.9 r eadme (or new in tip?
        if (ChartUtil.isVarOrProp(ChartUtil.log_equation)) {
            eloger = s -> System.out.println(s);
        }
        String lep = expresion.solve(pointsValues, PresetEquationsManager.getParamsFromParams(equationNameOrDef), eloger, new ExpressionLogger.InheritingExpressionLogger(s -> {
            System.out.println(s);
            replies.add(s);
        }), presets);
        if (Boolean.parseBoolean(lep)) {
            //build.setResult(Result.UNSTABLE);
            return true;
        }
        return false;
    }
}
