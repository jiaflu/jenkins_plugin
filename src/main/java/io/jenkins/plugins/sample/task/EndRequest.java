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

public class EndRequest extends Step {

    private String requestId;
    private String instance;


    @DataBoundConstructor
    public EndRequest(@Nonnull String instance, @Nonnull String requestId) {
        this.instance = instance;
        this.requestId = requestId;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {


        return new EndRequest.Execution(this, context);
    }

    public String getRequestId() {
        return requestId;
    }

    public String getInstance() {
        return instance;
    }

    private static class Execution extends SynchronousStepExecution<Void> {

        private transient EndRequest step;

        Execution(@Nonnull EndRequest step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception{

            TaskListener listener = getContext().get(TaskListener.class);
            PrintStream logger = listener.getLogger();

            String requestId = step.getRequestId();
            String instance = step.getInstance();


            String endWorkflowUrl = getSoaInitWorkflowUrl(instance, requestId);
            logger.println("soa_end_workflow:\n" + endWorkflowUrl);

            RestClient restClient = new RestClientImpl();
            restClient.get(endWorkflowUrl, null);

            return null;
        }



        String getSoaInitWorkflowUrl(String instance,String requestId) {
            return UrlUtil.getDomain(instance) + "/workflow/end?request_id=" + requestId;
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
            return "soa_end_workflow";
        }

        @Override
        public String getDisplayName() {
            return "End Workflow";
        }
    }
}

