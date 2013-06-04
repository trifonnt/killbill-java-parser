package com.ning.killbill;

import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestWithEmbeddedEnumIntoAnnotation extends TestBase {

    @Test(groups = "fast")
    public void testSimpleInterface() {


        assertEquals(listener.getPackageName(), "com.ning.billing.lifecycle");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 2);

        ClassEnumOrInterface testClass = getClassEnumOrInterface("LifecycleLevel", classesorInterfaces);
        assertEquals(testClass.getName(), "LifecycleLevel");
        assertEquals(testClass.isEnum(), true);
        assertEquals(testClass.getEnumValues().size(), 9);

        assertTrue(isEnumValueDefined("LOAD_CATALOG", testClass.getEnumValues()));


        testClass = getClassEnumOrInterface("Sequence", classesorInterfaces);
        assertEquals(testClass.getName(), "Sequence");
        assertEquals(testClass.isEnum(), true);
        assertEquals(testClass.getEnumValues().size(), 4);

        assertTrue(isEnumValueDefined("SHUTDOWN_POST_EVENT_UNREGISTRATION", testClass.getEnumValues()));
    }

    @Override
    public String getResourceName() {
        return "EmbeddedEnumIntoAnnotation";
    }
}
