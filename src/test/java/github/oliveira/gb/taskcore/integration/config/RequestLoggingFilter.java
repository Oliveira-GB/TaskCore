package github.oliveira.gb.taskcore.integration.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("=== REQUEST ===");
        System.out.println(httpRequest.getMethod() + " " + httpRequest.getRequestURI());
        System.out.println("Headers: " + java.util.Collections.list(httpRequest.getHeaderNames()));

        chain.doFilter(request, response);

        System.out.println("=== RESPONSE ===");
        System.out.println("Status: " + httpResponse.getStatus());
    }
}