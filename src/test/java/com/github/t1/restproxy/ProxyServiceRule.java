package com.github.t1.restproxy;

import java.io.IOException;
import java.nio.file.*;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.filter.LoggingFilter;

import io.dropwizard.testing.junit.DropwizardClientRule;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
class ProxyServiceRule extends DropwizardClientRule {
    public static final ConfigResource CONFIG_RESOURCE = new ConfigResource();
    public static final Path CONFIGS_PATH = Paths.get("target", "configs");
    public static final Configs CONFIGS;

    static {
        fileCleanup();
        CONFIGS = new Configs(CONFIGS_PATH);
    }

    @SneakyThrows(IOException.class)
    private static void fileCleanup() {
        if (Files.exists(CONFIGS_PATH))
            for (Path file : Files.newDirectoryStream(CONFIGS_PATH, "*.json")) {
                log.warning("remove unexpected test config: " + file);
                Files.delete(file);
            }
    }

    ProxyServiceRule() {
        super( //
                new LoggingFilter(log, true), //
                CONFIG_RESOURCE, //
                new ProxyResource(), //
                new EchoResource(), //
                new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(CONFIGS).to(Configs.class);
                        bind(new Contexts()).to(Contexts.class);
                    }
                });
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
