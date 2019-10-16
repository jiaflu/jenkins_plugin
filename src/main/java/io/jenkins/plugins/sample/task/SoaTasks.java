package io.jenkins.plugins.sample.task;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.sample.util.ResponseResult;
import io.jenkins.plugins.sample.util.RestClient;
import io.jenkins.plugins.sample.util.RestClientImpl;
import io.jenkins.plugins.sample.util.UrlUtil;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Set;

public class SoaTasks extends Step {

    private String requestId;
    private String instance;
    private String task;
    private String ackRequired; // tools need send back message to soa, tools split by ,
    private String autoTools;

    @DataBoundConstructor
    public SoaTasks(@Nonnull String task, @Nullable String ackRequired,
                    @Nullable String requestId, @Nullable String instance, @Nullable String automate_tools) {
        this.task = task;
        this.requestId = requestId;
        this.instance = instance;
        this.ackRequired = ackRequired;
        this.autoTools = automate_tools;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new SoaTasks.Execution(this, context);
    }

    public String getRequestId() {
        return requestId;
    }

    public String getInstance() {
        return instance;
    }

    public String getTask() {
        return task;
    }

    public String getAckRequired() {
        return ackRequired;
    }

    public String getAutoTools() {
        return autoTools;
    }

    /**
     * Simple synchronized step execution.
     */

    private static class Execution extends SynchronousStepExecution<String> {
        private transient SoaTasks step;
        private Run run;

        Execution(@Nonnull SoaTasks step, StepContext context) throws IOException, InterruptedException {
            super(context);
            this.step = step;
            this.run = context.get(Run.class);
        }

        // 具体执行方法
        @Override
        protected String run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            PrintStream logger = listener.getLogger();

            String task = step.getTask();
            String requestId = step.getRequestId();
            String instance = step.getInstance();
            String ackRequired = step.getAckRequired();
            String autoTools = step.getAutoTools();

            int number = run.getNumber();

            String triggerTaskUrl = getTaskUrl(instance, task, requestId, ackRequired, autoTools, number);
            logger.println("soaTask(jiafenglu):\n" + triggerTaskUrl);

            RestClient restClient = new RestClientImpl();
            //ResponseResult responseResult = restClient.get(triggerTaskUrl, ResponseResult.class);
            restClient.get(triggerTaskUrl, ResponseResult.class);

            return "okla";
        }

        String getTaskUrl(String instance, String task, String requestId, String ackRequired, String autoTools, int number) {
//            return UrlUtil.getDomain(instance)
//                    + "/workflow/task?task=" + task
//                    + "&request_id=" + requestId
//                    + "&ack_required=" + ackRequired
//                    + "&automate_tools=" + autoTools
//                    + "&build_number=" + number;
            return UrlUtil.getDomain(instance)
                    + "/slm/healthcheck";
        }
    }

    //Jenkins 内部会扫描 @Extenstion 注解来知道注册了有哪些插件。
    @Extension
    public static class Descriptor extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }

        @Override
        public String getFunctionName() {
            return "soaTask";
        }

        @Override
        public String getDisplayName() {
            return "SOA Task Distributor";
        }
    }

}
