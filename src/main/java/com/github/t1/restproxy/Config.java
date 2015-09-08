package com.github.t1.restproxy;

import static javax.xml.bind.annotation.XmlAccessType.*;
import static lombok.AccessLevel.*;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.t1.rest.UriTemplate;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(force = true, access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@XmlRootElement
@XmlAccessorType(FIELD)
public class Config {
    public static class ConfigBuilder {
        public ConfigBuilder target(UriTemplate target) {
            return this.target(target.toString());
        }

        public ConfigBuilder target(String target) {
            this.target = target;
            return this;
        }
    }

    public static ConfigBuilder builder(String name) {
        return new ConfigBuilder().name(name);
    }

    @XmlAttribute
    String name;
    String target;

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

    @JsonIgnore
    public void setTargetUriTemplate(UriTemplate template) {
        this.target = template.toString();
    }

    public UriTemplate getTargetUriTemplate() {
        return UriTemplate.fromString(target);
    }
}
