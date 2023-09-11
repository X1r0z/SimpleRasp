# SimpleRasp

Simple Java Rasp

基于 Java Instrumentation + Javaassist

## 使用

`@RaspHandler` 标记一个用于处理的 Handler 类, 参数用于定位被 Hook 的方法或构造函数

- className: 类名
- methodName: 方法名 (可选)
- isConstructor: 是否为构造函数 (默认为 false)
- parameterTypes: 方法/构造函数的参数类型

`@RaspBefore` 标记一个在目标方法刚开始调用时进行处理的方法, 对应 Javaassist 中的 insertBefore

方法签名: `public static Object[] handleBefore(Object obj, Object[] params)`

- obj: 当目标方法为非静态方法时, 该值为目标对象本身 (this), 为静态方法时该值为 null
- params: 目标方法的参数列表

`@RaspAfter` 标记一个在目标方法 return 之前进行处理的方法, 对应 Javaassist 中的 insertAfter

方法签名: `public static Object handleAfter(Object obj, Object result)`

- obj: 同上
- result: 目标方法 return 的值, 如果方法返回类型为 void, 则该值为 null

## Demo

拦截 Runtime.exec 方法

```java
package com.simplerasp.handlers;

import com.simplerasp.annotations.RaspAfter;
import com.simplerasp.annotations.RaspBefore;
import com.simplerasp.annotations.RaspHandler;
import com.simplerasp.exceptions.RaspException;
import sun.misc.IOUtils;

@RaspHandler(className = "java.lang.Runtime", methodName = "exec", parameterTypes = {String.class})
public class RuntimeExecHandler {

    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        System.out.println("before");

        String cmd = (String) params[0];
        System.out.println("try to exec: " + cmd);
        if (cmd.contains("Calculator")) {
            throw new RaspException("Reject malicious command execution attempts");
        }
        return params;
    }

    @RaspAfter
    public static Object handleAfter(Object obj, Object result) throws Exception{
        System.out.println("after");

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
package com.simplerasp.handlers;

import com.simplerasp.annotations.RaspBefore;
import com.simplerasp.annotations.RaspHandler;
import com.simplerasp.exceptions.RaspException;

@RaspHandler(className = "java.lang.ProcessBuilder", isConstructor = true, parameterTypes = {String[].class})
public class ProcessBuilderHandler {

    @RaspBefore
    public static Object[] handleBefore(Object obj, Object[] params) {
        System.out.println("before");

        String cmd = String.join(" ", (String[])params[0]);
        System.out.println("try to exec: " + cmd);
        if (cmd.contains("Calculator")) {
            throw new RaspException("Reject malicious command execution attempts");
        }
        return params;
    }
}
```

## License

Modified some code from the following repos

[ronmamo/reflections](https://github.com/ronmamo/reflections)
