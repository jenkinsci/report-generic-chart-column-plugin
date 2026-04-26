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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.chartjs.Chartjs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PropertiesParser {

    private static interface ListProvider {

        String getList();

        int getSurrounding();
    }

    List<String> getBlacklisted(Job<?, ?> job, final ChartModel chart) {
        return getList(job, chart, new ListProvider() {
            @Override
            public String getList() {
                return chart.getResultDenyList();
            }

            @Override
            public int getSurrounding() {
                return 0;
            }

        });

    }

    List<String> getWhitelisted(Job<?, ?> job, ChartModel chart) {
        return getList(job, chart, new ListProvider() {
            @Override
            public String getList() {
                return chart.getResultAllowList();
            }

            @Override
            public int getSurrounding() {
                return chart.getRangeAroundAlist();
            }
        });

    }

    /*
    Counting white list size without surroundings which is needed in title over the graph
     */
    List<String> getWhiteListWithoutSurroundings(Job<?, ?> job, ChartModel chart) {
        return getList(job, chart, new ListProvider() {
            @Override
            public String getList() {
                return chart.getResultAllowList();
            }

            @Override
            public int getSurrounding() {
                return 0;
            }
        });

    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "run.getResult().isWorseThan(Result.UNSTABLE) claims to have NPE, but I'm failing to see it")
    private List<String> getList(Job<?, ?> job, ChartModel chart, ListProvider provider) {
        if (provider.getList() == null || provider.getList().trim().isEmpty()) {
            return Collections.emptyList();
        }
        int limit = chart.getLimit();
        Run[] builds = job.getBuilds().toArray(new Run[0]);
        List<String> result = new ArrayList<>(limit);
        for (int i = 0; i < builds.length; i++) {
            Run run = builds[i];
            if (run == null
                    || run.getResult() == null
                    || run.getResult().isWorseThan(Result.UNSTABLE)) {
                continue;
            }
            String[] items = provider.getList().split("\\s+");
            for (String item : items) {
                if (run.getDisplayName().matches(item)) {
                    int numberOfFailedBuilds = 0;
                    for (int j = 0; j <= provider.getSurrounding() + numberOfFailedBuilds; j++) {
                        if (addNotFailedBuild(i + j, result, builds)) {
                            numberOfFailedBuilds++;
                        }
                    }
                    numberOfFailedBuilds = 0;
                    for (int j = -1; j >= -(provider.getSurrounding() + numberOfFailedBuilds); j--) {
                        if (addNotFailedBuild(i + j, result, builds)) {
                            numberOfFailedBuilds++;
                        }
                    }
                }
            }
        }
        return result;
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "run.getResult().isWorseThan(Result.UNSTABLE) claims to have NPE, but I'm failing to see it")
    private boolean addNotFailedBuild(int position, List<String> result, Run[] builds) {
        if (position >= 0 && position < builds.length) {
            boolean crashed =
                    builds[position] == null
                            || builds[position].getResult() == null
                            || builds[position].getResult().isWorseThan(Result.UNSTABLE);
            if (crashed) {
                return true;
            }
            /*Preventing duplicates in whitelist. Not because of the graph, there is
            already chunk of code preventing from showing duplicity in the graph.
            (The final list are recreated again with help of these lists)
            Its because lenght of whitelist which is shown over the graph.*/
            if (!result.contains(builds[position].getDisplayName())) {
                result.add(builds[position].getDisplayName());
            }
        }
        return false;
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "run.getResult().isWorseThan(Result.UNSTABLE) claims to have NPE, but I'm failing to see it")
    public ChartPointsWithBlacklist getReportPointsWithBlacklist(Job<?, ?> job, ChartModel chart) {
        List<ChartPoint> list = new ArrayList<>();
        List<String> blacklisted = getBlacklisted(job, chart);
        List<String> whitelisted = getWhitelisted(job, chart);
        List<String> whiteListWithoutSurroundings = getWhiteListWithoutSurroundings(job, chart);
        List<String> pointsInRangeOfwhitelisted = new ArrayList<>(whitelisted);
        int whiteListSizeWithoutSurroundings = whiteListWithoutSurroundings.toArray().length;
        pointsInRangeOfwhitelisted.removeAll(whiteListWithoutSurroundings);
        for (Run run : job.getBuilds()) {
            if (run == null
                    || run.getResult() == null
                    || run.getResult().isWorseThan(Result.UNSTABLE)) {
                continue;
            }
            if (blacklisted.contains(run.getDisplayName())) {
                continue;
            }
            if (!whitelisted.contains(run.getDisplayName()) && !whitelisted.isEmpty()) {
                continue;
            }

            List<ChartPoint> sublist = findPropertiesValues(run, chart, pointsInRangeOfwhitelisted);
            list.addAll(sublist);

            if (list.size() == chart.getLimit()) {
                break;
            }
        }

        Collections.reverse(list);

        return new ChartPointsWithBlacklist(list, blacklisted, whitelisted, whiteListSizeWithoutSurroundings);
    }

    private static List<ChartPoint> findPropertiesValues(Run run, ChartModel chart, List<String> pointsInRangeOfwhitelisted) {
        return ChartUtil.findPropertiesValues(run.getRootDir().toPath(),
                chart.getKey().trim(),
                chart.getFileNameGlob(),
                run.getDisplayName(),
                Chartjs.getShortName(run.getDisplayName(), run.getNumber()),
                run.getNumber(),
                chart.getPointColor(pointsInRangeOfwhitelisted.contains(run.getDisplayName())),
                run.getResult().toString());
    }

}
