package com.simplerasp.transformer;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
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
            System.out.println("Found target class: " + this.className);
            try {
                ClassPool pool = ClassPool.getDefault();
                CtClass ctClass = pool.get(this.className);

                // 将 parameterTypes 类型转换为 CtClass
                CtClass[] ctParameterTypes = new CtClass[this.parameterTypes.length];
                for (int i = 0; i < this.parameterTypes.length; i ++) {
                    ctParameterTypes[i] = pool.get(this.parameterTypes[i].getName());
                }

                // 使用 Javaassist 定位目标方法/构造函数
                if (this.isConstructor) {
                    CtBehavior ctBehavior = ctClass.getDeclaredConstructor(ctParameterTypes);
                    this.raspTransform(ctClass, ctBehavior);
                } else {
                    CtBehavior ctBehavior = ctClass.getDeclaredMethod(this.methodName, ctParameterTypes);

                    // Hook native 方法
                    if (Modifier.isNative(ctBehavior.getModifiers())) {
                        CtMethod ctMethod = (CtMethod) ctBehavior;

                        // 使原有的 native 方法名加上 prefix
                        CtMethod renameCtNativeMethod = CtNewMethod.copy(ctMethod,ctClass, null);
                        renameCtNativeMethod.setName("RASP_" + ctMethod.getName());
                        ctClass.removeMethod(ctMethod);
                        ctClass.addMethod(renameCtNativeMethod);

                        // 加入同名 hook 方法, 调用实际带 prefix 的 native 方法
                        CtMethod hookCtNativeMethod = CtNewMethod.copy(ctMethod,ctClass, null);
                        hookCtNativeMethod.setModifiers(ctMethod.getModifiers() &~ Modifier.NATIVE);
                        hookCtNativeMethod.setBody("{ return " + renameCtNativeMethod.getName() + "($$); }");
                        ctClass.addMethod(hookCtNativeMethod);

                        // transform 时传入同名的 hook 方法
                        this.raspTransform(ctClass, hookCtNativeMethod);
                    } else {
                        this.raspTransform(ctClass, ctBehavior);
                    }
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

    protected abstract void raspTransform(CtClass ctClass, CtBehavior ctBehavior);
}
