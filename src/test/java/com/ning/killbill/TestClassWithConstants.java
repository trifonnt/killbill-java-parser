package com.ning.killbill;

import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.MethodOrDecl;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;


public class TestClassWithConstants extends TestBase {

    @Test(groups = "fast")
    public void testSimpleClass() {


        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testClass = classesorInterfaces.get(0);
        final List<MethodOrDecl> methodOrDecls = testClass.getMethodOrDecls();
        assertEquals(methodOrDecls.size(), 7);


        assertEquals(methodOrDecls.get(0).getName(), "API_PREFIX");
        assertEquals(methodOrDecls.get(0).getInitializerValue(), "");

        assertEquals(methodOrDecls.get(1).getName(), "API_VERSION");
        assertEquals(methodOrDecls.get(1).getInitializerValue(), "/1.0");

        assertEquals(methodOrDecls.get(2).getName(), "API_POSTFIX");
        assertEquals(methodOrDecls.get(2).getInitializerValue(), "/kb");

        assertEquals(methodOrDecls.get(3).getName(), "PREFIX");
        assertEquals(methodOrDecls.get(3).getInitializerValue(), "API_PREFIX+API_VERSION+API_POSTFIX");

        assertEquals(methodOrDecls.get(4).getName(), "SECURITY");
        assertEquals(methodOrDecls.get(4).getInitializerValue(), "security");

        assertEquals(methodOrDecls.get(5).getName(), "SECURITY_PATH");
        assertEquals(methodOrDecls.get(5).getInitializerValue(), "API_PREFIX+/+API_PREFIX2");

        assertEquals(methodOrDecls.get(6).getName(), "methodFoo");
        assertNull(methodOrDecls.get(6).getInitializerValue());

    }


    @Override
    public String getResourceName() {
        return "ClassWithConstants";
    }
}
