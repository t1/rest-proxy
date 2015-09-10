@XmlJavaTypeAdapter(PathJaxbAdapter.class)
package com.github.t1.restproxy;

import java.nio.file.*;

import javax.xml.bind.annotation.adapters.*;

class PathJaxbAdapter extends XmlAdapter<String, Path> {
    @Override
    public Path unmarshal(String value) {
        return Paths.get(value);
    }

    @Override
    public String marshal(Path value) {
        return value.toString();
    }
}
