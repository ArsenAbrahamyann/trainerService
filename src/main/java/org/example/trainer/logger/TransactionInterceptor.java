package org.example.trainer.logger;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Interceptor for generating and managing a unique transaction ID for each request.
 * This ID can be used for logging and tracing purposes throughout the request lifecycle.
 */
@Component("customTransactionInterceptor")
public class TransactionInterceptor implements HandlerInterceptor {

    private static final String TRANSACTION_ID_KEY = "transactionId";

    /**
     * Pre-handle method executed before the request is processed.
     * Checks if a transaction ID exists in the session; if not, generates a new one
     * and stores it in both the session and MDC for tracing.
     *
     * @param request  the incoming {@link HttpServletRequest}
     * @param response the outgoing {@link HttpServletResponse}
     * @param handler  the handler responsible for processing the request
     * @return {@code true} to continue processing the request
     * @throws Exception if any error occurs
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        HttpSession session = request.getSession();

        String transactionId = (String) session.getAttribute(TRANSACTION_ID_KEY);
        if (transactionId == null) {
            transactionId = UUID.randomUUID().toString();
            session.setAttribute(TRANSACTION_ID_KEY, transactionId);
        }

        MDC.put(TRANSACTION_ID_KEY, transactionId);
        return true;
    }

    /**
     * After-completion method executed after the request has been processed.
     * This method removes the transaction ID from the MDC to prevent leaking into other requests.
     *
     * @param request  the processed {@link HttpServletRequest}
     * @param response the processed {@link HttpServletResponse}
     * @param handler  the handler that processed the request
     * @param ex       any exception that occurred during processing (may be {@code null})
     * @throws Exception if any error occurs
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) throws Exception {
        MDC.remove(TRANSACTION_ID_KEY);
    }

}
