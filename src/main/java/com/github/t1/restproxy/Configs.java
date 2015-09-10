package com.github.t1.restproxy;

import static com.github.t1.rest.fallback.JsonMessageBodyReader.*;
import static java.util.Collections.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import javax.inject.Singleton;

import lombok.SneakyThrows;

@Singleton
public class Configs {
    private final Path store;
    private final List<Config> configs = new ArrayList<>();

    public Configs() {
        this(Paths.get("configs"));
    }

    @SneakyThrows(IOException.class)
    public Configs(Path store) {
        this.store = store;
        if (Files.exists(store))
            load();
        else
            Files.createDirectories(store);
    }

    // VisibleForTesting
    @SneakyThrows(IOException.class)
    public void load() {
        for (Path file : Files.newDirectoryStream(store, "*.json"))
            configs.add(MAPPER.readValue(file.toFile(), Config.class));
    }

    public Optional<Config> get(String name) {
        return get(Paths.get(name));
    }

    public Optional<Config> get(java.nio.file.Path path) {
        for (Config config : configs)
            if (config.getName().equals(path.toString()))
                return Optional.of(config);
        if (path.getNameCount() <= 1)
            return Optional.empty();
        Path parentPath = path.subpath(0, path.getNameCount() - 1);
        return get(parentPath);
    }

    public List<Config> getConfigs() {
        return unmodifiableList(configs);
    }

    public Configs add(Config config) {
        if (config.isPersistent())
            save(config);
        configs.add(config);
        return this;
    }

    public void remove(String path) {
        Iterator<Config> iter = configs.iterator();
        while (iter.hasNext()) {
            Config config = iter.next();
            if (config.getName().equals(path)) {
                if (config.isPersistent())
                    deleteStore(config);
                iter.remove();
            }
        }
    }

    public void clear() {
        while (!configs.isEmpty())
            deleteStore(configs.remove(0));
    }

    @SneakyThrows(IOException.class)
    private void deleteStore(Config config) {
        Files.deleteIfExists(fileName(config));
    }

    @SneakyThrows(IOException.class)
    private void save(Config config) {
        MAPPER.writeValue(Files.newBufferedWriter(fileName(config)), config);
    }

    private Path fileName(Config config) {
        return store.resolve(config.getName() + ".json");
    }
}
