package com.simplerasp.utils;

public interface Logger {
    public void trace(Object... arg);
    public void warn(Object... arg);
    public void info(Object... arg);
    public void error(Object... arg);
}