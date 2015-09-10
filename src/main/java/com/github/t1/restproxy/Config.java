package com.github.t1.restproxy;

import static lombok.AccessLevel.*;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.*;

import com.github.t1.rest.UriTemplate;

import lombok.*;
import lombok.experimental.Wither;

@Immutable
@Value
@Wither
@NoArgsConstructor(force = true, access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Config {
    public static Config builder(String name) {
        return new Config().withName(name);
    }

    @XmlAttribute
    String name;

    @XmlAttribute
    Boolean persistent;

    @XmlElement
    UriTemplate target;

    public boolean isPersistent() {
        return persistent == null || persistent == Boolean.TRUE;
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
