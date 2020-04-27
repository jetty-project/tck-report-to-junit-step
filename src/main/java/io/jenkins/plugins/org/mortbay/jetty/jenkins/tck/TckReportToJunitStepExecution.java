package io.jenkins.plugins.org.mortbay.jetty.jenkins.tck;

import hudson.FilePath;
import hudson.model.Result;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TckReportToJunitStepExecution extends StepExecution
{
    private String tckReportTxtPath;
    private String junitFolderPath;
    public TckReportToJunitStepExecution(@Nonnull StepContext context, String tckReportTxtPath, String junitFolderPath)
    {
        super(context);
        this.tckReportTxtPath = tckReportTxtPath;
        this.junitFolderPath = junitFolderPath;
    }

    @Override
    public boolean start()
        throws Exception
    {
        FilePath filePath = getContext().get(FilePath.class);

        FilePath tctReport = filePath.child(tckReportTxtPath);
        List<String> lines = IOUtils.readLines( tctReport.read(), Charset.defaultCharset());
        final Map<String, List<TestResult>> resultPerClass = new TreeMap<>();

//        com/sun/ts/tests/servlet/api/javax_servlet/asynccontext/URLClient.java#asyncListenerTest1                                                                          Passed.
//        com/sun/ts/tests/servlet/api/javax_servlet/asynccontext/URLClient.java#asyncListenerTest6                                                                          Passed.
//        com/sun/ts/tests/servlet/api/javax_servlet/asynccontext/URLClient.java#dispatchContextPathTest                                                                     Passed.
//        com/sun/ts/tests/servlet/api/javax_servlet/asynccontext/URLClient.java#dispatchContextPathTest1                                                                    Passed.
//        com/sun/ts/tests/servlet/api/javax_servlet/asynccontext/URLClient.java#dispatchContextPathTest2                                                                    Passed.
//        com/sun/ts/tests/servlet/api/javax_servlet/asynccontext/URLClient.java#dispatchZeroArgTest                                                                         Passed.
//        com/sun/ts/tests/servlet/api/javax_servlet/asynccontext/URLClient.java#dispatchZeroArgTest1                                                                        Passed.
//        com/sun/ts/tests/servlet/api/javax_servlet/asynccontext/URLClient.java#dispatchZeroArgTest2                                                                        Passed.
//        com/sun/ts/tests/servlet/api/javax_servlet/asynccontext/URLClient.java#forwardTest1                                                                                Failed. Test case throws exception: [BaseUrlClient] forwardTest1 failed! Check output for cause of failure.
//        com/sun/ts/tests/servlet/api/javax_servlet/asynccontext/URLClient.java#getRequestTest                                                                              Passed.

        lines.forEach(line ->
        {
            String[] values = StringUtils.split(line);
            String className = StringUtils.substringBefore(values[0], "#");
            List<TestResult> results = resultPerClass.computeIfAbsent(className, k -> new ArrayList<>());
            String testName = StringUtils.substringAfter(values[0], "#");
            if (!StringUtils.startsWith(values[1], "Passed."))
            {
                results.add(new TestResult(testName, StringUtils.substringAfter(line, "Failed.")));
            }
            else
            {
                results.add(new TestResult(testName, null));
            }
        });


        FilePath reportsDirectory = filePath.child(junitFolderPath);
        if (!reportsDirectory.exists())
        {
            reportsDirectory.mkdirs();
        }
        for (Map.Entry<String, List<TestResult>> entry : resultPerClass.entrySet())
        {
            Xpp3Dom testsuite = new Xpp3Dom("testsuite");
            testsuite.setAttribute( "name", entry.getKey() );
            testsuite.setAttribute( "tests", Integer.toString( entry.getValue().size() ) );
            testsuite.setAttribute( "failures",
                                    Long.toString( entry.getValue().stream().filter( testResult -> testResult.failureMessage != null ).count() ));

            for(TestResult testResult : entry.getValue())
            {
                Xpp3Dom testcase = new Xpp3Dom("testcase");
                testsuite.addChild(testcase);
                testcase.setAttribute( "name", testResult.testName );
                testcase.setAttribute( "classname", entry.getKey() );
                if(testResult.failureMessage!=null)
                {
                    Xpp3Dom failure = new Xpp3Dom( "failure" );
                    testcase.addChild( failure );
                    failure.setAttribute( "message", testResult.failureMessage );
                }
            }
            try(OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(reportsDirectory.child( StringUtils.replace( entry.getKey(), "/", ".")
                                                                       + ".xml" ).write(), Charset.defaultCharset()))
            {
                Xpp3DomWriter.write( outputStreamWriter, testsuite );
            }
        }
        getContext().setResult(Result.SUCCESS);
        getContext().onSuccess(Result.SUCCESS);
        return true;
    }

    private static class TestResult
    {
        private final String testName;
        private final String failureMessage;

        public TestResult(String testName, String failureMessage)
        {
            this.testName = testName;
            this.failureMessage = failureMessage;
        }
    }
}
