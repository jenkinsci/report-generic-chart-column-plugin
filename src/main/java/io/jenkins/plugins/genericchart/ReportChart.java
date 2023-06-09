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

import hudson.model.Job;

import java.util.List;

public class ReportChart {

    private final String title;
    private final String color;
    private final List<ChartPoint> points;
    private final List<String> blist;
    private final List<String> wlist;
    private final int rangeAroundWlist;
    private final int whiteListSizeWithoutSurroundings;
    private final String unstableCondition;

    private ReportChart(String title, String color, String unstableCondition, List<ChartPoint> points, List<String> blist, List<String> wlist, int rangeAroundWlist, int whiteListSizeWithoutSurroundings) {
        this.blist = blist;
        this.title = title;
        this.color = color;
        this.unstableCondition = unstableCondition;
        this.points = points;
        this.wlist = wlist;
        this.rangeAroundWlist = rangeAroundWlist;
        this.whiteListSizeWithoutSurroundings = whiteListSizeWithoutSurroundings;
    }

    public static ReportChart createReportChart(ChartModel m, PropertiesParser parser, Job<?, ?> job) {
        final ChartPointsWithBlacklist points = parser.getReportPointsWithBlacklist(job, m);
        return new ReportChart(
                m.getTitle(),
                m.getChartColor(),
                m.getUnstableCondition(),
                points.getPoints(),
                points.getBlacklist(),
                points.getWhitelist(),
                m.getRangeAroundAlist(),
                points.getWhiteListSizeWithoutSurroundings());
    }

    public String getTitle() {
        return title + " (dennied " + blist.size() + ")" + " (allowed " + whiteListSizeWithoutSurroundings + "+" + Integer.toString(wlist.size() - whiteListSizeWithoutSurroundings) + ")";
    }

    public String getColor() {
        return color;
    }

    public List<ChartPoint> getPoints() {
        return points;
    }

    public int getRangeAroundWlist() {
        return rangeAroundWlist;
    }

    public int getWhiteListSizeWithoutSurroundings() {
        return whiteListSizeWithoutSurroundings;
    }

    public String getUnstableCondition() {
        return unstableCondition;
    }
}
