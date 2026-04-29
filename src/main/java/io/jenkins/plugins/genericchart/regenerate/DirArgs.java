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


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DirArgs {

    public static final String out_dir = "OUT_DIR";
    public static final String nvr_dir = "NVR_DIR";
    public static final String job_dir = "JOB_DIR";
    public static final String add_files = "ADD_FILES";


    private String sanitize(String a) {
        if (a == null) {
            return null;
        }
        if (a.isBlank()) {
            return null;
        }
        return a.trim();
    }

    public String getOut() {
        return sanitize(System.getenv(out_dir));
    }

    public String getNvrDb() {
        return sanitize(System.getenv(nvr_dir));
    }

    public String getJobDb() {
        return sanitize(System.getenv(job_dir));
    }


    private List<String> getAdditionalFiles() {
        String all = System.getenv(add_files);
        if (all == null) {
            return null;
        }
        if (all.isBlank()) {
            return null;
        }
        return Arrays.asList(all.trim().split(","));
    }

    public List<String> getAllSource() {
        List<String> sources = new ArrayList<>();
        if (getAdditionalFiles() != null) {
            sources.addAll(getAdditionalFiles());
        }
        sources.add(PlaintextWriter.GENERIC_CHART_RESULTS_TXT);
        sources.add(PlaintextWriter.GENERIC_CHART_RESULTS_PROPS);
        sources.add(PlaintextWriter.GENERIC_CHART_RESULTS_HISTORY);
        return sources;
    }


    public static void export(Path buildPath, DirArgs params, String displayName, int buildId, String jobName) throws IOException {
        List<Path> allFiles = params.getAllSource().stream().map(s -> new File(buildPath.toFile(), s).getAbsoluteFile().toPath()).toList();
        String result = "UNKNOWN";
        File buildXml = new File(buildPath.toFile(), "build.xml");
        if (buildXml.exists()) {
            result = ReportSummaryUtil.getBuildResult(buildPath.toFile());
        }
        if (params.getOut() != null) {
            copyWithOverwrite(allFiles, new File(params.getOut()).toPath());
        }
        if (params.getJobDb() != null) {
            File jobDir = new File(params.getJobDb() + "/" + jobName + "/" + displayName + "/" + result + "/" + buildId);
            copyWithOverwrite(allFiles, jobDir.toPath());
        }
        if (params.getNvrDb() != null) {
            File nvrDir = new File(params.getNvrDb() + "/" + displayName + "/" + jobName + "/" + result + "/" + buildId);
            copyWithOverwrite(allFiles, nvrDir.toPath());


        }
    }


    //this is flatening, also becasue we can in theory copy ../../config.xml
    private static void copyWithOverwrite(List<Path> allFiles, Path outDir) throws IOException {
        Files.createDirectories(outDir);
        for (Path file : allFiles) {
            Path targetPath = outDir.resolve(file.getFileName());
            if (Files.exists(file)) {
                Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

}

