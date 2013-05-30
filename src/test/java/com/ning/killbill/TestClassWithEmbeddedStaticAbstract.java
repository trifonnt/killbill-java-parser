package com.ning.killbill;

import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;

import static org.testng.Assert.assertEquals;

public class TestClassWithEmbeddedStaticAbstract extends TestBase {


    @Test(groups = "fast")
    public void testClassWithEmbeddedStaticAbstract() {


        assertEquals(listener.getPackageName(), "com.ning.billing.jaxrs.json");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 3);

        ClassEnumOrInterface testClass = getClassEnumOrInterface("SubscriptionJsonWithEvents", classesorInterfaces);
        assertEquals(testClass.getName(), "SubscriptionJsonWithEvents");
        assertEquals(testClass.isClass(), true);
        assertEquals(testClass.isAbstract(), false);


        testClass = getClassEnumOrInterface("SubscriptionReadEventJson", classesorInterfaces);
        assertEquals(testClass.getName(), "SubscriptionReadEventJson");
        assertEquals(testClass.isClass(), true);
        assertEquals(testClass.isAbstract(), false);


        testClass = getClassEnumOrInterface("SubscriptionBaseEventJson", classesorInterfaces);
        assertEquals(testClass.getName(), "SubscriptionBaseEventJson");
        assertEquals(testClass.isClass(), true);
        assertEquals(testClass.isAbstract(), true);
    }

    @Override
    public String getResourceName() {
        return "ClassWithEmbeddedStaticAbstract";
    }
}
