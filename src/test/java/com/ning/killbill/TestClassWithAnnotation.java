package com.ning.killbill;

import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.Argument;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Constructor;
import com.ning.killbill.objects.Method;


import static junit.framework.Assert.assertNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

public class TestClassWithAnnotation extends TestBase {


    @Test(groups = "fast", enabled=false)
    public void testClassWithAnnotation() {


        assertEquals(listener.getPackageName(), "com.ning.billing.jaxrs.json");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testClass = classesorInterfaces.get(0);
        assertEquals(testClass.getName(), "AccountEmailJson");
        assertEquals(testClass.isClass(), true);

        assertEquals(testClass.getCtors().size(), 1);
        final Constructor ctor = testClass.getCtors().get(0);
        assertEquals(ctor.getOrderedArguments().size(), 2);

        Argument argument = ctor.getOrderedArguments().get(0);
        assertEquals(argument.getType(), "java.lang.String");
        assertEquals(argument.getName(), "accountId");
        assertEquals(argument.getAnnotations().size(), 1);
        assertEquals(argument.getAnnotations().get(0).getName(), "JsonProperty");
        assertEquals(argument.getAnnotations().get(0).getValue(), "accountId");

        argument = ctor.getOrderedArguments().get(1);
        assertEquals(argument.getType(), "java.lang.String");
        assertEquals(argument.getName(), "email");
        assertEquals(argument.getAnnotations().size(), 1);
        assertEquals(argument.getAnnotations().get(0).getName(), "JsonProperty");
        assertEquals(argument.getAnnotations().get(0).getValue(), "email");

        assertEquals(ctor.getAnnotations().size(), 1);
        assertEquals(ctor.getAnnotations().get(0).getName(), "JsonCreator");
        assertNull(ctor.getAnnotations().get(0).getValue());

        assertEquals(testClass.getSuperInterfaces().size(), 0);
        assertNotNull(testClass.getSuperBaseClass());

        assertEquals(testClass.getMethods().size(), 3);

        Method mToAccountEmail = getMethod("toAccountEmail", testClass.getMethods());
        assertNotNull(mToAccountEmail);
        assertFalse(mToAccountEmail.isGetter());
        assertEquals(mToAccountEmail.getOrderedArguments().size(), 1);
        assertEquals(mToAccountEmail.getAnnotations().size(), 1);
        assertEquals(mToAccountEmail.getAnnotations().get(0).getName(), "JsonIgnore");
        assertNull(mToAccountEmail.getAnnotations().get(0).getValue());
    }

    @Override
    public String getResourceName() {
        return "ClassWithAnnotation";
    }
}
