package com.github.t1.restproxy;

import static lombok.AccessLevel.*;

import java.nio.file.*;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.t1.rest.UriTemplate;

import lombok.*;
import lombok.experimental.Wither;

// @Immutable
@Value
@Wither
@NoArgsConstructor(force = true, access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Config {
    public static Config named(String name) {
        return new Config().withName(name);
    }

    // @Immutable
    @Value
    @Wither
    @NoArgsConstructor(force = true, access = PRIVATE)
    @AllArgsConstructor(access = PRIVATE)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class RecorderConfig {
        public static RecorderConfig recorder() {
            return new RecorderConfig();
        }

        @XmlElement
        Path persistencePath;

        public RecorderConfig withPath(Path path) {
            return new RecorderConfig(path);
        }

        public RecorderConfig withPath(String first, String... more) {
            return this.withPath(Paths.get(first, more));
        }
    }

    @XmlAttribute
    String name;

    @XmlAttribute
    Boolean persistent;

    @XmlElement
    @org.codehaus.jackson.annotate.JsonIgnore
    UriTemplate target;

    @XmlElement
    RecorderConfig recorder;

    @JsonIgnore // use field
    @org.codehaus.jackson.annotate.JsonIgnore
    public boolean isPersistent() {
        return persistent == null || persistent == Boolean.TRUE;
    }

    @org.codehaus.jackson.annotate.JsonProperty("target")
    public String getCodehausTarget() {
        return target.toString();
    }

    public Config with(RecorderConfig recorder) {
        return withRecorder(recorder);
    }

    public String resolve(String path) {
        if (!path.startsWith(name))
            throw new AssertionError("expected requested path '" + path + "' to start with '" + name + "'");
        path = path.substring(name.length());
        if (path.isEmpty())
            return path;
        if (!path.startsWith("/"))
            throw new AssertionError("expected requested path '" + path + "' to continue with a slash '/' or nothing");
        return path.substring(1);
    }
}
