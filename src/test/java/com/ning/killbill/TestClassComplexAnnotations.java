package com.ning.killbill;

import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Method;
import org.testng.annotations.Test;

import java.util.List;

import static junit.framework.Assert.assertNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TestClassComplexAnnotations extends TestBase {

    @Test(groups = "fast")
    public void testClassWithAnnotation() {

        assertEquals(listener.getPackageName(), "com.ning.billing.jaxrs.resources");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testClass = classesorInterfaces.get(0);
        assertEquals(testClass.getName(), "AccountResource");
        assertEquals(testClass.isClass(), true);


        Method mGetAccount = getMethod("getAccount", testClass.getMethods());
        assertNotNull(mGetAccount);
        assertEquals(mGetAccount.getAnnotations().size(), 3);

        assertEquals(mGetAccount.getAnnotations().get(0).getName(), "GET");
        assertNull(mGetAccount.getAnnotations().get(0).getValue());

        assertEquals(mGetAccount.getAnnotations().get(1).getName(), "Path");
        //assertNull(mGetAccount.getAnnotations().get(0).getValue(), "/{accountId:" + UUID_PATTERN + "}");
        //System.out.println("value = " + mGetAccount.getAnnotations().get(1).getValue());

        assertEquals(mGetAccount.getAnnotations().get(2).getName(), "Produces");
        //System.out.println("value = " + mGetAccount.getAnnotations().get(2).getValue());

    }

    @Override
    public String getResourceName() {
        return "ClassComplexAnnotations";
    }
}
