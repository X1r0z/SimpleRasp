package com.simplerasp.transformers;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;

import java.lang.reflect.Method;

public class BeforeTransformer extends BaseTransformer {
    private Method hookMethod;
    private static final String BEFORE_BODY = "$args = %s.%s(%s,$args);";

    public BeforeTransformer(String className,
                             String methodName,
                             Class[] parameterTypes,
                             boolean isConstructor,
                             Method hookMethod) {
        super(className, methodName, parameterTypes, isConstructor);
        this.hookMethod = hookMethod;
    }

    @Override
    public void raspTransform(CtClass ctClass, CtBehavior ctBehavior) {
        try {
            ctBehavior.insertBefore(String.format(BEFORE_BODY,
                    this.hookMethod.getDeclaringClass().getName(),
                    this.hookMethod.getName(),
                    Modifier.isStatic(ctBehavior.getModifiers()) ? "null" : "$0"
            ));
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
