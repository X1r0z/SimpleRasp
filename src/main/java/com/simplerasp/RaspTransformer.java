package com.simplerasp;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;

public class RaspTransformer implements ClassFileTransformer {
    private String className;
    private String methodName;
    private boolean isConstructor;
    private Class[] parameterTypes;
    private Map<String, String> handlerMap;
    private static final String beforeBody = "$args = %s.%s(%s,$args);";
    private static final String afterBody = "$_ = %s.%s(%s,$_);";


    public RaspTransformer(String className, String methodName, boolean isConstructor, Class[] parameterTypes) {
        this.className = className;
        this.methodName = methodName;
        this.isConstructor = isConstructor;
        this.parameterTypes = parameterTypes;
    }

    public void setHandlerMap(Map<String, String> handlerMap) {
        this.handlerMap = handlerMap;
    }
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        // 匹配 className
        if (this.className.replace(".", "/").equals(className)) {
            System.out.println("found target class: " + this.className);
            try {
                ClassPool pool = ClassPool.getDefault();
                CtClass ctClass = pool.get(this.className);

                // 将 parameterTypes 类型转换为 CtClass
                CtClass[] ctParameterTypes = new CtClass[this.parameterTypes.length];
                for (int i = 0; i < this.parameterTypes.length; i ++) {
                    ctParameterTypes[i] = pool.get(this.parameterTypes[i].getName());
                }

                // 使用 Javaassist 定位目标方法/构造函数
                CtBehavior ctBehavior = null;

                if (this.isConstructor) {
                   ctBehavior = ctClass.getDeclaredConstructor(ctParameterTypes);
                } else {
                    ctBehavior = ctClass.getDeclaredMethod(this.methodName, ctParameterTypes);
                }

                // 修改字节码, 插入 handler
                if (this.handlerMap.get("beforeName") != null) {
                    ctBehavior.insertBefore(String.format(beforeBody,
                            this.handlerMap.get("className"),
                            this.handlerMap.get("beforeName"),
                            Modifier.isStatic(ctBehavior.getModifiers()) ? "null" : "$0"
                    ));
                }

                if (this.handlerMap.get("afterName") != null) {
                    ctBehavior.insertAfter(String.format(afterBody,
                            this.handlerMap.get("className"),
                            this.handlerMap.get("afterName"),
                            Modifier.isStatic(ctBehavior.getModifiers()) ? "null" : "$0"
                    ));
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
