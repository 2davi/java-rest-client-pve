package dev.the2davi.lab.cmmn.format;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class TypeUtil {
	private TypeUtil() {
		throw new IllegalStateException("Utility class");
	}
	
	public static String encodeUTF_8(Object value) {
		if(value == null || value.toString().isEmpty()) {
			return null;
		}
		return URLEncoder.encode(value.toString(), StandardCharsets.UTF_8);
	}
	
	public static <R> R encodeUTF_8(Object value, Function<String, R> after) {
		if(value == null || value.toString().isEmpty()) {
			return null;
		}
		String encoded = URLEncoder.encode(value.toString(), StandardCharsets.UTF_8);
		return after.apply(encoded);
	}
}
