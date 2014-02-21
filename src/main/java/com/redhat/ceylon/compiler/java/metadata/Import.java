package com.redhat.ceylon.compiler.java.metadata;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation on Ceylon modules that denotes the dependencies of that module.
 * Copied from Ceylon compiler module (https://github.com/ceylon/ceylon-compiler)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Import {
    public boolean export() default false;
    public boolean optional() default false;
    public String name() default "";
    public String version() default "";
}
