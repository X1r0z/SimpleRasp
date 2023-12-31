package com.simplerasp.handler;

import com.simplerasp.annotation.RaspAfter;
import com.simplerasp.annotation.RaspBefore;
import com.simplerasp.annotation.RaspHandler;
import com.simplerasp.exception.RaspException;
import sun.misc.IOUtils;

@RaspHandler(
        className = "java.lang.Runtime",
        methodName = "exec",
        parameterTypes = {String.class}
)
public class RuntimeExecHandler {
    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        String cmd = (String) params[0];
        System.out.println("Try to exec: " + cmd);
        if (cmd.contains("Calculator")) {
            throw new RaspException("Reject malicious command execution attempt");
        }
        return params;
    }

    @RaspAfter
    public static Object handleAfter(Object obj, Object result) throws Exception {
        Process p = (Process) result;
        String output = new String(IOUtils.readAllBytes(p.getInputStream()));
        if (output.contains("uid=")) {
            throw new RaspException("Reject malicious command execution output");
        }
        return result;
    }
}
