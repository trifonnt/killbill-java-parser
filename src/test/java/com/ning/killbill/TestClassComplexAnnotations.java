package com.ning.killbill;

import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.MethodOrDecl;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class TestClassComplexAnnotations extends TestBase {

    @Test(groups = "fast")
    public void testClassWithAnnotation() {

        assertEquals(listener.getPackageName(), "com.ning.billing.jaxrs.resources");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);


        final ClassEnumOrInterface testClass = classesorInterfaces.get(0);
        assertEquals(testClass.getName(), "AccountResource");
        assertEquals(testClass.isClass(), true);
        assertEquals(testClass.getAnnotations().size(), 2);
        assertEquals(testClass.getAnnotations().get(0).getName(), "Singleton");
        assertNull(testClass.getAnnotations().get(0).getValue());
        assertEquals(testClass.getAnnotations().get(1).getName(), "Path");
        assertEquals(testClass.getAnnotations().get(1).getValue(), "JaxrsResource.ACCOUNTS_PATH");

        MethodOrDecl mGetAccount = getMethod("getAccount", testClass.getMethodOrDecls());
        assertNotNull(mGetAccount);
        assertEquals(mGetAccount.getAnnotations().size(), 3);

        assertEquals(mGetAccount.getAnnotations().get(0).getName(), "GET");
        assertNull(mGetAccount.getAnnotations().get(0).getValue());

        assertEquals(mGetAccount.getAnnotations().get(1).getName(), "Path");
        assertEquals(mGetAccount.getAnnotations().get(1).getValue(), "/{accountId:+UUID_PATTERN+}");
        assertEquals(mGetAccount.getAnnotations().get(2).getName(), "Produces");
        assertEquals(mGetAccount.getAnnotations().get(2).getValue(), "APPLICATION_JSON");
    }

    @Override
    public String getResourceName() {
        return "ClassWithComplexAnnotations";
    }
}
