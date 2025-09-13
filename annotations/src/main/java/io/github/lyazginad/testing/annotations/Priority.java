package io.github.lyazginad.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the execution priority of a test case
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Priority {
    Level value();

    enum Level {
        P0, P1, P2, P3, P4
    }
}