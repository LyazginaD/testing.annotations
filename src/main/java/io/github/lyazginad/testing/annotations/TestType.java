package io.github.lyazginad.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the type of testing for a test method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestType {
    Type value();

    enum Type {
        FUNCTIONAL, PERFORMANCE, SECURITY, USABILITY, COMPATIBILITY
    }
}