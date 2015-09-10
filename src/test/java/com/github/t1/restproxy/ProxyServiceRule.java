package com.github.t1.restproxy;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.filter.LoggingFilter;

import io.dropwizard.testing.junit.DropwizardClientRule;
import lombok.extern.java.Log;

@Log
class ProxyServiceRule extends DropwizardClientRule {
    public static final ConfigResource CONFIG_RESOURCE = new ConfigResource();
    public static final Configs CONFIGS = new Configs();

    ProxyServiceRule() {
        super( //
                new LoggingFilter(log, true), //
                CONFIG_RESOURCE, //
                new ProxyResource(), //
                new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(CONFIGS).to(Configs.class);
                    }
                });
    }

    @Override
    protected void before() throws Throwable {
        cleanup();
        super.before();
    }

    @Override
    protected void after() {
        super.after();
        cleanup();
    }

    private void cleanup() {
        for (Config config : CONFIGS.getConfigs())
            log.warning("remove unexpected test config: " + config.getName());
        CONFIGS.clear();
    }
}
