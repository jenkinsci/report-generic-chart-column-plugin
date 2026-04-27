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
package io.jenkins.plugins.genericchart.regenerate;

import java.util.Map;

import io.jenkins.plugins.chartjs.ColorChanger;

public class LoadedChart {

    private String title;
    private String fileNameGlob;
    @SuppressWarnings({"lgtm[jenkins/plaintext-storage]", "This is not a password, it is key in properties file"})
    private String key;
    private int limit;
    private String resultDenyList;
    private String resultAllowList;
    private String chartColor;
    private int rangeAroundAlist;
    private String unstableCondition;


    public LoadedChart(Map<String, String> map) {
        this.unstableCondition = map.get("unstableCondition");
        this.title = map.get("title");
        this.fileNameGlob = map.get("fileNameGlob");;
        this.key = map.get("key");;
        String slimit =  map.get("limit");
        if (slimit == null || slimit.isBlank()) {
            this.limit = 10;
        } else {
            this.limit = Integer.parseInt(slimit);
        }
        String scolor = map.get("chartColor");
        if (scolor == null || scolor.isBlank()) {
            this.chartColor = ColorChanger.randomColor();
        } else {
            this.chartColor = scolor;
        }
        String srangeAroundAlist =  map.get("rangeAroundAlist");
        if (srangeAroundAlist == null || srangeAroundAlist.isBlank()) {
            this.rangeAroundAlist = 0;
        } else {
            this.rangeAroundAlist = Integer.parseInt(srangeAroundAlist);
        }
        this.resultDenyList = map.get("resultDenyList");
        this.resultAllowList = map.get("resultAllowList");
    }

    public String getTitle() {
        return title;
    }

    public String getTitleLikeChart() {
        return "" + getKey() + " from " + getFileNameGlob();
    }

    public String getFileNameGlob() {
        return fileNameGlob;
    }

    public String getKey() {
        return key;
    }


    public int getLimit() {
        return limit;
    }


    public String getChartColor() {
        return chartColor;
    }

    public int getRangeAroundAlist() {
        return rangeAroundAlist;
    }

    public String getResultDenyList() {
        return resultDenyList;
    }

    public String getResultAllowList() {
        return resultAllowList;
    }

    public String getPointColor(boolean isInRangeOfAllowListed) {
        if (isInRangeOfAllowListed) {
            //there is 32 because it slightly change shade of color so graph is more readable
            return ColorChanger.shiftColorBy(chartColor, 64, 64, 32);
        }
        return chartColor;
    }

    public String getUnstableCondition() {
        return unstableCondition;
    }

}
