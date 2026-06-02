package com.ddogalmap.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

	private static final String START_TIME = "startTime";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

		request.setAttribute(START_TIME, System.currentTimeMillis());

		log.info("[REQUEST] {} {}", request.getMethod(), request.getRequestURI());

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

		Long startTime = (Long) request.getAttribute(START_TIME);

		long duration = System.currentTimeMillis() - startTime;

		if (ex == null) {

			log.info("[RESPONSE] {} {} status={} duration={}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);

		} else {

			log.error("[ERROR] {} {} status={} duration={}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), duration, ex);

		}
	}
}