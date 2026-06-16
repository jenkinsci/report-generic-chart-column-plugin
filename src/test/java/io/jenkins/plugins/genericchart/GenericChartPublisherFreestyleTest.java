package io.jenkins.plugins.genericchart;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WithJenkins
class GenericChartPublisherFreestyleTest {

    @Test
    void testFreestyleWithSimpleChart(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test-freestyle");

        project.getBuildersList().add(new hudson.tasks.Shell(
                "echo 'test.value=100' > test.properties"));
        project.getPublishersList().add(new hudson.tasks.ArtifactArchiver("test.properties"));

        ChartModel chart = new ChartModel("Test Chart", "test.properties", "test.value", 10, "#FF6384", 5);
        project.getPublishersList().add(new GenericChartPublisher(List.of(chart)));

        FreeStyleBuild build = jenkins.assertBuildStatusSuccess(project.scheduleBuild2(0));
        assertEquals(Result.SUCCESS, build.getResult());

        GenericChartProjectAction action = project.getAction(GenericChartProjectAction.class);
        assertNotNull(action, "GenericChartProjectAction should be added to freestyle job");
        assertFalse(action.getCharts().isEmpty(), "Charts should be available in the action");
    }

    @Test
    void testFreestyleWithMultipleCharts(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test-freestyle-multiple");

        project.getBuildersList().add(new hudson.tasks.Shell(
                "echo 'cpu=75\nmemory=80' > metrics.properties"));
        project.getPublishersList().add(new hudson.tasks.ArtifactArchiver("metrics.properties"));

        ChartModel cpuChart = new ChartModel("CPU", "metrics.properties", "cpu", 10, "#FF6384", 5);
        ChartModel memChart = new ChartModel("Memory", "metrics.properties", "memory", 10, "#36A2EB", 5);
        project.getPublishersList().add(new GenericChartPublisher(List.of(cpuChart, memChart)));

        FreeStyleBuild build = jenkins.assertBuildStatusSuccess(project.scheduleBuild2(0));
        assertEquals(Result.SUCCESS, build.getResult());

        GenericChartProjectAction action = project.getAction(GenericChartProjectAction.class);
        assertNotNull(action, "GenericChartProjectAction should be added to freestyle job");
        assertEquals(2, action.getCharts().size(), "Should have 2 charts");
    }
}
