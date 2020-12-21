package edu.yu.cs.intro.orderManagement;

import java.lang.reflect.Method;

public class TestHelper {
    public TestHelper() {

    }

    public void setCharDifLimit(int limit) {
        // idfk what this does
    }

    public void runMethod(Class c, Assignment8Tests test, String name) throws Throwable {
        Method method = c.getDeclaredMethod(name, new Class[0]);
        method.invoke(test, new Object[0]);
    }

    public Throwable getExceptionOutput(Throwable t) {
        return t;
    }
}