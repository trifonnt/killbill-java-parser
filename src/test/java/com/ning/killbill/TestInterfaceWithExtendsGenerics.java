package com.ning.killbill;

import java.util.List;

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
        assertEquals(registerService.getReturnValueType(), "void");
        assertEquals(registerService.getOrderedArguments().size(), 2);

        assertEquals(registerService.getOrderedArguments().get(0).getName(), "desc");
        assertEquals(registerService.getOrderedArguments().get(0).getType(), "com.ning.billing.osgi.api.OSGIServiceDescriptor");
        assertEquals(registerService.getOrderedArguments().get(1).getName(), "service");
        assertEquals(registerService.getOrderedArguments().get(1).getType(), KillbillListener.UNDEFINED_GENERIC);
    }

        @Override
    public String getResourceName() {
        return "InterfaceWithExtendGenerics";
    }
}
