package io.jenkins.plugins.org.mortbay.jetty.jenkins.tck;

import hudson.Extension;
import hudson.FilePath;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public class TckReportToJunitStep
    extends Step
{

    private String tckReportTxtPath;

    private String junitFolderPath;

    @DataBoundConstructor
    public TckReportToJunitStep( String tckReportTxtPath, String junitFolderPath )
    {
        this.tckReportTxtPath = tckReportTxtPath;
        this.junitFolderPath = junitFolderPath;
    }

    @Override
    public StepExecution start( StepContext stepContext )
        throws Exception
    {
        return new TckReportToJunitStepExecution(stepContext, tckReportTxtPath, junitFolderPath);
    }

    @Extension
    @Symbol( "tckreporttojunit" )
    public static class DescriptorImpl
        extends StepDescriptor
    {
        @Override
        public String getFunctionName()
        {
            return "tckreporttojunit";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext()
        {
            return Collections.singleton( FilePath.class );
        }

        @Nonnull
        @Override
        public String getDisplayName()
        {
            return "TCK Report to Junit report";
        }
    }
}
