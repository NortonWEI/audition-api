package com.audition.configuration;

import com.audition.common.logging.AuditionLogger;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ResponseHeaderInjector extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHeaderInjector.class);
    private final AuditionLogger auditionLogger;

    public ResponseHeaderInjector(AuditionLogger auditionLogger) {
        this.auditionLogger = auditionLogger;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        try {
            Span currentSpan = Span.current();
            SpanContext ctxSpan = currentSpan.getSpanContext();
            if (ctxSpan != null && ctxSpan.isValid()) {
                response.setHeader("X-Trace-Id", ctxSpan.getTraceId());
                response.setHeader("X-Span-Id", ctxSpan.getSpanId());
            }
        } catch (Exception e) {
            // Log the exception if a OpenTelemetry has error occurs
            auditionLogger.logErrorWithException(LOGGER,
                "Failed to inject OpenTelemetry trace/span IDs into response headers", e);
        }
        filterChain.doFilter(request, response);
    }
}
