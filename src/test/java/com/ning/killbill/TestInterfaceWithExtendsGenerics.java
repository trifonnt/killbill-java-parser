package com.ning.killbill;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Method;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

public class TestInterfaceWithExtendsGenerics extends TestBase {


    @Test(groups = "fast")
    public void testInterfaceWithExtendsGenerics() {


        assertEquals(listener.getPackageName(), "com.ning.billing.osgi.api");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testClass = classesorInterfaces.get(0);
        assertEquals(testClass.getName(), "OSGIServiceRegistration");
        assertEquals(testClass.isInterface(), true);
        assertEquals(testClass.getMethods().size(), 5);

       // void registerService(OSGIServiceDescriptor desc, T service);

        Method registerService = getMethod("registerService", testClass.getMethods());
        assertNotNull(registerService);
        assertFalse(registerService.isGetter());
        assertEquals(registerService.getReturnValueType().getBaseType(), "void");
        assertEquals(registerService.getOrderedArguments().size(), 2);

        assertEquals(registerService.getOrderedArguments().get(0).getName(), "desc");
        assertEquals(registerService.getOrderedArguments().get(0).getType().getBaseType(), "com.ning.billing.osgi.api.OSGIServiceDescriptor");
        assertEquals(registerService.getOrderedArguments().get(1).getName(), "service");
        assertEquals(registerService.getOrderedArguments().get(1).getType().getBaseType(), KillbillListener.UNDEFINED_GENERIC);

        Method getAllServices = getMethod("getAllServices", testClass.getMethods());
        assertNotNull(getAllServices);
        Assert.assertTrue(getAllServices.isGetter());
        assertEquals(getAllServices.getReturnValueType().getBaseType(), "java.util.Set");
        assertEquals(getAllServices.getReturnValueType().getGenericType(), "java.lang.String");
        assertEquals(getAllServices.getOrderedArguments().size(), 0);


        Method unregisterServices = getMethod("unregisterServices", testClass.getMethods());
        assertNotNull(unregisterServices);
        assertFalse(unregisterServices.isGetter());
        assertEquals(unregisterServices.getReturnValueType().getBaseType(), "void");
        assertEquals(unregisterServices.getOrderedArguments().size(), 1);

        assertEquals(unregisterServices.getOrderedArguments().get(0).getName(), "serviceName");
        assertEquals(unregisterServices.getOrderedArguments().get(0).getType().getBaseType(), "java.util.List");
        assertEquals(unregisterServices.getOrderedArguments().get(0).getType().getGenericType(), "java.lang.String");
    }

        @Override
    public String getResourceName() {
        return "InterfaceWithExtendGenerics";
    }
}
