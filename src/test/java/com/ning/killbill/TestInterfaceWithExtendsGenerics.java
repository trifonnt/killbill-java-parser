package com.ning.killbill;

import java.util.List;

import com.ning.killbill.objects.MethodOrDecl;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;

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
        assertEquals(testClass.getMethodOrDecls().size(), 5);

       // void registerService(OSGIServiceDescriptor desc, T service);

        MethodOrDecl registerService = getMethod("registerService", testClass.getMethodOrDecls());
        assertNotNull(registerService);
        assertFalse(registerService.isGetter());
        assertEquals(registerService.getReturnValueType().getBaseType(), "void");
        assertEquals(registerService.getOrderedArguments().size(), 2);

        assertEquals(registerService.getOrderedArguments().get(0).getName(), "desc");
        assertEquals(registerService.getOrderedArguments().get(0).getType().getBaseType(), "com.ning.billing.osgi.api.OSGIServiceDescriptor");
        assertEquals(registerService.getOrderedArguments().get(1).getName(), "service");
        assertEquals(registerService.getOrderedArguments().get(1).getType().getBaseType(), KillbillListener.UNDEFINED_GENERIC);

        MethodOrDecl getAllServices = getMethod("getAllServices", testClass.getMethodOrDecls());
        assertNotNull(getAllServices);
        Assert.assertTrue(getAllServices.isGetter());
        assertEquals(getAllServices.getReturnValueType().getBaseType(), "java.util.Set");
        assertEquals(getAllServices.getReturnValueType().getGenericType(), "java.lang.String");
        assertEquals(getAllServices.getOrderedArguments().size(), 0);


        MethodOrDecl unregisterServices = getMethod("unregisterServices", testClass.getMethodOrDecls());
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
