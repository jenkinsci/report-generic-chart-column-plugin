package io.jenkins.plugins.genericchart;

import java.util.List;

import io.jenkins.plugins.genericchart.regenerate.DirArgs;

class GenericChartPublisherDirArgs extends DirArgs {

    private final String targetFolders;
    private final String additionalFiles;

    public GenericChartPublisherDirArgs(String targetFolders, String additionalFiles) {
        super();
        this.targetFolders = targetFolders;
        this.additionalFiles = additionalFiles;
    }

    @Override
    public String getOut() {
        List<String> count = getAdditionalFilesImpl(targetFolders);
        for (String c : count) {
            if (c.startsWith("out-dir:")) {
                return c.substring("out-dir:".length());
            }
        }
        return null;

    }

    @Override
    public String getNvrDb() {
        if (sanitize(targetFolders) == null) {
            return null;
        }
        List<String> count = getAdditionalFilesImpl(targetFolders);
        if (count.size() == 1) {
            return targetFolders + "/nvr-db";
        } else {
            for (String c : count) {
                if (c.startsWith("nvr-db:")) {
                    return c.substring("nvr-db:".length());
                }
            }
            return null;
        }
    }

    @Override
    public String getJobDb() {
        if (sanitize(targetFolders) == null) {
            return null;
        }
        List<String> count = getAdditionalFilesImpl(targetFolders);
        if (count.size() == 1) {
            return targetFolders + "/job-db";
        } else {
            for (String c : count) {
                if (c.startsWith("job-db:")) {
                    return c.substring("job-db:".length());
                }
            }
            return null;
        }
    }

    @Override
    protected List<String> getAdditionalFiles() {
        return getAdditionalFilesImpl(additionalFiles);
    }

}
