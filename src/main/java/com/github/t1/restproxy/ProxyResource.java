package com.github.t1.restproxy;

import static com.github.t1.log.LogLevel.*;
import static java.util.Arrays.*;
import static java.util.Locale.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

import java.util.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.github.t1.log.Logged;
import com.github.t1.rest.*;
import com.github.t1.rest.Headers.Header;

import lombok.extern.slf4j.Slf4j;

@Path("/{path:[^-].*}")
@Logged(level = INFO)
@Slf4j
public class ProxyResource {
    private static final List<String> NON_PROXIABLE_HEADERS = asList("host", "connection");

    @Inject
    Configs configs;

    @Inject
    Contexts contexts;

    @Context
    HttpServletRequest servletRequest;

    @GET
    public Response get(@PathParam("path") String path) {
        Optional<Config> found = configs.get(path);
        if (!found.isPresent())
            return Response.status(NOT_FOUND).type(TEXT_PLAIN).entity("path not configured: " + path).build();
        Config config = found.get();
        RestRequest<String> outRequest = request(config, path);
        EntityResponse<String> outResponse = outRequest.GET_Response();
        return response(outResponse);
    }

    private RestRequest<String> request(Config config, String path) {
        UriTemplate target = config.getTarget();
        log.debug("proxy to {} with path {}", target, path);
        UriTemplate uri = target.nonQuery().path(config.resolve(path));
        RestRequest<String> outRequest = contexts.get(config).createResource(uri).request().accept(String.class);
        for (Enumeration<String> e = servletRequest.getHeaderNames(); e.hasMoreElements();) {
            String headerName = e.nextElement();
            if (NON_PROXIABLE_HEADERS.contains(headerName.toLowerCase(US)))
                continue;
            String headerValue = servletRequest.getHeader(headerName);
            log.debug("proxy header {}: {}", headerName, headerValue);
            outRequest = outRequest.header(headerName, headerValue);
            // TODO add Via header
        }
        return outRequest;
    }

    private Response response(EntityResponse<String> response) {
        ResponseBuilder result = Response.status(response.status());
        for (Header header : response.headers())
            result.header(header.name(), header.value());
        result.entity(response.get());
        return result.build();
    }
}
