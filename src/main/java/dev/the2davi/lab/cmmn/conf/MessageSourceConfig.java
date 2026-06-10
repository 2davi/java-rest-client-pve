package dev.the2davi.lab.cmmn.conf;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class MessageSourceConfig {
	
	private static final String EXCEPTION_MESSAGES = "classpath:messages/exception-messages";
	private static final String VALIDATION_MESSAGES = "classpath:messages/validation-messages";
	private static final Integer MESSAGE_CACHE_SECONDS = 3600;
	
	@Bean
	LocaleResolver localeResolver() {
		AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
		localeResolver.setDefaultLocale(Locale.KOREAN);
		return localeResolver;
	}
	
	@Bean
	MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames(EXCEPTION_MESSAGES, VALIDATION_MESSAGES);
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		messageSource.setDefaultLocale(Locale.KOREAN);
		messageSource.setFallbackToSystemLocale(false);
		messageSource.setCacheSeconds(MESSAGE_CACHE_SECONDS);
		return messageSource;
	}
	
	@Bean
	MessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
		return new MessageSourceAccessor(messageSource);
	}
}
