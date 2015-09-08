package com.github.t1.restproxy;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class TestTools {
    public static Logger logger(String name) {
        return (Logger) LoggerFactory.getLogger(name);
    }
}
