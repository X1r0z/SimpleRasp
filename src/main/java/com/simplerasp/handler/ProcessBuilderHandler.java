package com.simplerasp.handler;

import com.simplerasp.annotation.RaspBefore;
import com.simplerasp.annotation.RaspHandler;
import com.simplerasp.exception.RaspException;

@RaspHandler(
        className = "java.lang.ProcessBuilder",
        isConstructor = true,
        parameterTypes = {String[].class}
)
public class ProcessBuilderHandler {
    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        String cmd = String.join(" ", (String[])params[0]);
        System.out.println("Try to exec: " + cmd);
        if (cmd.contains("Calculator")) {
            throw new RaspException("Reject malicious command execution attempt");
        }
        return params;
    }
}
