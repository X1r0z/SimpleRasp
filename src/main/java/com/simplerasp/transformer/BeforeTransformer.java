package com.simplerasp.transformer;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;

import java.lang.reflect.Method;

public class BeforeTransformer extends BaseTransformer {
    private Method beforeMethod;
    private static final String BEFORE_BODY = "$args = %s.%s(%s,$args);";

    public BeforeTransformer(String className,
                             String methodName,
                             Class[] parameterTypes,
                             boolean isConstructor) {
        super(className, methodName, parameterTypes, isConstructor);
    }

    public void setBeforeMethod(Method beforeMethod) {
        this.beforeMethod = beforeMethod;
    }

    @Override
    public void raspTransform(CtClass ctClass, CtBehavior ctBehavior) {
        try {
            // 在方法开头加入 handler 逻辑, 用于处理传入的参数
            ctBehavior.insertBefore(String.format(
                    BEFORE_BODY,
                    this.beforeMethod.getDeclaringClass().getName(),
                    this.beforeMethod.getName(),
                    Modifier.isStatic(ctBehavior.getModifiers()) ? "null" : "$0"
            ));
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
