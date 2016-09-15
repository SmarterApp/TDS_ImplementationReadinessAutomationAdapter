package org.cresst.sb.irp.automation.adapter.engine.async.requestprocessor;

import org.cresst.sb.irp.automation.adapter.domain.AutomationRequest;
import org.cresst.sb.irp.automation.adapter.domain.AutomationRequestError;
import org.cresst.sb.irp.automation.adapter.domain.AutomationToken;
import org.cresst.sb.irp.automation.adapter.engine.async.requesthandler.AutomationRequestResultHandler;
import org.cresst.sb.irp.automation.adapter.engine.task.AutomationTaskRunner;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class is designed to run as a single instance in its own single thread to process IRP Automation requests.
 * The requests are submitted to a BlockingQueue and processed sequentially. It ensures that only a single
 * Adapter Automation process is run per vendor implementation at time.
 */
class AsyncAutomationRequestProcessor implements AutomationRequestProcessor, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(AsyncAutomationRequestProcessor.class);

    private volatile static boolean runProcessor = false;
    private final static Object lockObject = new Object();

    private final BlockingQueue<AutomationRequest> automationRequestQueue = new ArrayBlockingQueue<>(16);

    // Contains tokens for running automation requests
    private final static Set<AutomationToken> automationTokens = Collections.synchronizedSet(new HashSet<AutomationToken>());

    private final TaskExecutor taskExecutor;
    private final AutomationRequestResultHandler automationRequestResultHandler;
    private final AutomationStatusHandler automationStatusHandler;

    public AsyncAutomationRequestProcessor(TaskExecutor taskExecutor,
                                           AutomationRequestResultHandler automationRequestResultHandler,
                                           AutomationStatusHandler automationStatusHandler) {

        this.taskExecutor = taskExecutor;
        this.automationRequestResultHandler = automationRequestResultHandler;
        this.automationStatusHandler = automationStatusHandler;
    }

    /**
     * Starts the queue processor in its own thread
     */
    public void initializeProcessor() {
        taskExecutor.execute(this);
    }

    /**
     * Stops the queue processor
     */
    public void shutdownProcessor() {
        runProcessor = false;
    }

    @Override
    public void run() {
        synchronized (lockObject) {
            if (runProcessor) {
                return;
            }
            runProcessor = true;
        }
        processAutomationRequests();
    }

    @Override
    public void processAutomationRequest(AutomationRequest automationRequest) {
        queueAutomationRequest(automationRequest);
    }

    private void queueAutomationRequest(AutomationRequest automationRequest) {
        if (!automationRequestQueue.offer(automationRequest)) {
            sendAutomationStartError("Request queue is full", automationRequest);
        }
    }

    /**
     * Only one of these runs in the application.
     */
    private void processAutomationRequests()  {
        logger.info("Started Automation Request Processor");
        try {
            while (runProcessor) {
                final AutomationRequest automationRequest = automationRequestQueue.take();
                final AutomationToken automationToken = new AutomationToken(automationRequest);

                if (automationTokens.contains(automationToken)) {
                    logger.info("Duplicate automation requested. Sending existing automation token.");
                    sendAutomationToken(automationRequest, automationToken);
                } else {
                    if (startAutomationTask(automationRequest, automationToken)) {
                        logger.info("Started automation task for {} with token {}", automationRequest, automationToken);
                        automationTokens.add(automationToken);
                        sendAutomationToken(automationRequest, automationToken);
                    } else {
                        sendAutomationStartError("Task executor is busy", automationRequest);
                    }
                }
            }

            clearQueue();
        } catch (InterruptedException e) {
            logger.info("Ending AutomationRequest processor because blocking queue has been interrupted while waiting.");

            clearQueue();

            // Restore interrupt status
            Thread.currentThread().interrupt();
        }

        logger.info("Automation Request Processor stopped");
    }

    private void clearQueue() {
        final List<AutomationRequest> remainingAutomationRequests = new ArrayList<>();
        automationRequestQueue.drainTo(remainingAutomationRequests);

        for (AutomationRequest automationRequest : remainingAutomationRequests) {
            sendAutomationStartError("IRP is shutting down", automationRequest);
        }
    }

    private boolean startAutomationTask(final AutomationRequest automationRequest, final AutomationToken automationToken) {
        try {
            AutomationTaskRunner automationTaskRunner = new AutomationTaskRunner(automationRequest, automationToken, automationStatusHandler);
            automationTaskRunner.onCompletion(new Runnable() {
                @Override
                public void run() {
                    automationTokens.remove(automationToken);
                }
            });
            taskExecutor.execute(automationTaskRunner);
        } catch (TaskRejectedException e) {
            logger.error("Unable to start automation task", e);
            return false;
        }

        return true;
    }

    private void sendAutomationToken(final AutomationRequest automationRequest, final AutomationToken automationToken) {
        automationRequestResultHandler.handleAutomationRequestResult(automationRequest, automationToken);
    }

    private void sendAutomationStartError(final String message, final AutomationRequest automationRequest) {
        final String errorMessage = "Can't start IRP Automation - " + message;
        final AutomationRequestError automationRequestError = new AutomationRequestError(errorMessage, automationRequest);

        automationRequestResultHandler.handleAutomationRequestError(automationRequestError);
    }
}
