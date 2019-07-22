package org.mortbay.jetty.load.generator.jenkins.tck.test;

import hudson.FilePath;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.util.List;

public class ToJunitTest
{

    @Rule
    public final JenkinsRule rule = new JenkinsRule();

    @Test
    public void testToJunit()
        throws Exception
    {

        WorkflowJob j = rule.jenkins.createProject( WorkflowJob.class, "yup" );

        j.setDefinition( new CpsFlowDefinition( "  node {\n" + //
                                                    "      tckreporttojunit(tckReportTxtPath: 'reports_dir/summary.txt', junitFolderPath: 'surefire-reports')\n"
                                                    + //
                                                    "  }\n", true ) );

        // copy test resources
        FilePath ws = rule.jenkins.getWorkspaceFor( j );

        FilePath reports = new FilePath( new File( "src/test/resources/" ) );
        FilePath reportsDir = ws.child( "reports_dir" );
        reportsDir.mkdirs();
        reports.copyRecursiveTo( reportsDir );

        WorkflowRun r = j.scheduleBuild2( 0 ).waitForStart();
        rule.assertBuildStatus( Result.SUCCESS, rule.waitForCompletion( r ) );

        List<FilePath> junitFiles = ws.child( "surefire-reports" ).list();
        System.out.println( "junitFiles:" + junitFiles );
        Assert.assertTrue(junitFiles.size()>1);
    }
}
