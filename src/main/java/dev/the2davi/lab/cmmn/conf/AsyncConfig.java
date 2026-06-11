package dev.the2davi.lab.cmmn.conf;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {
	
	@Override
	public Executor getAsyncExecutor() {
		// Java 21 Virtual Thread: sleep·blocking IO 용도
		SimpleAsyncTaskExecutor delegate = new SimpleAsyncTaskExecutor("pve-async-");
		delegate.setVirtualThreads(true);
		
		//SecurityContext를 워커 스레드로 복제·전파하는 겉 껍데기
		return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
	}
}
