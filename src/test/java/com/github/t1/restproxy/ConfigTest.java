package com.github.t1.restproxy;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.*;

import java.io.*;

import javax.xml.bind.JAXB;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigTest {
    private static final String JSON = "{\"name\":\"foo\",\"target\":\"http://example.com\"}";
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" //
            + "<config name=\"foo\">\n" //
            + "    <target>http://example.com</target>\n" //
            + "</config>\n";
    private static final Config CONFIG = Config.builder("foo").target("http://example.com").build();

    private String xml(Object object) {
        StringWriter writer = new StringWriter();
        JAXB.marshal(object, writer);
        return writer.toString();
    }

    private ObjectMapper jsonMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Test
    public void shouldMarshalAsXml() {
        String xml = xml(CONFIG);

        assertEquals(XML, xml);
    }

    @Test
    public void shouldMarshalAsJson() throws Exception {
        String json = jsonMapper().writeValueAsString(CONFIG);

        assertEquals(JSON, json);
    }

    @Test
    public void shouldUnmarshalFromXml() {
        Config config = JAXB.unmarshal(new StringReader(XML), Config.class);

        assertEquals(CONFIG, config);
    }

    @Test
    public void shouldUnmarshalFromJson() throws Exception {
        Config config = jsonMapper().readValue(JSON, Config.class);

        assertEquals(CONFIG, config);
    }

    @Test
    public void shouldFailToResolveEmptyString() {
        shouldFailToResolve("");
    }

    @Test
    public void shouldFailToResolveNonMatchingString() {
        shouldFailToResolve("baz");
    }

    @Test
    public void shouldFailToResolvePartiallyMatchingString() {
        shouldFailToResolve("bar");
    }

    private void shouldFailToResolve(String requestPath) {
        Config config = Config.builder("foo/bar").target("target").build();

        assertThat(catchThrowable(() -> {
            config.resolve(requestPath);
        })) //
                .isInstanceOf(AssertionError.class) //
                .hasMessageContaining("expected requested path '" + requestPath + "' to start with 'foo/bar'");
    }

    @Test
    public void shouldResolveFullyMatchingString() {
        Config config = Config.builder("foo/bar").target("target").build();

        String resolved = config.resolve("foo/bar");

        assertEquals("", resolved);
    }

    @Test
    public void shouldFailToResolveFullyMatchingStringWithTrailingNonSlashCharacter() {
        Config config = Config.builder("foo/bar").target("target").build();

        assertThat(catchThrowable(() -> {
            config.resolve("foo/barx");
        })) //
                .isInstanceOf(AssertionError.class) //
                .hasMessageContaining("expected requested path 'x' to continue with a slash '/' or nothing");
    }

    @Test
    public void shouldResolveMoreThanMatchingString() {
        Config config = Config.builder("foo/bar").target("target").build();

        String resolved = config.resolve("foo/bar/baz");

        assertEquals("baz", resolved);
    }

    @Test
    public void shouldResolveMuchMoreThanMatchingString() {
        Config config = Config.builder("foo/bar").target("target").build();

        String resolved = config.resolve("foo/bar/baz/bog");

        assertEquals("baz/bog", resolved);
    }
}
