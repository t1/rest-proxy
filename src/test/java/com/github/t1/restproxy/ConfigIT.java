package com.github.t1.restproxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.*;

import com.github.t1.rest.RestResource;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class ConfigIT {
    private static final Config CONFIG = Config.builder("foo").target("http://example.com").build();

    @ClassRule
    public static final DropwizardClientRule service = new ProxyServiceRule();

    private final RestResource proxy = new RestResource(service.baseUri() + "/-configs/foo");

    @Before
    @After
    public void cleanup() {
        ProxyServiceRule.CONFIGS.getConfigs().clear();
    }

    @Test
    public void shouldGetConfig() {
        ProxyServiceRule.CONFIGS.add(CONFIG);

        Config actualConfig = proxy.GET(Config.class);

        assertEquals(CONFIG, actualConfig);
    }

    @Test
    @Ignore
    public void shouldPostConfig() {
        // TODO implement proxy.POST(Config.class);

        assertThat(ProxyServiceRule.CONFIGS.getConfigs()).contains(CONFIG);
    }

    @Test
    @Ignore
    public void shouldPutConfig() {
        proxy.PUT(Config.class); // TODO implement rest-client PUT

        assertThat(ProxyServiceRule.CONFIGS.getConfigs()).contains(CONFIG);
    }
}
