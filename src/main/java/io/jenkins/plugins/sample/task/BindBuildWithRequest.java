package io.jenkins.plugins.sample.task;

import hudson.Extension;
import hudson.model.TaskListener;
import io.jenkins.plugins.sample.util.RestClient;
import io.jenkins.plugins.sample.util.RestClientImpl;
import io.jenkins.plugins.sample.util.UrlUtil;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Set;

public class BindBuildWithRequest extends Step {

    private String buildNumber;
    private String requestId;
    private String instance;


    @DataBoundConstructor
    public BindBuildWithRequest(@Nonnull String instance, @Nonnull String buildNumber, @Nonnull String requestId) {
        this.instance = instance;
        this.buildNumber = buildNumber;
        this.requestId = requestId;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {


        return new BindBuildWithRequest.Execution(this, context);
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getInstance() {
        return instance;
    }

    private static class Execution extends SynchronousStepExecution<Void> {

        private transient BindBuildWithRequest step;

        Execution(@Nonnull BindBuildWithRequest step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception{

            TaskListener listener = getContext().get(TaskListener.class);
            PrintStream logger = listener.getLogger();

            String buildNumber = step.getBuildNumber();
            String requestId = step.getRequestId();
            String instance = step.getInstance();

            String initWorkflowUrl = getSoaInitWorkflowUrl(instance, buildNumber, requestId);
            logger.println("soa_init_workflow:\n" + initWorkflowUrl);

            RestClient restClient = new RestClientImpl();
            restClient.get(initWorkflowUrl, null);

            return null;
        }


        String getSoaInitWorkflowUrl(String instance, String buildNumber,String requestId) {
            return UrlUtil.getDomain(instance)
                    + "/workflow/init?build_number=" + buildNumber
                    + "&request_id=" + requestId;
        }

    }

    @Extension
    public static class Descriptor extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }

        @Override
        public String getFunctionName() {
            return "soa_init_workflow";
        }

        @Override
        public String getDisplayName() {
            return "Bind Build Number With Request";
        }
    }
}

