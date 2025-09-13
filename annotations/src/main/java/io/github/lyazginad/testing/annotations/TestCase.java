package io.github.lyazginad.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a test case with order and name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestCase {
    /** Execution order (lower numbers execute first) */
    int order() default Integer.MAX_VALUE;
    /** Human-readable test name */
    String name() default "";
    /** Test category */
    String category() default "general";
}