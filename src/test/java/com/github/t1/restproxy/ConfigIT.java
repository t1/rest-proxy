package com.github.t1.restproxy;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.nio.file.*;

import org.junit.*;

import com.github.t1.rest.*;
import com.github.t1.rest.UriTemplate.NonQuery;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class ConfigIT {
    private static final String PERSISTENT_CONFIG_JSON = "{" //
            + "\"name\":\"foo\"," //
            + "\"persistent\":true," //
            + "\"target\":\"http://example.com/fox\"" //
            + "}" //
            ;

    private static final Config CONFIG = Config //
            .builder("foo") //
            .withPersistent(false) //
            .withTarget(UriTemplate.fromString("http://example.com/fox")) //
            ;

    private static final Config PERSISTENT_CONFIG = CONFIG.withPersistent(true);

    private static final Path FOO_CONFIG_PATH = Paths.get("configs", "foo.json");

    @ClassRule
    public static final DropwizardClientRule service = new ProxyServiceRule();

    private final NonQuery configUri = UriTemplate.from(service.baseUri()).nonQuery().path("-configs");
    private final RestResource fooConfig = new RestResource(configUri.path("foo"));

    @After
    public void cleanup() {
        ProxyServiceRule.CONFIGS.remove(PERSISTENT_CONFIG.getName());
    }

    @Test
    public void shouldGetConfig() {
        ProxyServiceRule.CONFIGS.add(CONFIG);

        Config actualConfig = fooConfig.GET(Config.class);

        assertEquals(CONFIG, actualConfig);
    }

    @Test
    public void shouldPutConfig() {
        // TODO implement proxy.PUT(Config.class);
        ProxyServiceRule.CONFIG_RESOURCE.put(CONFIG.getName(), CONFIG);

        assertEquals(asList(CONFIG), ProxyServiceRule.CONFIGS.getConfigs());
    }

    @Test
    public void shouldPostConfig() {
        // TODO implement proxy.POST(Config.class);
        ProxyServiceRule.CONFIG_RESOURCE.post(CONFIG);

        assertEquals(asList(CONFIG), ProxyServiceRule.CONFIGS.getConfigs());
    }

    @Test
    public void shouldPersistPostedConfig() {
        // the file will be removed in #cleanup()
        // TODO use http POST when available
        ProxyServiceRule.CONFIG_RESOURCE.post(PERSISTENT_CONFIG);

        assertThat(FOO_CONFIG_PATH).hasContent(PERSISTENT_CONFIG_JSON);
    }

    @Test
    public void sholdLoadConfig() throws Exception {
        // the file will be removed in #cleanup()
        Files.write(FOO_CONFIG_PATH, PERSISTENT_CONFIG_JSON.getBytes());

        ProxyServiceRule.CONFIGS.load();

        assertEquals(PERSISTENT_CONFIG, fooConfig.GET(Config.class));
    }
}
