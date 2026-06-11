package dev.the2davi.lab.audit.recorder.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import dev.the2davi.lab.audit.TaskOutcome;
import dev.the2davi.lab.audit.recorder.TaskOutcomeRecorder;

@Component
public class Slf4jTaskOutcomeRecorder implements TaskOutcomeRecorder {

	private static final Logger log = LoggerFactory.getLogger(Slf4jTaskOutcomeRecorder.class);
	
	@Override
	public void record(TaskOutcome o) {
		log.info("TASK_OUTCOME status={} type={} target={} node={} durationMs={} exit=\"{}\" upid={}",
				o.status(), o.action(), o.targetId(), o.node(), o.durationMs(), o.exitStatus(), o.upid());
	}
}
