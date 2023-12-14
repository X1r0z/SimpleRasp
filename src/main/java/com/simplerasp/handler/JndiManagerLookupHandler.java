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
