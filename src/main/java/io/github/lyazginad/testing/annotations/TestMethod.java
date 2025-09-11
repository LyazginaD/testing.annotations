package io.github.lyazginad.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the testing methodology for a test method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestMethod {
    Method value();

    enum Method {
        BLACK_BOX, WHITE_BOX, GRAY_BOX,
        POSITIVE, NEGATIVE, BOUNDARY_VALUE,
        EQUIVALENCE_PARTITIONING, STATE_TRANSITION
    }
}