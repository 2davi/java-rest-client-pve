package dev.the2davi.lab.monitor.recorder;

import dev.the2davi.lab.monitor.TaskOutcome;

/**
 * <pre>
 * 작업 결과를 기록하는 포트.
 * - SLF4J 어댑터에 기록할 수도, JdbcClient나 MyBatis로 갈아끼울 수도 있다.
 * - 절대 예외를 발생시키지 않는다. 기록에 실패했다고 해서 모니터링은 계속 이루어진다.
 *  
 * << 개정이력 >>
 *   
 *  수정일      수정자		수정내용
 *  ------------------------------------------------
 *  2026-06-11  kcy0122			최초 생성
 * </pre>
 */
public interface TaskOutcomeRecorder {
	void record(TaskOutcome outcome);
}
