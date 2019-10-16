package io.jenkins.plugins.sample.task;

import hudson.Extension;
import hudson.model.TaskListener;
import io.jenkins.plugins.sample.util.ResponseResult;
import io.jenkins.plugins.sample.util.RestClient;
import io.jenkins.plugins.sample.util.RestClientImpl;
import io.jenkins.plugins.sample.util.UrlUtil;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RenderWorkflow extends Step {

    private String requestId;
    private String instance;


    @DataBoundConstructor
    public RenderWorkflow(@Nonnull String instance, @Nonnull String requestId) {
        this.requestId = requestId;
        this.instance = instance;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new RenderWorkflow.Execution(this, context);
    }

    public String getRequestId() {
        return requestId;
    }

    public String getInstance() {
        return instance;
    }

    /**
     * Simple synchronous step execution.
     */
    private static class Execution extends SynchronousStepExecution<ResponseResult<List>> {
        private transient RenderWorkflow step;

        /**
         * Execution Constructor
         *
         * @param step    step
         * @param context the step context
         */
        Execution(@Nonnull RenderWorkflow step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected ResponseResult<List> run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            PrintStream logger = listener.getLogger();

            String requestId = step.getRequestId();
            String instance = step.getInstance();

            String triggerTaskUrl = getTaskUrl(instance, requestId);

            logger.println("renderWorkflow:\n" + triggerTaskUrl);

            RestClient restClient = new RestClientImpl();
            ResponseResult responseResult = restClient.get(triggerTaskUrl, ResponseResult.class);

            return responseResult;
        }


        String getTaskUrl(String instance, String requestId) {
            return UrlUtil.getDomain(instance)
                    + "/workflow/render?request_id=" + requestId;
        }

    }


    /**
     * Standard Descriptor.
     */
    @Extension
    public static class Descriptor extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }

        @Override
        public String getFunctionName() {
            return "soa_render_workflow";
        }

        @Override
        public String getDisplayName() {
            return "SOA Render Workflow";
        }
    }
}

