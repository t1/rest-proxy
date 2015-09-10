package com.github.t1.restproxy;

import static com.github.t1.restproxy.Config.RecorderConfig.*;
import static com.github.t1.restproxy.ProxyServiceRule.*;
import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.nio.file.*;

import org.junit.*;

import com.github.t1.rest.*;
import com.github.t1.rest.UriTemplate.NonQuery;

public class ConfigIT {
    private static final String PERSISTENT_CONFIG_JSON = "{" //
            + "\"name\":\"foo\"," //
            + "\"persistent\":true," //
            + "\"target\":\"http://example.com/fox\"," //
            + "\"recorder\":{\"path\":\"target/recordings\"}" //
            + "}" //
            ;

    private static final Config CONFIG = Config.named("foo") //
            .withPersistent(false) //
            .withTarget(UriTemplate.fromString("http://example.com/fox")) //
            .with(recorder().withPath("target", "recordings")) //
            ;

    private static final Config PERSISTENT_CONFIG = CONFIG.withPersistent(true);

    private static final Path FOO_CONFIG_PATH = CONFIGS_PATH.resolve("foo.json");

    @ClassRule
    public static final ProxyServiceRule service = new ProxyServiceRule();

    private final NonQuery configUri = UriTemplate.from(service.baseUri()).nonQuery().path("-configs");
    private final RestResource fooConfig = new RestResource(configUri.path("foo"));

    @After
    public void cleanup() {
        CONFIGS.remove(PERSISTENT_CONFIG.getName());
    }

    @Test
    public void shouldGetConfig() {
        CONFIGS.add(CONFIG);

        Config actualConfig = fooConfig.GET(Config.class);

        assertEquals(CONFIG, actualConfig);
    }

    @Test
    public void shouldPutConfig() {
        // TODO implement proxy.PUT(Config.class);
        CONFIG_RESOURCE.put(CONFIG.getName(), CONFIG);

        assertEquals(asList(CONFIG), CONFIGS.getConfigs());
    }

    @Test
    public void shouldPostConfig() {
        // TODO implement proxy.POST(Config.class);
        CONFIG_RESOURCE.post(CONFIG);

        assertEquals(asList(CONFIG), CONFIGS.getConfigs());
    }

    @Test
    public void shouldPersistPostedConfig() {
        // the file will be removed in #cleanup()
        // TODO use http POST when available
        CONFIG_RESOURCE.post(PERSISTENT_CONFIG);

        assertThat(FOO_CONFIG_PATH).hasContent(PERSISTENT_CONFIG_JSON);
    }

    @Test
    public void sholdLoadConfig() throws Exception {
        // the file will be removed in #cleanup()
        Files.write(FOO_CONFIG_PATH, PERSISTENT_CONFIG_JSON.getBytes());

        CONFIGS.load();

        assertEquals(PERSISTENT_CONFIG, fooConfig.GET(Config.class));
    }
}
