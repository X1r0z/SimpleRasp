package com.simplerasp.handler;

import com.simplerasp.annotation.RaspBefore;
import com.simplerasp.annotation.RaspHandler;
import com.simplerasp.exception.RaspException;

import java.io.ObjectStreamClass;

@RaspHandler(
        className = "java.io.ObjectInputStream",
        methodName = "resolveClass",
        parameterTypes = {ObjectStreamClass.class}
)
public class ObjectInputStreamHandler {
    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        ObjectStreamClass osc = (ObjectStreamClass) params[0];
        if (osc.getName().equals("org.apache.commons.collections.functors.InvokerTransformer")) {
            throw new RaspException("Reject malicious deserialization attempt");
        }
        return params;
    }
}