package org.example.healthcare.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogDoctor {
    String action();       // "GET_ALL", "GET_BY_ID", "GET_BY_SPECIALTY", "UPDATE", "DELETE"
    String cacheAction() default "NONE";  // "MISS", "EVICT", "NONE"
}