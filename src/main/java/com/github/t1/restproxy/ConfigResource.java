package com.github.t1.restproxy;

import static com.github.t1.log.LogLevel.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.github.t1.log.Logged;

import lombok.extern.slf4j.Slf4j;

@Path("/-configs")
@Slf4j
@Logged(level = INFO)
public class ConfigResource {
    @Inject
    Configs configs;

    @GET
    public Response getAll(@Context UriInfo uriInfo) {
        Map<String, String> map = new LinkedHashMap<>();
        log.debug("get {} configs", configs.getConfigs().size());
        for (Config config : configs.getConfigs())
            map.put(config.getName(), uriInfo.getBaseUriBuilder().path(config.getName()).build().toString());
        log.debug("return {}", map);
        return Response.ok(map).build();
    }

    @GET
    @Path("{name}")
    public Response get(@PathParam("name") String name) {
        Optional<Config> found = configs.get(name);
        if (!found.isPresent())
            return Response.status(NOT_FOUND).type(TEXT_PLAIN).entity("no config with name: " + name).build();
        return Response.ok(found.get()).build();
    }

    @POST
    public void post(Config config) {
        log.debug("post config: {}", config);
        configs.add(config);
        log.debug("now have {} configs", configs.getConfigs().size());
    }

    @PUT
    @Path("{name}")
    public void put(@PathParam("name") String name, Config config) {
        log.debug("put config: {}", config);
        configs.remove(name);
        configs.add(config);
        log.debug("now have {} configs", configs.getConfigs().size());
    }

    @DELETE
    @Path("{name}")
    public void delete(@PathParam("name") String name) {
        log.debug("delete config: {}", name);
        configs.remove(name);
        log.debug("now have {} configs", configs.getConfigs().size());
    }
}
