package com.github.t1.restproxy;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javax.ws.rs.core.MediaType.*;

import java.io.IOException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebFilter("/*")
public class DefaultAcceptHeaderFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String method = request.getMethod();
        String accept = request.getHeader("Accept");
        log.debug("doFilter {} with Accept header: {}", method, accept);
        if ("GET".equals(method) && (accept == null || WILDCARD.equals(accept)))
            request = withHeader(request, "Accept", APPLICATION_JSON);
        chain.doFilter(request, response);
    }

    private HttpServletRequest withHeader(HttpServletRequest request, String headerName, String headerValue) {
        log.debug("adding default header {}: {}", headerName, headerValue);
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (headerName.equals(name))
                    return headerValue;
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                if (!names.contains(headerName))
                    names.add(headerName);
                return Collections.enumeration(names);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (headerName.equals(name))
                    return enumeration(asList(headerValue));
                return super.getHeaders(name);
            }

            // TODO overwrite getIntHeader & getDateHeader
        };
    }
}
