package com.ning.killbill;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.Argument;
import com.ning.killbill.objects.ClassOrInterface;
import com.ning.killbill.objects.Method;

import com.google.common.io.Resources;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestSimpleClass extends TestBase {

    @Test(groups = "fast")
    public void testSimpleInterface() {


        assertEquals(listener.getPackageName(), "com.ning.billing.payment.api");
        final List<ClassOrInterface> classesorInterfaces = listener.getAllClassesOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassOrInterface testClass = classesorInterfaces.get(0);
        assertEquals(testClass.getName(), "PaymentMethodKVInfo");
        assertEquals(testClass.isInterface(), false);

        assertEquals(testClass.getSuperInterfaces().size(), 2);
        isSuperInterfaceDefined("com.ning.billing.payment.api.Bar", testClass.getSuperInterfaces());
        isSuperInterfaceDefined("com.ning.billing.payment.api.Buzz", testClass.getSuperInterfaces());

        assertNotNull(testClass.getSuperBaseClass());
        assertEquals(testClass.getSuperBaseClass(), "com.ning.billing.payment.Foo");

        assertEquals(testClass.getMethods().size(), 6);

        Method hashCode = getMethod("hashCode", testClass.getMethods());
        assertNotNull(hashCode);
        assertFalse(hashCode.isGetter());
        assertEquals(hashCode.getOrderedArguments().size(), 0);

        Method mEquals = getMethod("equals", testClass.getMethods());
        assertNotNull(mEquals);
        assertFalse(mEquals.isGetter());
        assertEquals(mEquals.getOrderedArguments().size(), 1);

        Argument argument = mEquals.getOrderedArguments().get(0);
        assertEquals(argument.getType(), "java.lang.Object");
        assertEquals(argument.getName(), "o");

        Method mToString = getMethod("toString", testClass.getMethods());
        assertNotNull(mToString);
        assertFalse(mToString.isGetter());
        assertEquals(mToString.getOrderedArguments().size(), 0);

        Method mGetIsUpdatable = getMethod("getIsUpdatable", testClass.getMethods());
        assertNotNull(mGetIsUpdatable);
        assertTrue(mGetIsUpdatable.isGetter());
        assertEquals(mToString.getOrderedArguments().size(), 0);

        Method mGetValue = getMethod("getValue", testClass.getMethods());
        assertNotNull(mGetValue);
        assertTrue(mGetValue.isGetter());
        assertEquals(mGetValue.getOrderedArguments().size(), 0);

        Method mGetKey = getMethod("getKey", testClass.getMethods());
        assertNotNull(mGetKey);
        assertTrue(mGetKey.isGetter());
        assertEquals(mGetKey.getOrderedArguments().size(), 0);

    }


    @Override
    public String getResourceFileName() throws IOException, URISyntaxException {
        URL resource = Resources.getResource("SimpleClass");
        File resourceFile = new File(resource.toURI());
        return resourceFile.getAbsolutePath();
    }
}
