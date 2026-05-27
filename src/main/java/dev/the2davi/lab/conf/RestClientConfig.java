package dev.the2davi.lab.conf;

import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;

@Configuration @EnableAsync
public class RestClientConfig {

	@Bean
	RestClient pveRestClient(
			@Value("${proxmox.api.url}") String apiUrl
			, @Value("${proxmox.api.token}") String apiToken
			, @Value("${proxmox.timeout.connect}") Integer connectTimeout
			, @Value("${proxmox.timeout.read}") Integer readTimeout) throws Exception {
		//인증서 검문소를 하이패스로 만든다.
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				@Override public X509Certificate[] getAcceptedIssuers() { return null; }
				@Override public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
				@Override public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
			}
		};
		
		//SSLContext 초기화
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustAllCerts, new SecureRandom());
		
		//+hostname이 다르든 말든
//		@SuppressWarnings("unused")
//		HostnameVerifier allHostValid = (_hostname, _session) -> true;
		System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
		//IPv6 헛발질 방지
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		//?
//		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
//			@Override
//			protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException{
//				if(connection instanceof HttpsURLConnection httpsConnection) {
//					httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
//					httpsConnection.setHostnameVerifier(allHostValid);
//				}
//				super.prepareConnection(connection, httpMethod);
//			}
//		};
		HttpClient httpClient = HttpClient.newBuilder()
				.sslContext(sslContext)
				.connectTimeout(Duration.ofSeconds(connectTimeout))
				.version(HttpClient.Version.HTTP_1_1)
				.followRedirects(HttpClient.Redirect.NORMAL)
				//System Proxy 사용 비활성
				.proxy(HttpClient.Builder.NO_PROXY)
				//JAVA 21+: API 요청마다 가상 스레드를 사용한다.
				.executor(Executors.newVirtualThreadPerTaskExecutor())
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(Duration.ofSeconds(readTimeout));
		
		//완성된 RestClient를 Bean으로 등록
		return RestClient.builder()
					.requestFactory(requestFactory)
					.baseUrl(apiUrl)
					.defaultHeader("Authorization", apiToken)
					.build();
	}
}
