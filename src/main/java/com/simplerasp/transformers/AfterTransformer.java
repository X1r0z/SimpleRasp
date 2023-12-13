package com.simplerasp.transformers;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;

import java.lang.reflect.Method;

public class AfterTransformer extends BaseTransformer {
    private Method hookMethod;
    private static final String AFTER_BODY = "$_ = %s.%s(%s,$_);";

    public AfterTransformer(String className, String methodName,
                            Class[] parameterTypes,
                            boolean isConstructor,
                            Method hookMethod) {
        super(className, methodName, parameterTypes, isConstructor);
        this.hookMethod = hookMethod;
    }

    @Override
    public void raspTransform(CtClass ctClass, CtBehavior ctBehavior) {
        try {
            ctBehavior.insertAfter(String.format(AFTER_BODY,
                    this.hookMethod.getDeclaringClass().getName(),
                    this.hookMethod.getName(),
                    Modifier.isStatic(ctBehavior.getModifiers()) ? "null" : "$0"
            ));
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
