/*
 * The MIT License
 *
 * Copyright 2015-2026 report-jtreg plugin contributors
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


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 * Utility class for backing up and storing report summaries.
 * mostly copied from jtreg plugin...
 */
public class ReportSummaryUtil {


    public static final String SUCCESS_DUPLICATE = "SUCCESS";
    public static final String UNSTABLE_DUPLICATE = "UNSTABLE";

    private static final Map<File, Map<String, String>> configCache = new HashMap<File, Map<String, String>>();

    private ReportSummaryUtil() {
    }

    static boolean isBuildDir(File dir) {
        try {
            return new File(dir, "archive").exists() && (dir.getCanonicalFile().getName().matches("[0-9]+"));
        } catch (IOException e) {
            return false;
        }
    }

    static File getConfig(File dir)  {
        try {
            File f =  new File(dir, "../../config.xml").getCanonicalFile();
            if (f.exists()) {
                return f;
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    static String getDisplayName(Path buildPath, int jobId) {
        String displayName = findInXml(new File(buildPath.toFile(), "build.xml"), "/build/displayName");
        if (displayName == null) {
            displayName = "#" + jobId;
        }
        return displayName;
    }
    static long[] getTimeAndDuration(Path buildPath) {
        long timeStamp;
        try {
            timeStamp = Long.parseLong(findInXml(new File(buildPath.toFile(), "build.xml"), "/build/timestamp"));
        } catch (Exception ex){
            timeStamp = -1;
        }
        long duration;
        try {
            duration = Long.parseLong(findInXml(new File(buildPath.toFile(), "build.xml"), "/build/duration"));
        } catch (Exception ex){
            duration = -1;
        }
        return new long[]{timeStamp, duration};
    }

    static String getBuildResult(File oldDir) {
        String resultOld = findInXml(new File(oldDir, "build.xml"), "/build/result");
        return resultOld;
    }

    static NodeList findNodeListInXml(File configFile, String xpath) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(configFile);

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.compile(xpath).evaluate(doc, XPathConstants.NODE);

            if (node != null) {
                return node.getChildNodes();
            } else {
                // warn the user that the value was not found and then return null
                System.err.println("Warning, the value defined by " + xpath + " in file " + configFile.getAbsolutePath() +
                        " does not exist, returning null.");
                return null;
            }
        } catch (ParserConfigurationException | XPathExpressionException | IOException | SAXException e) {
            // warn the user that an exception was thrown, print its stack trace and return null
            System.err.println("Warning, an exception was thrown when looking for a value defined by " + xpath + " in file " +
                    configFile.getAbsolutePath() + ", returning null.");
            e.printStackTrace();
            return null;
        }
    }

    public static String findInXml(File configFile, String findQuery) {
        // checks if the file/items in the file are already cached
        Map<String, String> cachedMap = configCache.get(configFile.getAbsoluteFile());
        if (cachedMap != null) {
            String cachedValue = cachedMap.get(findQuery);
            if (cachedValue != null) {
                return cachedValue;
            }
        }
        String value = findInConfigStaticNoCache(configFile, findQuery);
        // puts the value to the cache if not null
        if (value != null) {
            if (cachedMap == null) {
                cachedMap = new HashMap<>();
            }
            cachedMap.put(findQuery, value);
            configCache.put(configFile, cachedMap);
        }

        return value;
    }
    private static String findInConfigStaticNoCache(File configFile, String xpath) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(configFile);

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.compile(xpath).evaluate(doc, XPathConstants.NODE);

            if (node != null) {
                return node.getFirstChild().getNodeValue();
            } else {
                // warn the user that the value was not found and then return null
                System.err.println("Warning, the value defined by " + xpath + " in file " + configFile.getAbsolutePath() +
                        " does not exist, returning null.");
                return null;
            }
        } catch (ParserConfigurationException | XPathExpressionException | IOException | SAXException e) {
            // warn the user that an exception was thrown, print its stack trace and return null
            System.err.println("Warning, an exception was thrown when looking for a value defined by " + xpath + " in file " +
                    configFile.getAbsolutePath() + ", returning null.");
            e.printStackTrace();
            return null;
        }
    }

    private static Integer findPreviousBuild(Path buildPath, int jobId) {
        //find previous build (if any)
        Integer found = null;
        for (int i = jobId - 1; i > 0; i--) {
            File oldDir = new File(buildPath.toFile().getParentFile(), "" + i);
            if (oldDir.exists()) {
                String resultOld = getBuildResult(oldDir);
                if (SUCCESS_DUPLICATE.equals(resultOld) || UNSTABLE_DUPLICATE.equals(resultOld)) {
                    found = i;
                    break;
                }
            }
        }
        return found;
    }


    /**
     * Writes the common header for all report types.
     */
    private static void writeHeader(BufferedWriter writer, String jobName, String buildName, int buildNumber, String url, long buildtime, long duration) throws IOException {
        writer.write("=" .repeat(80) + "\n");
        writer.write(String.format("Performance Report for %s%n", jobName));
        writer.write(String.format("Build name: %s (ID %d)%n", buildName, buildNumber));
        writer.write(String.format("Build timestamp: %s ms%n", toKnown(buildtime, false, false)));
        writer.write(String.format("Build duration: %s ms%n", toKnown(duration, false, false)));
        writer.write("=".repeat(80) + "\n\n");
        introduction(writer, jobName, buildName, buildNumber, url, buildtime, duration);

    }

    private static void introduction(BufferedWriter writer, String jobName, String buildName, int buildNumber, String url, long buildtime, long duration) throws IOException {
        String s = "This results are related to build of " + buildName + "/" + buildNumber + " in test of " + jobName + ". It started at " + toKnown(buildtime, true, false) + " and had duration of " + toKnown(duration, true, true) + ".";
        writer.write(s + "\n\n");
    }

    private static void footerr(BufferedWriter writer, String jobName, String buildName, int buildNumber, String url, long buildtime, long duration) throws IOException {
        {
            writer.write("=".repeat(80) + "\\n");
            String s = "This results are related to build of " + buildName + "/" + buildNumber + " in test of " + jobName + "." + "It started at " + toKnown(buildtime, true, true) + " and had duration of " + toKnown(duration, true, true) + ".";
            writer.write(s + "\n\n");
            writer.write("=".repeat(80) + "\n\n\n");
        }
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

    private static void footer(BufferedWriter writer, String jobName, String buildName, int buildNumber, String url, String testType, String date) throws IOException {
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
        writer.write("End of " + testType + " of build " + buildName + "/" + buildNumber + " in job " + jobName + " from " + date + ".\n");
        writer.write("\n" + "=".repeat(80) + "\n\n");
    }

    /**
     * Returns "s" for plural or empty string for singular.
     */
    private static String pluralize(int count) {
        return count == 1 ? "" : "s";
    }


    //not stolen from jtregs!
    static List<Integer> getOldBuilds(Path buildPath, int buildId) {
        List<Integer> dataHistory = new ArrayList<>();
        for (int i = buildId - 1; i > 0; i--) {
            File oldDir = new File(buildPath.toFile().getParentFile(), "" + i);
            if (oldDir.exists()) {
                String resultOld = ReportSummaryUtil.getBuildResult(oldDir);
                if (ReportSummaryUtil.SUCCESS_DUPLICATE.equals(resultOld) || ReportSummaryUtil.UNSTABLE_DUPLICATE.equals(resultOld)) {
                    dataHistory.add(i);
                    if (dataHistory.size() > 10) {
                        break;
                    }
                }
            }
        }
        return dataHistory;
    }

}