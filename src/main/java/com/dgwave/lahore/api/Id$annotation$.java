package com.dgwave.lahore.api;

import com.redhat.ceylon.compiler.java.metadata.Ceylon;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lahore Module Id Annotation.
 * @author Akber Choudhry
 */
@Ceylon(major = 6)
@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.TYPE })
public @interface Id$annotation$ {
  String id();
}
