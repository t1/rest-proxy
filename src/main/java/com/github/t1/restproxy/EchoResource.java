package com.github.t1.restproxy;

import static com.github.t1.log.LogLevel.*;
import static lombok.AccessLevel.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import com.github.t1.log.Logged;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Path("/-echo")
@Slf4j
@Logged(level = INFO)
public class EchoResource {
    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    @NoArgsConstructor(force = true, access = PRIVATE)
    public static class Echo {
        String method;
        String path;
        Map<String, String> headers;
        String body;
    }

    @Context
    HttpServletRequest servletRequest;

    @GET
    @Path("{path:.*}")
    public Echo get(@PathParam("path") String path, String body) {
        log.debug("echo {}", path);
        return Echo.builder() //
                .method("GET") //
                .path(path) //
                .headers(headers()) //
                .body(body) //
                .build();
    }

    private Map<String, String> headers() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Enumeration<String> headers = servletRequest.getHeaderNames(); headers.hasMoreElements();) {
            String headerName = headers.nextElement();
            StringBuilder headerValues = new StringBuilder();
            for (Enumeration<String> values = servletRequest.getHeaders(headerName); values.hasMoreElements();) {
                String headerValue = values.nextElement();
                if (headerValues.length() > 0)
                    headerValues.append("; ");
                headerValues.append(headerValue);
            }
            map.put(headerName, headerValues.toString());
        }
        return map;
    }
}
