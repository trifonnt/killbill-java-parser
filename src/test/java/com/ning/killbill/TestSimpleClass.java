package com.ning.killbill;

import java.util.List;

import com.ning.killbill.objects.Field;
import com.ning.killbill.objects.MethodOrDecl;
import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;

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

        assertEquals(testClass.getMethodOrDecls().size(), 7);

        MethodOrDecl getKey = getMethod("getKey", testClass.getMethodOrDecls());
        assertNotNull(getKey);
        assertTrue(getKey.isGetter());
        assertEquals(getKey.getReturnValueType().getBaseType(), "java.lang.String");
        assertEquals(getKey.getOrderedArguments().size(), 0);

        MethodOrDecl hashCode = getMethod("hashCode", testClass.getMethodOrDecls());
        assertNotNull(hashCode);
        assertFalse(hashCode.isGetter());
        assertEquals(hashCode.getReturnValueType().getBaseType(), "int");
        assertEquals(hashCode.getOrderedArguments().size(), 0);

        MethodOrDecl mEquals = getMethod("equals", testClass.getMethodOrDecls());
        assertNotNull(mEquals);
        assertFalse(mEquals.isGetter());
        assertEquals(mEquals.getReturnValueType().getBaseType(), "boolean");
        assertEquals(mEquals.getOrderedArguments().size(), 1);

        Field argument = mEquals.getOrderedArguments().get(0);
        assertEquals(argument.getType().getBaseType(), "java.lang.Object");
        assertEquals(argument.getName(), "o");


        MethodOrDecl mToString = getMethod("toString", testClass.getMethodOrDecls());
        assertNotNull(mToString);
        assertFalse(mToString.isGetter());
        assertEquals(mToString.getReturnValueType().getBaseType(), "java.lang.String");
        assertEquals(mToString.getOrderedArguments().size(), 0);

        MethodOrDecl mGetIsUpdatable = getMethod("getIsUpdatable", testClass.getMethodOrDecls());
        assertNotNull(mGetIsUpdatable);
        assertTrue(mGetIsUpdatable.isGetter());
        assertEquals(mGetIsUpdatable.getReturnValueType().getBaseType(), "java.lang.Boolean");
        assertEquals(mGetIsUpdatable.getOrderedArguments().size(), 0);

        MethodOrDecl mGetValue = getMethod("getValue", testClass.getMethodOrDecls());
        assertNotNull(mGetValue);
        assertTrue(mGetValue.isGetter());
        assertEquals(mGetValue.getReturnValueType().getBaseType(), "int");
        assertEquals(mGetValue.getOrderedArguments().size(), 0);

        MethodOrDecl mGetKey = getMethod("getKey", testClass.getMethodOrDecls());
        assertNotNull(mGetKey);
        assertTrue(mGetKey.isGetter());
        assertEquals(mGetKey.getReturnValueType().getBaseType(), "java.lang.String");
        assertEquals(mGetKey.getOrderedArguments().size(), 0);

        MethodOrDecl meMthodWithThreeArgs = getMethod("methodWithThreeArgs", testClass.getMethodOrDecls());
        assertNotNull(meMthodWithThreeArgs);
        assertFalse(meMthodWithThreeArgs.isGetter());
        assertEquals(meMthodWithThreeArgs.getReturnValueType().getBaseType(), "void");
        assertEquals(meMthodWithThreeArgs.getOrderedArguments().size(), 3);

        argument = meMthodWithThreeArgs.getOrderedArguments().get(0);
        assertEquals(argument.getType().getBaseType(), "java.lang.Boolean");
        assertEquals(argument.getName(), "b");

        argument = meMthodWithThreeArgs.getOrderedArguments().get(1);
        assertEquals(argument.getType().getBaseType(), "int");
        assertEquals(argument.getName(), "foo");

        argument = meMthodWithThreeArgs.getOrderedArguments().get(2);
        assertEquals(argument.getType().getBaseType(), "java.lang.Object");
        assertEquals(argument.getName(), "o");
    }


    @Override
    public String getResourceName() {
        return "SimpleClass";
    }
}
