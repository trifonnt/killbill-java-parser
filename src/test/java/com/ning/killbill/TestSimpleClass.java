package com.ning.killbill;

import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.Field;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Method;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestSimpleClass extends TestBase {

    @Test(groups = "fast")
    public void testSimpleClass() {


        assertEquals(listener.getPackageName(), "com.ning.billing.payment.api");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testClass = classesorInterfaces.get(0);
        assertEquals(testClass.getName(), "PaymentMethodKVInfo");
        assertEquals(testClass.isInterface(), false);
        assertEquals(testClass.isEnum(), false);
        assertEquals(testClass.isClass(), true);
        assertTrue(testClass.isAbstract());

        assertEquals(testClass.getSuperInterfaces().size(), 2);
        isSuperInterfaceDefined("com.ning.billing.payment.api.InterfaceWithGenericMethods", testClass.getSuperInterfaces());
        isSuperInterfaceDefined("com.ning.billing.payment.api.Buzz", testClass.getSuperInterfaces());

        assertNotNull(testClass.getSuperBaseClass());
        assertEquals(testClass.getSuperBaseClass(), "com.ning.billing.payment.Foo");

        assertEquals(testClass.getMethods().size(), 7);

        Method getKey = getMethod("getKey", testClass.getMethods());
        assertNotNull(getKey);
        assertTrue(getKey.isGetter());
        assertEquals(getKey.getReturnValueType(), "java.lang.String");
        assertEquals(getKey.getOrderedArguments().size(), 0);

        Method hashCode = getMethod("hashCode", testClass.getMethods());
        assertNotNull(hashCode);
        assertFalse(hashCode.isGetter());
        assertEquals(hashCode.getReturnValueType(), "int");
        assertEquals(hashCode.getOrderedArguments().size(), 0);

        Method mEquals = getMethod("equals", testClass.getMethods());
        assertNotNull(mEquals);
        assertFalse(mEquals.isGetter());
        assertEquals(mEquals.getReturnValueType(), "boolean");
        assertEquals(mEquals.getOrderedArguments().size(), 1);

        Field argument = mEquals.getOrderedArguments().get(0);
        assertEquals(argument.getType(), "java.lang.Object");
        assertEquals(argument.getName(), "o");


        Method mToString = getMethod("toString", testClass.getMethods());
        assertNotNull(mToString);
        assertFalse(mToString.isGetter());
        assertEquals(mToString.getReturnValueType(), "java.lang.String");
        assertEquals(mToString.getOrderedArguments().size(), 0);

        Method mGetIsUpdatable = getMethod("getIsUpdatable", testClass.getMethods());
        assertNotNull(mGetIsUpdatable);
        assertTrue(mGetIsUpdatable.isGetter());
        assertEquals(mGetIsUpdatable.getReturnValueType(), "java.lang.Boolean");
        assertEquals(mGetIsUpdatable.getOrderedArguments().size(), 0);

        Method mGetValue = getMethod("getValue", testClass.getMethods());
        assertNotNull(mGetValue);
        assertTrue(mGetValue.isGetter());
        assertEquals(mGetValue.getReturnValueType(), "int");
        assertEquals(mGetValue.getOrderedArguments().size(), 0);

        Method mGetKey = getMethod("getKey", testClass.getMethods());
        assertNotNull(mGetKey);
        assertTrue(mGetKey.isGetter());
        assertEquals(mGetKey.getReturnValueType(), "java.lang.String");
        assertEquals(mGetKey.getOrderedArguments().size(), 0);

        Method meMthodWithThreeArgs = getMethod("methodWithThreeArgs", testClass.getMethods());
        assertNotNull(meMthodWithThreeArgs);
        assertFalse(meMthodWithThreeArgs.isGetter());
        assertEquals(meMthodWithThreeArgs.getReturnValueType(), "void");
        assertEquals(meMthodWithThreeArgs.getOrderedArguments().size(), 3);

        argument = meMthodWithThreeArgs.getOrderedArguments().get(0);
        assertEquals(argument.getType(), "java.lang.Boolean");
        assertEquals(argument.getName(), "b");

        argument = meMthodWithThreeArgs.getOrderedArguments().get(1);
        assertEquals(argument.getType(), "int");
        assertEquals(argument.getName(), "foo");

        argument = meMthodWithThreeArgs.getOrderedArguments().get(2);
        assertEquals(argument.getType(), "java.lang.Object");
        assertEquals(argument.getName(), "o");
    }


    @Override
    public String getResourceName() {
        return "SimpleClass";
    }
}
