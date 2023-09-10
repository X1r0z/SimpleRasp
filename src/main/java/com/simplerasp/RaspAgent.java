package com.simplerasp;
import com.simplerasp.annotations.RaspHandler;
import org.reflections.Reflections;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

public class RaspAgent {
    public static void premain(String args, Instrumentation inst) throws Exception {
        System.out.println("rasp premain");

        String jarPath = RaspAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        inst.appendToBootstrapClassLoaderSearch(new JarFile(jarPath));
//        inst.appendToSystemClassLoaderSearch(new JarFile(jarPath));

        Reflections ref = new Reflections("com.simplerasp");
        Set<Class<?>> handlerClasses = ref.getTypesAnnotatedWith(RaspHandler.class);
        for (Class<?> handlerClass : handlerClasses) {
            RaspHandler handlerAnnotation = handlerClass.getAnnotation(RaspHandler.class);
            String className = handlerAnnotation.className();
            String methodName = handlerAnnotation.methodName();
            Class[] parameterTypes = handlerAnnotation.parameterTypes();
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);

            Map<String, String> handlerMap = new HashMap<>();
            handlerMap.put("className", handlerClass.getName());

            RaspTransformer transformer = new RaspTransformer(className, methodName, parameterTypes);
            transformer.setHandlerMap(handlerMap);

            inst.addTransformer(transformer, true);
            inst.retransformClasses(clazz);
        }
    }

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        System.out.println("agentmain");
    }

    public static void main(String[] args) throws Exception{

    }
}
