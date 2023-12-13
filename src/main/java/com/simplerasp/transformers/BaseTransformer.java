package com.simplerasp.transformers;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public abstract class BaseTransformer implements ClassFileTransformer {
    protected String className;
    protected String methodName;
    protected Class[] parameterTypes;
    protected boolean isConstructor;

    public BaseTransformer(String className,
                           String methodName,
                           Class[] parameterTypes,
                           boolean isConstructor) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.isConstructor = isConstructor;
    }

    @Override
    public byte[] transform(ClassLoader loader, String _className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        // 匹配 className
        if (this.className.replace(".", "/").equals(_className)) {
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
                CtBehavior ctBehavior;
                if (this.isConstructor) {
                   ctBehavior = ctClass.getDeclaredConstructor(ctParameterTypes);
                } else {
                    ctBehavior = ctClass.getDeclaredMethod(this.methodName, ctParameterTypes);
                }

                // 修改字节码, 插入 handler
                this.raspTransform(ctClass, ctBehavior);
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

    protected abstract void raspTransform(CtClass ctClass, CtBehavior ctBehavior);
}
