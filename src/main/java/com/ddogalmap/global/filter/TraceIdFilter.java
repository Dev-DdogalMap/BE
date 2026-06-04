package com.ddogalmap.global.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class TraceIdFilter implements Filter {

	public static final String TRACE_ID = "traceId";
	public static final String REQUEST_ID = "requestId";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		try {

			String traceId = UUID.randomUUID().toString();

			MDC.put(TRACE_ID, traceId);

			MDC.put("method", httpRequest.getMethod());
			MDC.put("uri", httpRequest.getRequestURI());

			chain.doFilter(request, response);

		} finally {

			MDC.clear();
		}
	}
}