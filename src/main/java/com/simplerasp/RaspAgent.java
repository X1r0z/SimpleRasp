package com.simplerasp;

import com.simplerasp.annotations.RaspAfter;
import com.simplerasp.annotations.RaspBefore;
import com.simplerasp.annotations.RaspHandler;
import com.simplerasp.transformers.AfterTransformer;
import com.simplerasp.transformers.BeforeTransformer;
import org.reflections.Reflections;
import org.reflections.scanners.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.jar.JarFile;

public class RaspAgent {
    public static void premain(String args, Instrumentation inst) throws Exception {
        System.out.println("premain");

        // 解决双亲委派问题, 使得 Hook 使用 BootstrapClassLoader 加载的类时, 能够正常加载到我们自定义的 handler
        String jarPath = RaspAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        inst.appendToBootstrapClassLoaderSearch(new JarFile(jarPath));
//        inst.appendToSystemClassLoaderSearch(new JarFile(jarPath));

        // 扫描注解获取所有 handler class 以及 method
        Reflections ref = new Reflections("com.simplerasp", new TypeAnnotationsScanner(), new MethodAnnotationsScanner());
        Set<Class<?>> handlerClasses = ref.getTypesAnnotatedWith(RaspHandler.class);
        Set<Method> beforeMethods = ref.getMethodsAnnotatedWith(RaspBefore.class);
        Set<Method> afterMethods = ref.getMethodsAnnotatedWith(RaspAfter.class);

        // 遍历使用了 @RaspHandler 注解的类
        for (Class<?> handlerClass : handlerClasses) {
            RaspHandler handlerAnnotation = handlerClass.getAnnotation(RaspHandler.class);
            Method beforeMethod = null;
            Method afterMethod = null;

            // 寻找使用了 @RaspBefore 注解的方法
            for (Method m : beforeMethods) {
                if (m.getDeclaringClass() == handlerClass) {
                    beforeMethod = m;
                }
            }
            // 寻找使用了 @RaspAfter 注解的方法
            for (Method m : afterMethods) {
                if (m.getDeclaringClass() == handlerClass) {
                    afterMethod = m;
                }
            }

            // 获取 @RaspHandler 注解的信息
            String className = handlerAnnotation.className();
            String methodName = handlerAnnotation.methodName();
            Class[] parameterTypes = handlerAnnotation.parameterTypes();
            boolean isConstructor = handlerAnnotation.isConstructor();

            Class clazz;
            try {
                // 在 transform 前先加载一遍, 防止 Javaassist 获取不到类
                clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                // 当前类不存在, 跳过本次循环
                continue;
            }

            // 添加 transformer 并 retransform

            if (beforeMethod != null) {
                BeforeTransformer transformer = new BeforeTransformer(
                        className,
                        methodName,
                        parameterTypes,
                        isConstructor,
                        beforeMethod
                );
                inst.addTransformer(transformer, true);
                inst.retransformClasses(clazz);
            }

            if (afterMethod != null) {
                AfterTransformer transformer = new AfterTransformer(
                        className,
                        methodName,
                        parameterTypes,
                        isConstructor,
                        afterMethod
                );
                inst.addTransformer(transformer, true);
                inst.retransformClasses(clazz);
            }
        }
    }

    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("agentmain");
    }

    public static void main(String[] args) {
        System.out.println("main");
    }
}
