package dev.the2davi.lab.cmmn.conf;

import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;

import io.micrometer.common.util.StringUtils;

@Configuration @EnableAsync
public class RestClientConfig {

	@Bean
	RestClient pveRestClient(
			@Value("${proxmox.api.url}") String apiUrl
			, ClientHttpRequestFactory pveRequestFactory) throws Exception {
		
		//* 2026-06-10; 동적 인증 인터셉터 정의
		//return문에서 `defaultHeader`에 apiToken을 동적으로 가져와 박아놓도록.
		ClientHttpRequestInterceptor dynamicAuthInterceptor = (request, body, execution) -> {
			//현재 Thread에 박혀있는 인증 정보 읽기
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if(auth != null && auth.getDetails() instanceof Map<?, ?> details) {
				//만약 인증정보가 이미 존재하면, 읽어와서 그대로 재활용
				String pveTicket = (String) details.get("pve_ticket");
				String pveCsrf = (String) details.get("pve_csrf");
				
				if(pveTicket != null && StringUtils.isNotBlank(pveTicket)) {
					request.getHeaders().add("Cookie", String.format("PVEAuthCookie=%s", pveTicket));
				}
				if(pveCsrf != null && StringUtils.isNotBlank(pveCsrf)) {
					request.getHeaders().add("CSRFPreventionToken", pveCsrf);
				}
			}
			
			return execution.execute(request, body);
			
			//JwtAuthenticationFilter
		};
		
		//완성된 RestClient를 Bean으로 등록
		return RestClient.builder()
					.requestFactory(pveRequestFactory)
					.baseUrl(apiUrl)
					.requestInterceptor(dynamicAuthInterceptor)
					.build();
	}
	
	@Bean
	ClientHttpRequestFactory pveRequestFactory(
			@Value("${proxmox.timeout.connect}") Integer connectTimeout
			, @Value("${proxmox.timeout.read}") Integer readTimeout) throws Exception {
		
		TrustManager[] trustAllCerts = {
				new X509TrustManager() {
					@Override public X509Certificate[] getAcceptedIssuers() { return null; }
					@Override public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
					@Override public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
				}
		};
		
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustAllCerts, new SecureRandom());
		System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		HttpClient httpClient = HttpClient.newBuilder()
				.sslContext(sslContext)
				.connectTimeout(Duration.ofSeconds(connectTimeout))
				.version(HttpClient.Version.HTTP_1_1)
				.proxy(HttpClient.Builder.NO_PROXY)
				.executor(Executors.newVirtualThreadPerTaskExecutor())
				.build();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(Duration.ofSeconds(readTimeout));
		return factory;
	}
}
