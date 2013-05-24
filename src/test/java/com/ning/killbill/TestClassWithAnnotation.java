package com.ning.killbill;

import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.Argument;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Constructor;

import static org.testng.Assert.assertEquals;
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

        argument = ctor.getOrderedArguments().get(1);
        assertEquals(argument.getType(), "java.lang.String");
        assertEquals(argument.getName(), "email");

        assertEquals(testClass.getSuperInterfaces().size(), 0);
        assertNotNull(testClass.getSuperBaseClass());
    }

    @Override
    public String getResourceName() {
        return "ClassWithAnnotation";
    }
}
