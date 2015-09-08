package com.github.t1.restproxy;

import java.nio.file.*;
import java.util.*;

import javax.inject.Singleton;

import com.github.t1.restproxy.Config.ConfigBuilder;

import lombok.Data;

@Data
@Singleton
public class Configs {
    private List<Config> configs = new ArrayList<>();

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

    public void remove(String path) {
        Iterator<Config> iter = configs.iterator();
        while (iter.hasNext()) {
            Config config = iter.next();
            if (config.getName().equals(path))
                iter.remove();
        }
    }

    public Configs add(ConfigBuilder builder) {
        return add(builder.build());
    }

    public Configs add(Config config) {
        configs.add(config);
        return this;
    }
}
