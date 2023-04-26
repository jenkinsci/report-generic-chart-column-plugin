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
import hudson.model.Job;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import java.util.List;
import java.util.UUID;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class GenericChartColumn extends ListViewColumn {

    private String fileNameGlob;
    @SuppressWarnings({"lgtm[jenkins/plaintext-storage]", "This is not a password, it is key in properties file"})
    private String key;
    private int limit;
    private String columnCaption;
    private String chartColor;
    private String resultsDenyList;
    private String resultsAllowList;
    private int rangeAroundAlist;

    @DataBoundConstructor
    public GenericChartColumn(String fileNameGlob, String key, int limit, String columnCaption, String chartColor, int rangeAroundAlist) {
        this.fileNameGlob = fileNameGlob;
        this.key = key;
        this.limit = limit;
        this.columnCaption = columnCaption;
        this.chartColor = chartColor;
        this.rangeAroundAlist = rangeAroundAlist;
    }

    public List<ChartPoint> getReportPoints(Job<?, ?> job) {
        ChartModel model = new ChartModel(key, fileNameGlob, key, limit, chartColor, rangeAroundAlist);
        model.setResultDenyList(resultsDenyList);
        model.setResultAllowList(resultsAllowList);
        return new PropertiesParser().getReportPointsWithBlacklist(job, model).getPoints();
    }

    public String getLatestResult(final List<ChartPoint> results) {
        if (!results.isEmpty()) {
            return results.get(results.size() - 1).getValue();
        } else {
            return "0";
        }
    }

    public String getFileNameGlob() {
        return fileNameGlob;
    }

    public String generateChartName() {
        return "chart"+UUID.randomUUID().toString().replace("-","");
    }

    @DataBoundSetter
    public void setFileNameGlob(String fileNameGlob) {
        this.fileNameGlob = fileNameGlob;
    }

    public String getKey() {
        return key;
    }

    @DataBoundSetter
    public void setKey(String key) {
        this.key = key;
    }

    public int getLimit() {
        return limit;
    }

    @DataBoundSetter
    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public String getColumnCaption() {
        return columnCaption;
    }

    @DataBoundSetter
    public void setColumnCaption(String columnCaption) {
        this.columnCaption = columnCaption;
    }

    public String getChartColor() {
        return chartColor;
    }

    @DataBoundSetter
    public void setChartColor(String chartColor) {
        this.chartColor = chartColor;
    }

    @Extension
    public static final GenericChartColumnDescriptor DESCRIPTOR = new GenericChartColumnDescriptor();

    public static class GenericChartColumnDescriptor extends ListViewColumnDescriptor {

        @Override
        public boolean shownByDefault() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return "Chart";
        }

    }

    @DataBoundSetter
    public void setResultDenyList(String resultDenyList) {
        this.resultsDenyList = resultDenyList;
    }

    public String getResultDenyList() {
        return resultsDenyList;
    }

    @DataBoundSetter
    public void setResultAllowList(String resultAllowList) {
        this.resultsAllowList = resultAllowList;
    }

    public String getResultAllowList() {
        return resultsAllowList;
    }

    public int getRangeAroundAlist() {
        return rangeAroundAlist;
    }

    @DataBoundSetter
    public void setRangeAroundAlist(int rangeAroundAlist) {
        this.rangeAroundAlist = rangeAroundAlist;
    }

}
