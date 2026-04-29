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
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.jenkins.plugins.genericchart.ChartPoint;
import io.jenkins.plugins.genericchart.ChartUtil;
import io.jenkins.plugins.genericchart.equations.IncrementalSequentialEvaluator;
import io.jenkins.plugins.genericchart.equations.PresetEquationDefinition;
import io.jenkins.plugins.genericchart.equations.PresetEquationsManager;
import parser.logical.ExpressionLogger;

public class PlaintextWriter implements AutoCloseable {
    public static final String GENERIC_CHART_RESULTS_TXT = "generic-chart-results.txt";
    public static final String GENERIC_CHART_RESULTS_PROPS = "generic-chart-values.properties";
    public static final String GENERIC_CHART_RESULTS_HISTORY = "generic-chart-history.properties";
    private final BufferedWriter writer;
    private final File file;
    private final Map<String, String> values = new TreeMap<>();
    private final Map<String, String> history = new TreeMap<>();

    public PlaintextWriter(File targetDir) throws IOException {
        this.file = new File(targetDir, GENERIC_CHART_RESULTS_TXT);
        this.writer = new BufferedWriter(new FileWriter(file, Charset.defaultCharset()));
    }

    private static void writeAllProperties(File dir, Map<String, String> values, Map<String, String> history) {
        writeProperties(dir,values);
        writeHistory(dir,history);
    }

    private static void writeProperties(File dir, Map<String, String> values) {
        try {
            writePropertiesImpl(new File(dir, GENERIC_CHART_RESULTS_PROPS), values);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void writeHistory(File dir, Map<String, String> values) {
        try {
            writePropertiesImpl(new File(dir, GENERIC_CHART_RESULTS_HISTORY), values);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void writePropertiesImpl(File file, Map<String, String> values) throws IOException {
        try (BufferedWriter propwriter = new BufferedWriter(new FileWriter(file, Charset.defaultCharset()))) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                propwriter.write(entry.getKey() + "=" + entry.getValue());
                propwriter.newLine();
            }
        }

    }

    @Override
    public void close() throws IOException {
        writer.close();
        System.err.println("Closed : " + this.file.getAbsolutePath());
        writeAllProperties(file.getParentFile(), values, history);
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
        history.put("generic-chart-plugin~displayName", buildName);
        history.put("generic-chart-plugin~buildNumber", buildNumber + "");
        history.put("generic-chart-plugin~job", jobName);
        history.put("generic-chart-plugin~timestamp", toKnown(buildtime, false, false));
        history.put("generic-chart-plugin~duration", toKnown(duration, false, false));
        history.put("generic-chart-plugin~timestampNice", toKnown(buildtime, true, false));
        history.put("generic-chart-plugin~durationNice", toKnown(duration, true, true));

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

    public void allUsedPastBuilds(List<ChartPoint> oneChartAllData, ExpressionLogger outputControlCandidate, boolean running, String pkey, String glob) throws IOException {
        int i = -1;
        for (ChartPoint chartPoint : oneChartAllData) {
            i++;
            String result = chartPoint.getResult() + (i == 0 ? " (this)" : "");
            if (running) {
                result =  (i == 0 ? " RUNNING" : " " + chartPoint.getResult());
            }

            String mkey=glob+"~"+pkey;
            if (i==0){
                values.put(mkey, chartPoint.getValue());
            }
            String historyValues = history.getOrDefault(mkey, "");
            historyValues = historyValues + chartPoint.getValue() + " ";
            history.put(mkey, historyValues);

            outputControlCandidate.log(chartPoint.getBuildName() + "/" + chartPoint.getBuildNumber() + ": " + chartPoint.getValue() + " " + result);
        }
        outputControlCandidate.log("shortened values (shown reverted, newest->oldest): " + oneChartAllData.stream().map(s -> s.getValue()).collect(Collectors.joining(",")));
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

    public void closeAllCharts(int failures, String displayName, int buildId, String jobName, ExpressionLogger ex) {
        println("=".repeat(80) + "");
        if (failures == 0) {
            String s = "Generic chart report from properties found no regression for " + displayName + "/" + buildId + " in " + jobName;
            println(s);
            ex.log(s);
        } else {
            String s =  "Generic chart report from properties found " + failures + " regression(s) for " + displayName + "/" + buildId + " in " + jobName;
            println(s);
            ex.log(s);
        }
    }


    public boolean calcSingleChartAndResolve(LoadedChart chart, List<ChartPoint> oneChartAllData, ExpressionLogger outputControlCandidate) throws IOException{
        try {
            boolean thicChartResult = calc(chart, oneChartAllData, outputControlCandidate);
            if (thicChartResult) {
                outputControlCandidate.log("Result of " + chart.getTitleLikeChart() + " is true, that is regression.");
            } else {
                outputControlCandidate.log("Result of " + chart.getTitleLikeChart() + " is false, that is ok.");
            }
            return thicChartResult;
        } catch (Exception ex) {
            ex.printStackTrace();
            outputControlCandidate.log(ex.toString());
            outputControlCandidate.log("Failed to calculate" + chart.getTitleLikeChart() + ", considering it as regression.");
            return true;
        }
    }

    private boolean calc(LoadedChart chartDef, List<ChartPoint> points, ExpressionLogger outputControlCandidate) throws IOException, URISyntaxException, IOException{
        PresetEquationsManager presets = new PresetEquationsManager(ChartUtil.getVarOrProp(ChartUtil.PRESET_DEFS));
        String equationNameOrDef = chartDef.getUnstableCondition();
        PresetEquationDefinition isPreset = presets.getFromCommandString(equationNameOrDef);
        IncrementalSequentialEvaluator expresion;
        if (isPreset != null) {
            expresion = isPreset.getExpressions();
        } else {
            expresion = IncrementalSequentialEvaluator.getUserDefIncrementalSequentialEvaluator(equationNameOrDef);
        }
        List<String> pointsValues = points.stream().map(a -> a.getValue()).collect(Collectors.toList());
        ExpressionLogger eloger = s -> {
        };
        if (ChartUtil.isVarOrProp(ChartUtil.log_equation) || !expresion.hasAnswers()) {
            eloger = s -> outputControlCandidate.log(s);
        }
        String lep = expresion.solve(pointsValues, PresetEquationsManager.getParamsFromParams(equationNameOrDef), eloger, new ExpressionLogger.InheritingExpressionLogger(s -> {
            outputControlCandidate.log(s);
        }), presets);
        if (Boolean.parseBoolean(lep)) {
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
