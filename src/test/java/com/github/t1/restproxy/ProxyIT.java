package com.github.t1.restproxy;

import static ch.qos.logback.classic.Level.*;
import static com.github.t1.restproxy.TestTools.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.*;
import static org.junit.Assert.*;

import org.glassfish.jersey.filter.LoggingFilter;
import org.junit.*;

import com.github.t1.rest.*;
import com.github.t1.rest.UriTemplate.NonQuery;
import com.github.t1.restproxy.EchoResource.Echo;

import io.dropwizard.testing.junit.DropwizardClientRule;
import lombok.extern.java.Log;

@Log
public class ProxyIT {
    private static final String HTTP_CLIENT_4_5 = "Apache-HttpClient/4.5";
    private static final String HTTP_CLIENT_4_5_CACHE = "1.1 localhost (" + HTTP_CLIENT_4_5 + " (cache))";
    private static final String JAVA_VERSION = System.getProperty("java.version");

    @ClassRule
    public static final DropwizardClientRule TARGET = new DropwizardClientRule( //
            new LoggingFilter(log, true), //
            new EchoResource());
    @ClassRule
    public static final ProxyServiceRule SERVICE = new ProxyServiceRule();

    private final NonQuery TARGET_BASE = UriTemplate.from(TARGET.baseUri()).nonQuery().path("-echo");
    private final NonQuery PROXY_BASE = UriTemplate.from(SERVICE.baseUri()).nonQuery();
    private final RestResource CONFIG = new RestResource(PROXY_BASE.path("-config"));

    private void assertDropwizard404(EntityResponse<String> response) {
        assertTrue(TEXT_HTML_TYPE.isCompatible(response.expecting(NOT_FOUND).contentType()));
    }

    @Before
    public void init() {
        // logger("org.apache.http.wire").setLevel(DEBUG);
        // logger("com.github.t1.rest").setLevel(DEBUG);
        logger("com.github.t1.restproxy").setLevel(DEBUG);
    }

    @Test
    public void shouldGetDirectlyFromTarget() {
        Echo response = new RestResource(TARGET_BASE.path("foo").path("bar")).GET(Echo.class);

        assertEquals("foo/bar", response.getPath());
    }

    @Test
    public void shouldNotGetProxiedResourceWithMinus() {
        Config config = Config.named("proxy target");
        CONFIG.PUT(config);

        EntityResponse<String> response = new RestResource(PROXY_BASE.path("-nowhere")).GET_Response(String.class);

        assertDropwizard404(response);
    }

    @Test
    public void shouldGetUnconfiguredProxiedResource() {
        EntityResponse<String> response = new RestResource(PROXY_BASE.path("somewhere")).GET_Response(String.class);

        assertEquals("path not configured: somewhere", response.expecting(NOT_FOUND).get());
    }

    @Test
    public void shouldGetProxiedResource() {
        ProxyServiceRule.CONFIGS.add(Config.named("foo").withTarget(TARGET_BASE));

        Echo response = new RestResource(PROXY_BASE.path("foo").path("bar")).GET(Echo.class);

        assertEquals("GET", response.getMethod());
        assertEquals("bar", response.getPath());
        assertThat(response.getHeaders()) //
                .contains(entry("Accept", "text/plain; text/*; application/*; application/json")) //
                .contains(entry("Host", TARGET_BASE.authority())) //
                .contains(entry("User-Agent", HTTP_CLIENT_4_5 + " (Java/" + JAVA_VERSION + ")")) //
                .contains(entry("Via", HTTP_CLIENT_4_5_CACHE + "; " + HTTP_CLIENT_4_5_CACHE)) //
                ;
    }
}
