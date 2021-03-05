package de.novatec.bpm.listener;

import de.novatec.bpm.variable.VariableHandler;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndEventListener implements ExecutionListener {

    private final Logger logger = LoggerFactory.getLogger(EndEventListener.class);

    @Override
    public void notify(DelegateExecution execution) {
        if (execution.getEventName().equals(EndEventListener.EVENTNAME_END)) {
            if (execution.getCurrentFlowElement().getName().contains("failed")) {
                logger.info("End event with status failed was reached");
                VariableHandler.setReservationSuccess(execution, false);
            } else {
                logger.info("End event with status success was reached");
                VariableHandler.setReservationSuccess(execution, true);
            }
        }
    }
}