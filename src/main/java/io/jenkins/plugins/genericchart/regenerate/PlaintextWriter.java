package io.jenkins.plugins.genericchart.regenerate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.jenkins.plugins.genericchart.ChartPoint;
import io.jenkins.plugins.genericchart.ChartUtil;
import io.jenkins.plugins.genericchart.equations.IncrementalSequentialEvaluator;
import io.jenkins.plugins.genericchart.equations.PresetEquationDefinition;
import io.jenkins.plugins.genericchart.equations.PresetEquationsManager;
import parser.logical.ExpressionLogger;

public class PlaintextWriter implements AutoCloseable {
    private final BufferedWriter writer;
    private final File file;

    public PlaintextWriter(File targetDir) throws IOException {
        this.file = new File(targetDir, "generic-chart-results.txt");
        this.writer = new BufferedWriter(new FileWriter(file, Charset.defaultCharset()));
    }

    @Override
    public void close() throws IOException {
        writer.close();
        System.err.println("Closed : " + this.file.getAbsolutePath());
    }

    public void println(String s) throws RuntimeException {
        try {
            writer.write(s);
            writer.newLine();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Writes the common header for all report types.
     */
    public void writeHeader(String jobName, String buildName, int buildNumber, String url, long buildtime, long duration) throws IOException {
        writer.write("=".repeat(80) + "\n");
        writer.write(String.format("Performance Report for %s%n", jobName));
        writer.write(String.format("Build name: %s (ID %d)%n", buildName, buildNumber));
        writer.write(String.format("Build timestamp: %s ms%n", toKnown(buildtime, false, false)));
        writer.write(String.format("Build duration: %s ms%n", toKnown(duration, false, false)));
        writer.write("=".repeat(80) + "\n\n");
        introduction(jobName, buildName, buildNumber, url, buildtime, duration);

    }

    private void introduction(String jobName, String buildName, int buildNumber, String url, long buildtime, long duration) throws IOException {
        String s = "This results are related to build of " + buildName + "/" + buildNumber + " in test of " + jobName + ". It started at " + toKnown(buildtime, true, false) + " and had duration of " + toKnown(duration, true, true) + ".";
        writer.write(s + "\n");
    }

    public void introductionChartsCount(int loadedChartsCunt, int buildId, String displayName, String jobName) throws IOException {
        println(loadedChartsCunt + " valid charts with guarding equation loaded for " + buildId + "/" + displayName + " " + jobName);
        if (loadedChartsCunt <= 0) {
            println("No performance data found");
        }
        println("");
    }

    public void introductionTimes(String status, long duration, long startTime) throws IOException {
        println(status + " took " + duration + ", started at " + startTime);
    }

    public  void singleChartTitle(LoadedChart chart, int chartCounter, int loadedChartsCount) throws IOException {
        println("");
        println("### " + chartCounter + "/" + loadedChartsCount + " " + chart.getTitleLikeChart());
    }

    public void allUsedPastBuilds(List<ChartPoint> oneChartAllData) throws IOException {
        int i = -1;
        for (ChartPoint chartPoint : oneChartAllData) {
            i++;
            println(chartPoint.getBuildName() + "/" + chartPoint.getBuildNumber() + ": " + chartPoint.getValue() + " " + chartPoint.getResult() + (i == 0 ? " (this)" : ""));
        }
        println("shortened values (shown reverted, newest->oldest): " + oneChartAllData.stream().map(s -> s.getValue()).collect(Collectors.joining(",")));
    }


    public void footerr(String jobName, String buildName, int buildNumber, String url, long buildtime, long duration) throws IOException {
        writer.write("=".repeat(80) + "\\n");
        String s = "This results are related to build of " + buildName + "/" + buildNumber + " in test of " + jobName + ". It started at " + toKnown(buildtime, true, true) + " and had duration of " + toKnown(duration, true, true) + ".";
        writer.write(s + "\n\n");
        writer.write("=".repeat(80) + "\n\n\n");
    }


    public void footer(String jobName, String buildName, int buildNumber,Long time, String url) throws IOException {
        String date = toKnown(time, true, false);
        if (url != null) {
            writer.write("=".repeat(80) + "\n\n");
            String page = url + "/job/" + jobName;
            writer.write("You can see the job page at: " + page + "\n");
            String build = page + "/" + buildNumber;
            writer.write("You can see the build  page at: " + build + "\n");
            writer.write("You can see the build artifacts at: " + build + "/artifact" + "\n");
            writer.write("You can see the build log at: " + build + "/console" + "\n");
            writer.write("You can see the build full log at: " + build + "/consoleFull" + "\n");
        }
        writer.write("\n" + "=".repeat(80) + "\n\n");
        writer.write("End of generic chart plugin performance report of build " + buildName + "/" + buildNumber + " in job " + jobName + " from " + date + ".\n");
        writer.write("\n" + "=".repeat(80) + "\n\n");
    }

    public void closeAllCharts(int failures, String displayName, int buildId, String jobName) {
        println("=".repeat(80) + "");
        if (failures == 0) {
            println("Generic chart report from properties found no regression for " + displayName + "/" + buildId + " in " + jobName);
        } else {
            println("Generic chart report from properties found " + failures + " regression(s) for " + displayName + "/" + buildId + " in " + jobName);
        }
    }


    public boolean calcSingleChartAndResolve(LoadedChart chart, List<ChartPoint> oneChartAllData) throws IOException{
        try {
            boolean thicChartResult = calc(chart, oneChartAllData);
            if (thicChartResult) {
                println("Result of " + chart.getTitleLikeChart() + " is true, that is regression.");


            } else {
                println("Result of " + chart.getTitleLikeChart() + " is false, that is ok.");
            }
            return thicChartResult;
        } catch (Exception ex) {
            ex.printStackTrace();
            println(ex.toString());
            println("Failed to calculate" + chart.getTitleLikeChart() + ", considering it as regression.");
        }
        return true;
    }

    private boolean calc(LoadedChart chartDef, List<ChartPoint> points) throws IOException, URISyntaxException, IOException{
        PresetEquationsManager presets = new PresetEquationsManager(ChartUtil.getVarOrProp(ChartUtil.PRESET_DEFS));
        String equationNameOrDef = chartDef.getUnstableCondition();
        PresetEquationDefinition isPreset = presets.getFromCommandString(equationNameOrDef);
        IncrementalSequentialEvaluator expresion;
        if (isPreset != null) {
            //System.err.print(equationNameOrDef + " found as preset queue");
            expresion = isPreset.getExpressions();
        } else {
            expresion = IncrementalSequentialEvaluator.getUserDefIncrementalSequentialEvaluator(equationNameOrDef);
        }
        List<String> pointsValues = points.stream().map(a -> a.getValue()).collect(Collectors.toList());
        ExpressionLogger eloger = s -> {
        };
        //fixme links to the 1.9 r eadme (or new in tip?
        if (ChartUtil.isVarOrProp(ChartUtil.log_equation)) {
            eloger = s -> println(s);
        }
        String lep = expresion.solve(pointsValues, PresetEquationsManager.getParamsFromParams(equationNameOrDef), eloger, new ExpressionLogger.InheritingExpressionLogger(s -> {
            println(s);
        }), presets);
        if (Boolean.parseBoolean(lep)) {
            //build.setResult(Result.UNSTABLE);
            return true;
        }
        return false;
    }


    private static String toKnown(long time, boolean port, boolean duration) {
        if (time <= 0) {
            return "unknown";
        } else {
            if (!port) {
                return "" + time;
            } else {
                if (duration) {
                    return Duration.ofMillis(time).toString();
                } else {
                    return new Date(time).toInstant().toString();
                }
            }
        }
    }

    private static String pluralize(int count) {
        return count == 1 ? "" : "s";
    }

}
