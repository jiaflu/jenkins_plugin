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
import java.util.Set;

public class ValidateRequestParams extends Step {

    private String requestId;
    private String slmUrl;

    @DataBoundConstructor
    public ValidateRequestParams(@Nonnull String requestId, @Nonnull String slmUrl) {
        this.slmUrl = slmUrl;
        this.requestId = requestId;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new ValidateRequestParams.Execution(this, context);
    }


    public String getRequestId() {
        return requestId;
    }

    public String getSlmUrl() {
        return slmUrl;
    }


    /**
     * Simple synchronous step execution.
     */
    private static class Execution extends SynchronousStepExecution<String> {

        private transient ValidateRequestParams step;

        /**
         * Execution Constructor
         *
         * @param step    step
         * @param context the step context
         */
        Execution(@Nonnull ValidateRequestParams step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected String run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            PrintStream logger = listener.getLogger();

            String slmUrl = step.getSlmUrl();
            String requestId = step.getRequestId();

            String validateUrl = getValidationUrl(requestId, slmUrl);
            logger.println("soa_validate_req_params: \n" + validateUrl);

            RestClient restClient = new RestClientImpl();
            ResponseResult responseResult =
                    restClient.get(validateUrl, ResponseResult.class);

            return String.valueOf(responseResult.getMessage());
        }

        String getValidationUrl(String requestId, String slmUrl) {
            return UrlUtil.getDomain(slmUrl)
                    + "/request/input/validate?request_id=" + requestId;
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
            return "soa_validate_req_params";
        }

        @Override
        public String getDisplayName() {
            return "SOA Validate Request Parameters";
        }
    }
}
