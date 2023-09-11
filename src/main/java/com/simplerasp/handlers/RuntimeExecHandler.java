package com.simplerasp.handlers;

import com.simplerasp.annotations.RaspAfter;
import com.simplerasp.annotations.RaspBefore;
import com.simplerasp.annotations.RaspHandler;
import com.simplerasp.exceptions.RaspException;
import sun.misc.IOUtils;

@RaspHandler(className = "java.lang.Runtime", methodName = "exec", parameterTypes = {String.class})
public class RuntimeExecHandler {
    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        System.out.println("before");

        String cmd = (String) params[0];
        System.out.println("try to exec: " + cmd);
        if (cmd.contains("Calculator")) {
            throw new RaspException("Reject malicious command execution attempts");
        }
        return params;
    }

    @RaspAfter
    public static Object handlerAfter(Object obj, Object result) throws Exception{
        System.out.println("after");

        Process p = (Process) result;
        String output = new String(IOUtils.readAllBytes(p.getInputStream()));
        if (output.contains("uid=")) {
            throw new RaspException("Reject malicious command execution output");
        }
        return result;
    }
}
