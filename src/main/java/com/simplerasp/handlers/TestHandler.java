package com.simplerasp.handlers;

import com.simplerasp.annotations.RaspAfter;
import com.simplerasp.annotations.RaspBefore;
import com.simplerasp.annotations.RaspHandler;

@RaspHandler(className = "java.lang.Runtime", methodName = "exec", parameterTypes = {String.class})
//@RaspHandler(className = "com.example.Demo", methodName = "main", parameterTypes = {String[].class})
//@RaspHandler(className = "com.example.springbootdemo.controller.IndexController", methodName = "index", parameterTypes = {})
public class TestHandler {
    @RaspBefore
    public static Object[] before(Object obj, Object[] params) {
        System.out.println("before");
        return params;
    }

    @RaspAfter
    public static Object after(Object obj, Object result) {
        System.out.println("after");
        return result;
    }
}
