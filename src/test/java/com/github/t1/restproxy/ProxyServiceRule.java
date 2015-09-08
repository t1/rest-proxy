package com.github.t1.restproxy;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.filter.LoggingFilter;

import io.dropwizard.testing.junit.DropwizardClientRule;
import lombok.extern.java.Log;

@Log
class ProxyServiceRule extends DropwizardClientRule {
    public static final Configs CONFIGS = new Configs();

    ProxyServiceRule() {
        super( //
                new LoggingFilter(log, true), //
                new ConfigResource(), //
                new ProxyResource(), //
                new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(CONFIGS).to(Configs.class);
                    }
                });
    }
}
