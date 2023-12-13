package com.simplerasp.handlers;

import com.simplerasp.annotations.RaspBefore;
import com.simplerasp.annotations.RaspHandler;
import com.simplerasp.exceptions.RaspException;

@RaspHandler(className = "java.lang.ProcessBuilder", isConstructor = true, parameterTypes = {String[].class})
public class ProcessBuilderHandler {

    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        String cmd = String.join(" ", (String[])params[0]);
        System.out.println("try to exec: " + cmd);
        if (cmd.contains("Calculator")) {
            throw new RaspException("Reject malicious command execution attempt");
        }
        return params;
    }
}
