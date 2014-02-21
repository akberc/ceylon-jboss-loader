package com.dgwave.lahore.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.redhat.ceylon.compiler.java.metadata.Ceylon;

/**
 * Lahore Module Description Annotation.
 * @author Akber Choudhry
 */
@Ceylon(major = 6)
@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.TYPE })
public @interface Description$annotation$ {
  String description();
  
  String locale();
}
