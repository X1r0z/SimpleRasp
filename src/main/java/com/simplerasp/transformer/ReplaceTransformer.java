package com.simplerasp.transformer;

import javassist.*;

import java.lang.reflect.Field;

public class ReplaceTransformer extends BaseTransformer {
    private Field replaceField;

    public ReplaceTransformer(String className,
                              String methodName,
                              Class[] parameterTypes,
                              boolean isConstructor) {
        super(className, methodName, parameterTypes, isConstructor);
    }

    public void setReplaceField(Field replaceField) {
        this.replaceField = replaceField;
    }

    @Override
    protected void raspTransform(CtClass ctClass, CtBehavior ctBehavior) {
        try {
            // 替换整个方法体
            ctBehavior.setBody((String) this.replaceField.get(null));
        } catch (CannotCompileException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
