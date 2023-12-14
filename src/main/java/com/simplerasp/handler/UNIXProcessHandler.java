package com.simplerasp.handler;

import com.simplerasp.annotation.RaspHandler;
import com.simplerasp.annotation.RaspReplace;

@RaspHandler(
        className = "java.lang.UNIXProcess",
        methodName = "forkAndExec",
        parameterTypes = {
                int.class,
                byte[].class,
                byte[].class,
                byte[].class,
                int.class,
                byte[].class,
                int.class,
                byte[].class,
                int[].class,
                boolean.class
        },
        isNative = true
)
public class UNIXProcessHandler {
    @RaspReplace
    public static String body = "{ throw new Exception(\"Reject malicious command execution attempt\"); }";
}
