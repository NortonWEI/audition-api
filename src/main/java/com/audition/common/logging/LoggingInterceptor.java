package com.audition.common.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);
    private final AuditionLogger auditionLogger;

    public LoggingInterceptor(AuditionLogger auditionLogger) {
        this.auditionLogger = auditionLogger;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
        ClientHttpRequestExecution execution)
        throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        setTracingHeaders(response);
        logResponse(response);

        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        try {
            String reqBodyString =
                body != null && body.length > 0 ? new String(body, StandardCharsets.UTF_8) : StringUtils.EMPTY;
            String logMessage = String.format(
                "Request ==> %nMethod: %s, %nURI: %s, %nHeaders: %s, %nBody: %s",
                request.getMethod(), request.getURI(), request.getHeaders(), reqBodyString);
            auditionLogger.info(LOGGER, logMessage);
        } catch (Exception e) {
            // Catch any exception during logging to avoid affecting the main flow
            auditionLogger.logErrorWithException(LOGGER, "Failed to log request", e);
        }
    }

    private void logResponse(ClientHttpResponse response) {
        try {
            // copy response body to allow reading it without consuming the stream
            byte[] resBody = StreamUtils.copyToByteArray(response.getBody());
            String resBodyString =
                resBody.length > 0 ? new String(resBody, StandardCharsets.UTF_8) : StringUtils.EMPTY;
            String logMessage = String.format(
                "Response <== %nStatus Code: %s, %nHeaders: %s, %nBody: %s",
                response.getStatusCode(), response.getHeaders(), resBodyString);
            auditionLogger.info(LOGGER, logMessage);
        } catch (Exception e) {
            // Catch any exception during logging to avoid affecting the main flow
            auditionLogger.logErrorWithException(LOGGER, "Failed to log response", e);
        }
    }

    private void setTracingHeaders(ClientHttpResponse response) {
        String traceId = MDC.get("traceId");
        String spanId = MDC.get("spanId");
        if (StringUtils.isNotEmpty(traceId)) {
            response.getHeaders().set("X-Trace-Id", traceId);
        }
        if (StringUtils.isNotEmpty(spanId)) {
            response.getHeaders().set("X-Span-Id", spanId);
        }
    }
}
