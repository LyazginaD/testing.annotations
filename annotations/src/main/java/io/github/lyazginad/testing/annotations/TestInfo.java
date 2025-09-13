package io.github.lyazginad.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Additional test metadata annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface TestInfo {
    String author() default "";
    String version() default "1.0"; // значение по умолчанию уже "1.0"
    String description() default "";
}