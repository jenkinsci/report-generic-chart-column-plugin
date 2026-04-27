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

package io.jenkins.plugins.genericchart;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
/*No imports from hudson here! This is reused elsewhere!*/

public class ChartUtil {

    public static final String log_comments = "log_comments";
    public static final String log_equation = "log_equation";
    public static final String PRESET_DEFS = "preset_defs";
    public static final String JENKINS_URL = "jenkins_url";

    private ChartUtil() {
    }


    public static Predicate<String> getValueKeyPredicate(String key) {
        Predicate<String> lineValidator = str -> {
            if (str == null || str.trim().isEmpty()) {
                return false;
            }
            int index = getBestDelimiterIndex(str);
            if (index == Integer.MAX_VALUE) {
                return false;
            }
            if (!str.substring(0, index).trim().equals(key)) {
                return false;
            }
            try {
                Double.parseDouble(str.substring(index + 1).trim());
                return true;
            } catch (Exception ignore) {
            }
            return false;
        };
        return lineValidator;
    }


    public static String extractValue(String s) {
        return s.substring(getBestDelimiterIndex(s) + 1).trim();
    }

    public static int getBestDelimiterIndex(String str) {
        int index1 = str.indexOf('=');
        int index2 = str.indexOf(':');
        if (index1 < 0) {
            index1 = Integer.MAX_VALUE;
        }
        if (index2 < 0) {
            index2 = Integer.MAX_VALUE;
        }
        int index = Math.min(index1, index2);
        return index;
    }

    public static List<ChartPoint> findPropertiesValues(Path rootDir, String key, String fileNameGlob, String displayName, String shortenedDisplayName, int buildNumber, String color, String currentResult) {
        Predicate<String> lineValidator = ChartUtil.getValueKeyPredicate(key);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileNameGlob);
        List<ChartPoint> list = new ArrayList<>();
        try (Stream<Path> filesStream = Files.walk(rootDir).sequential()) {
            Optional<ChartPoint> optPoint = filesStream.filter((p) -> matcher.matches(p.getFileName())).map((p) -> pathToLine(p, lineValidator)).filter((o) -> o.isPresent()).map(o -> o.get()).map(s -> new ChartPoint(displayName, shortenedDisplayName, buildNumber, ChartUtil.extractValue(s), color, currentResult)).findFirst();
            if (optPoint.isPresent()) {
                list.add(optPoint.get());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private static Optional<String> pathToLine(Path path, Predicate<String> lineValidator) {
        try (Stream<String> stream = Files.lines(path)) {
            return stream.filter(lineValidator).findFirst();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }


    public static boolean isVarOrProp(String key) {
        return System.getenv().containsKey(key.toUpperCase()) || System.getProperties().containsKey(key.toLowerCase());
    }

    public static String getVarOrProp(String key) {
        if (System.getProperties().containsKey(key.toLowerCase()) && !System.getProperty(key.toLowerCase()).isBlank()) {
            return System.getProperties().getProperty(key.toLowerCase());
        }
        if (System.getenv().containsKey(key.toUpperCase()) && !System.getenv(key.toUpperCase()).isBlank()) {
            return System.getenv(key.toUpperCase());
        }
        return null;
    }


}