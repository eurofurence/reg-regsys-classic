package org.eurofurence.regsys.repositories.lowlevel;

import java.io.IOException;

@FunctionalInterface
public interface IOExceptionThrowingResponseConsumer<T> {
    void accept(T t, int s, String loc) throws IOException;
}
