package com.simplerasp;

import com.simplerasp.annotation.RaspAfter;
import com.simplerasp.annotation.RaspBefore;
import com.simplerasp.annotation.RaspHandler;
import com.simplerasp.annotation.RaspReplace;
import com.simplerasp.transformer.AfterTransformer;
import com.simplerasp.transformer.BeforeTransformer;
import com.simplerasp.transformer.ReplaceTransformer;
import org.reflections.Reflections;
import org.reflections.scanners.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
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
        Reflections ref = new Reflections(
                "com.simplerasp",
                new TypeAnnotationsScanner(),
                new MethodAnnotationsScanner(),
                new FieldAnnotationsScanner()
        );
        Set<Class<?>> handlerClasses = ref.getTypesAnnotatedWith(RaspHandler.class);
        Set<Method> beforeMethods = ref.getMethodsAnnotatedWith(RaspBefore.class);
        Set<Method> afterMethods = ref.getMethodsAnnotatedWith(RaspAfter.class);
        Set<Field> replaceFields = ref.getFieldsAnnotatedWith(RaspReplace.class);

        // 遍历使用了 @RaspHandler 注解的类
        for (Class<?> handlerClass : handlerClasses) {
            // 获取 @RaspHandler 注解的信息
            RaspHandler handlerAnnotation = handlerClass.getAnnotation(RaspHandler.class);
            String className = handlerAnnotation.className();
            String methodName = handlerAnnotation.methodName();
            Class[] parameterTypes = handlerAnnotation.parameterTypes();
            boolean isConstructor = handlerAnnotation.isConstructor();
            boolean isNative = handlerAnnotation.isNative();

            Method beforeMethod = null;
            Method afterMethod = null;
            Field replaceField = null;

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

            // 寻找使用了 @RaspReplace 注解的字段
            for (Field f : replaceFields) {
                if (f.getDeclaringClass() == handlerClass) {
                    replaceField = f;
                }
            }

            // 暂时先用 Class.forName 替代, 后续有时间再研究
//            try {
//                // 在 transform 前先加载一遍, 防止 Javaassist 获取不到类
//                // 注意使用 AppClassLoader 加载 class 后无法修改 native 方法
//                if(!isNative) Thread.currentThread().getContextClassLoader().loadClass(className);
//            } catch (ClassNotFoundException e) {
//                // 当前类不存在, 跳过本次循环
//                continue;
//            }

            // 添加 transformer 并 retransform

            if (beforeMethod != null) {
                BeforeTransformer transformer = new BeforeTransformer(
                        className,
                        methodName,
                        parameterTypes,
                        isConstructor
                );
                transformer.setBeforeMethod(beforeMethod);
                inst.addTransformer(transformer, true);
                if (isNative) inst.setNativeMethodPrefix(transformer, "RASP_");
                inst.retransformClasses(Class.forName(className));
            }

            if (afterMethod != null) {
                AfterTransformer transformer = new AfterTransformer(
                        className,
                        methodName,
                        parameterTypes,
                        isConstructor
                );
                transformer.setAfterMethod(afterMethod);
                inst.addTransformer(transformer, true);
                if (isNative) inst.setNativeMethodPrefix(transformer, "RASP_");
                inst.retransformClasses(Class.forName(className));
            }

            if (replaceField != null) {
                ReplaceTransformer transformer = new ReplaceTransformer(
                        className,
                        methodName,
                        parameterTypes,
                        isConstructor
                );
                transformer.setReplaceField(replaceField);
                inst.addTransformer(transformer, true);
                if (isNative) inst.setNativeMethodPrefix(transformer, "RASP_");
                inst.retransformClasses(Class.forName(className));
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
