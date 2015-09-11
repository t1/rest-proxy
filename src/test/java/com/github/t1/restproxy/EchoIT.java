package com.github.t1.restproxy;

import static ch.qos.logback.classic.Level.*;
import static com.github.t1.restproxy.TestTools.*;

import org.assertj.core.api.JUnitSoftAssertions;
import org.glassfish.jersey.filter.LoggingFilter;
import org.junit.*;

import com.github.t1.rest.RestResource;
import com.github.t1.restproxy.EchoResource.Echo;

import io.dropwizard.testing.junit.DropwizardClientRule;
import lombok.extern.java.Log;

@Log
public class EchoIT {
    @ClassRule
    public static final DropwizardClientRule ECHO = new DropwizardClientRule(//
            new LoggingFilter(log, true), //
            new EchoResource() //
    );

    private final RestResource echoResource = new RestResource(ECHO.baseUri() + "/-echo/foo");

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void init() {
        logger("com.github.t1.restproxy").setLevel(DEBUG);
    }

    @Test
    public void shouldEchoGET() {
        Echo echo = echoResource.header("fooheader", "bar").GET(Echo.class);

        softly.assertThat(echo.getId()).isNotNull();
        softly.assertThat(echo.getMethod()).isEqualTo("GET");
        softly.assertThat(echo.getPath()).isEqualTo("foo");
        softly.assertThat(echo.getHeaders().get("fooheader")).isEqualTo("bar");
        softly.assertThat(echo.getHeaders().get("Accept")).contains("application/json");
        // TODO body
    }
}
