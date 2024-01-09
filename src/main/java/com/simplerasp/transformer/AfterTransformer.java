package com.simplerasp.transformer;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;

import java.lang.reflect.Method;

public class AfterTransformer extends BaseTransformer {
    private Method afterMethod;
    private static final String AFTER_BODY = "$_ = %s.%s(%s,$_);";

    public AfterTransformer(String className,
                            String methodName,
                            Class[] parameterTypes) {
        super(className, methodName, parameterTypes);
    }

    public void setAfterMethod(Method afterMethod) {
        this.afterMethod = afterMethod;
    }

    @Override
    public void raspTransform(CtClass ctClass, CtBehavior ctBehavior) {
        try {
            // 在方法结尾加入 handler 逻辑, 用于处理返回的结果
            ctBehavior.insertAfter(String.format(
                    AFTER_BODY,
                    this.afterMethod.getDeclaringClass().getName(),
                    this.afterMethod.getName(),
                    Modifier.isStatic(ctBehavior.getModifiers()) ? "null" : "$0"
            ));
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
