# SimpleRasp

Simple Java Rasp

基于 Java Instrumentation + Javaassist

## 使用

`@RaspHandler` 标记一个 Rasp Handler 处理类, 其参数用于定位被 Hook 的方法或构造函数

- className: 类名
- methodName: 方法名 (为 `<init>` 时表示构造函数)
- parameterTypes: 方法/构造函数的参数类型
- isNative: 是否为 native 方法 (默认为 false)

`@RaspBefore` 标记一个对目标方法的参数进行拦截的方法, 对应 Javaassist 中的 insertBefore

方法签名: `public static Object[] handleBefore(Object obj, Object[] params)`

- obj: 当目标方法为非静态方法时, 该值为目标对象本身 (this), 为静态方法时该值为 null
- params: 目标方法的参数列表
- 返回值: 默认为 params, 用于替代目标方法原来的参数

`@RaspAfter` 标记一个对目标方法的返回值进行拦截的方法, 对应 Javaassist 中的 insertAfter

方法签名: `public static Object handleAfter(Object obj, Object result)`

- obj: 同上
- result: 目标方法 return 的值, 如果方法返回类型为 void, 则该值为 null
- 返回值: 默认为 result, 用于替代目标方法原来的返回值

`@RaspReplace` 标记一个对目标方法的方法体进行替换的字段, 对应 Javaassist 中的 setBody

字段签名: `public static String body`

如果字段内存在多行语句, 需要在开头和末尾加上大括号

## Demo

拦截 Runtime.exec 方法

```java
package com.simplerasp.handler;

import com.simplerasp.annotation.RaspAfter;
import com.simplerasp.annotation.RaspBefore;
import com.simplerasp.annotation.RaspHandler;
import com.simplerasp.exception.RaspException;
import sun.misc.IOUtils;

@RaspHandler(
        className = "java.lang.Runtime",
        methodName = "exec",
        parameterTypes = {String.class}
)
public class RuntimeExecHandler {
    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        String cmd = (String) params[0];
        System.out.println("Try to exec: " + cmd);
        if (cmd.contains("Calculator")) {
            throw new RaspException("Reject malicious command execution attempt");
        }
        return params;
    }

    @RaspAfter
    public static Object handleAfter(Object obj, Object result) throws Exception {
        Process p = (Process) result;
        String output = new String(IOUtils.readAllBytes(p.getInputStream()));
        if (output.contains("uid=")) {
            throw new RaspException("Reject malicious command execution output");
        }
        return result;
    }
}
```

拦截 ProcessBuilder 构造函数

```java
package com.simplerasp.handler;

import com.simplerasp.annotation.RaspBefore;
import com.simplerasp.annotation.RaspHandler;
import com.simplerasp.exception.RaspException;

@RaspHandler(
        className = "java.lang.ProcessBuilder",
        methodName = "<init>",
        parameterTypes = {String[].class}
)
public class ProcessBuilderHandler {
    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        String cmd = String.join(" ", (String[])params[0]);
        System.out.println("Try to exec: " + cmd);
        if (cmd.contains("Calculator")) {
            throw new RaspException("Reject malicious command execution attempt");
        }
        return params;
    }
}
```

拦截 Log4j2 JNDI 注入

```java
package com.simplerasp.handler;

import com.simplerasp.annotation.RaspBefore;
import com.simplerasp.annotation.RaspHandler;
import com.simplerasp.exception.RaspException;

@RaspHandler(
        className = "org.apache.logging.log4j.core.net.JndiManager",
        methodName = "lookup",
        parameterTypes = {String.class}
)
public class JndiManagerLookupHandler {
    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        String name = (String) params[0];
        String[] blacklist = new String[]{"ldap", "jndi"};
        for (String s : blacklist) {
            if (name.toLowerCase().contains(s)) {
                throw new RaspException("Reject malicious jndi lookup attempt");
            }
        }
        return params;
    }
}
```

拦截 UNIXProcess.forkAndExec 方法

当然 native 方法也支持使用 `@RaspBefore` 和 `@RaspAfter` 注解进行拦截

```java
package com.simplerasp.handler;

import com.simplerasp.annotation.RaspHandler;
import com.simplerasp.annotation.RaspReplace;

@RaspHandler(
        className = "java.lang.UNIXProcess",
        methodName = "forkAndExec",
        parameterTypes = {
                int.class,
                byte[].class,
                byte[].class,
                byte[].class,
                int.class,
                byte[].class,
                int.class,
                byte[].class,
                int[].class,
                boolean.class
        },
        isNative = true
)
public class UNIXProcessHandler {
    @RaspReplace
    public static String body = "{ throw new Exception(\"Reject malicious command execution attempt\"); }";
}
```

拦截 ObjectInputStream.resolveClass 方法

```java
package com.simplerasp.handler;

import com.simplerasp.annotation.RaspBefore;
import com.simplerasp.annotation.RaspHandler;
import com.simplerasp.exception.RaspException;

import java.io.ObjectStreamClass;

@RaspHandler(
        className = "java.io.ObjectInputStream",
        methodName = "resolveClass",
        parameterTypes = {ObjectStreamClass.class}
)
public class ObjectInputStreamHandler {
    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        ObjectStreamClass osc = (ObjectStreamClass) params[0];
        if (osc.getName().equals("org.apache.commons.collections.functors.InvokerTransformer")) {
            throw new RaspException("Reject malicious deserialization attempt");
        }
        return params;
    }
}
```