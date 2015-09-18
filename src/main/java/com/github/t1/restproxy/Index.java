package com.github.t1.restproxy;

import static javax.ws.rs.core.MediaType.*;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.swagger.annotations.*;
import lombok.*;

@SwaggerDefinition( //
        info = @Info( //
                title = "Rest-Proxy", //
                description = "", //
                version = "", //
                license = @License( //
                        name = "Apache License 2.0", //
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html" //
) //
) , //
        basePath = "http://localhost:8080/rest-proxy", //
        externalDocs = @ExternalDocs( //
                value = "see also on github:", //
                url = "http://github.com/t1/rest-proxy" //
) , //
        consumes = { APPLICATION_JSON, "application/yaml", APPLICATION_XML }, //
        produces = { APPLICATION_JSON, "application/yaml", APPLICATION_XML } //
)
@Path("/")
@Api(tags = "root")
public class Index {
    /** The JAX-RS Link class requires 2.0 */
    @Value
    @Builder
    static class Link {
        String uri;
        String rel;
        String title;
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Path("/index")
    @ApiOperation("list entry points into the app")
    public List<Link> getIndexList() {
        List<Link> list = new ArrayList<>();
        list.add(link(ConfigResource.class));
        list.add(link(EchoResource.class));
        list.add(link(ProxyResource.class));
        return list;
    }

    private Link link(Class<?> resource) {
        return Link.builder().uri(uri(resource)).rel(rel(resource)).build();
    }

    private String uri(Class<?> resource) {
        return uriInfo.getBaseUriBuilder().path(resource)
                .build("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "").toString();
    }

    private String rel(Class<?> resource) {
        String rel = resource.getAnnotation(Path.class).value();
        while (rel.startsWith("-") || rel.startsWith("/"))
            rel = rel.substring(1);
        return rel;
    }
}
