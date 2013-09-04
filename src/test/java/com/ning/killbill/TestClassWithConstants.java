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
        assertEquals(methodOrDecls.size(), 3);

        assertEquals(methodOrDecls.get(0).getName(), "API_PREFIX");
        assertEquals(methodOrDecls.get(0).getInitializerValue(), "boo");
        assertEquals(methodOrDecls.get(1).getName(), "API_PREFIX2");
        assertEquals(methodOrDecls.get(1).getInitializerValue(), "grrrrrr");
        assertEquals(methodOrDecls.get(2).getName(), "methodFoo");
        assertNull(methodOrDecls.get(2).getInitializerValue());

    }


    @Override
    public String getResourceName() {
        return "ClassWithConstants";
    }
}
