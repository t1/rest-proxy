package com.github.t1.restproxy;

import java.util.*;

import javax.inject.Singleton;

import com.github.t1.rest.*;

@Singleton
public class Contexts {
    private final Map<Config, RestContext> contexts = new HashMap<>();

    public RestContext get(Config config) {
        if (!contexts.containsKey(config))
            contexts.put(config, createContext(config));
        return contexts.get(config);
    }

    private RestContext createContext(Config config) {
        RestContext context = RestContext.REST;
        if (config.getRecorder() != null)
            context = new RestClientRecorder(context, config.getRecorder().getPersistencePath()).context();
        return context;
    }
}
