package io.github.lyazginad.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the testing level of a test method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestLevel {
    Level value();

    enum Level {
        UNIT, INTEGRATION, SYSTEM, ACCEPTANCE
    }
}