package com.simplerasp.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RaspHandler {
    String className();
    String methodName();
    Class[] parameterTypes();
}
