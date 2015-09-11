package com.github.t1.restproxy;

import static ch.qos.logback.classic.Level.*;
import static com.github.t1.restproxy.Config.RecorderConfig.*;
import static com.github.t1.restproxy.ProxyServiceRule.*;
import static com.github.t1.restproxy.TestTools.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.*;
import static org.junit.Assert.*;

import org.junit.*;

import com.github.t1.rest.*;
import com.github.t1.rest.UriTemplate.NonQuery;
import com.github.t1.restproxy.EchoResource.Echo;

public class ProxyIT {
    private static final String HTTP_CLIENT_4_5 = "Apache-HttpClient/4.5";
    private static final String HTTP_CLIENT_4_5_CACHE = "1.1 localhost (" + HTTP_CLIENT_4_5 + " (cache))";
    private static final String JAVA_VERSION = System.getProperty("java.version");

    @ClassRule
    public static final ProxyServiceRule SERVICE = new ProxyServiceRule();

    private final NonQuery BASE = UriTemplate.from(SERVICE.baseUri()).nonQuery();
    private final NonQuery ECHO = BASE.path("-echo");
    private final RestResource CONFIG = new RestResource(BASE.path("-config"));

    private void assertDropwizard404(EntityResponse<String> response) {
        assertTrue(TEXT_HTML_TYPE.isCompatible(response.expecting(NOT_FOUND).contentType()));
    }

    @Before
    public void init() {
        // logger("org.apache.http.wire").setLevel(DEBUG);
        // logger("com.github.t1.rest").setLevel(DEBUG);
        // logger("com.github.t1.rest.ResponseConverter").setLevel(INFO);
        logger("com.github.t1.rest.RestClientRecorder").setLevel(DEBUG);
        logger("com.github.t1.restproxy").setLevel(DEBUG);
    }

    @After
    public void cleanup() {
        RestClientRecorder.Recordings.clearAll();
    }


    @Test
    public void shouldGetDirectlyFromTarget() {
        Echo response = new RestResource(ECHO.path("foo").path("bar")).GET(Echo.class);

        assertEquals("foo/bar", response.getPath());
    }

    @Test
    public void shouldNotGetProxiedResourceWithMinus() {
        Config config = Config.named("proxy target");
        CONFIG.PUT(config);

        EntityResponse<String> response = new RestResource(BASE.path("-nowhere")).GET_Response(String.class);

        assertDropwizard404(response);
    }

    @Test
    public void shouldGetUnconfiguredProxiedResource() {
        EntityResponse<String> response = new RestResource(BASE.path("somewhere")).GET_Response(String.class);

        assertEquals("path not configured: somewhere", response.expecting(NOT_FOUND).get());
    }

    @Test
    public void shouldGetProxiedResource() {
        CONFIGS.add(Config.named("foo").withTarget(ECHO));

        Echo response = new RestResource(BASE.path("foo").path("bar")).GET(Echo.class);

        assertEquals("GET", response.getMethod());
        assertEquals("bar", response.getPath());
        assertThat(response.getHeaders()) //
                .contains(entry("Accept", "text/plain; text/*; application/*; application/json")) //
                .contains(entry("Host", ECHO.authority())) //
                .contains(entry("User-Agent", HTTP_CLIENT_4_5 + " (Java/" + JAVA_VERSION + ")")) //
                .contains(entry("Via", HTTP_CLIENT_4_5_CACHE + "; " + HTTP_CLIENT_4_5_CACHE)) //
                ;
    }

    @Test
    public void shouldNotCacheRequestWhenNotConfigured() {
        CONFIGS.add(Config.named("foo").withTarget(ECHO));

        RestResource resource = new RestResource(BASE.path("foo").path("bar"));
        Echo firstResponse = resource.GET(Echo.class);
        Echo secondResponse = resource.GET(Echo.class);

        assertNotEquals(firstResponse.toString(), secondResponse.toString());
    }

    @Test
    public void shouldCacheRequestWhenConfigured() {
        CONFIGS.add(Config.named("foo").withPersistent(false).withTarget(ECHO).withRecorder(recorder()));

        RestResource resource = new RestResource(BASE.path("foo").path("bar"));
        Echo firstResponse = resource.GET(Echo.class);
        Echo secondResponse = resource.GET(Echo.class);

        assertEquals(firstResponse.toString(), secondResponse.toString());
    }
}
