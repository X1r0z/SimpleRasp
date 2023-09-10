package com.simplerasp;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;

public class RaspTransformer implements ClassFileTransformer {
    private String className;
    private String methodName;
    private Class[] parameterTypes;
    private Map<String, String> handlerMap;
    public RaspTransformer(String className, String methodName, Class[] parameterTypes) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }
    public void setHandlerMap(Map<String, String> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (this.className.replace(".", "/").equals(className)) {
            try {
                System.out.println("found target class");
                ClassPool pool = ClassPool.getDefault();
                CtClass ctClass = pool.get(this.className);

                CtClass[] ctParameterTypes = new CtClass[this.parameterTypes.length];
                for (int i = 0; i < this.parameterTypes.length; i ++) {
                    ctParameterTypes[i] = pool.get(this.parameterTypes[i].getName());
                }

                CtMethod ctMethod = ctClass.getDeclaredMethod(this.methodName, ctParameterTypes);
                if (Modifier.isStatic(ctMethod.getModifiers())) {
                    ctMethod.insertBefore("$args = " + this.handlerMap.get("className") + ".before(null, $args);");
                    ctMethod.insertAfter("$_ = " + this.handlerMap.get("className") + ".after(null, $_);");
                } else {
                    ctMethod.insertBefore("$args = " + this.handlerMap.get("className") + ".before($0, $args);");
                    ctMethod.insertAfter("$_ = " + this.handlerMap.get("className") + ".after($0, $_);");
                }

                ctClass.detach();
                return ctClass.toBytecode();
            } catch (Exception e) {
                e.printStackTrace();
                return classfileBuffer;
            }
        } else {
            return classfileBuffer;
        }
    }
}
