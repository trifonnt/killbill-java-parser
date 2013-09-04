package com.ning.killbill;

import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestInterfaceWithEnums extends TestBase {

    @Test(groups = "fast")
    public void testSimpleInterface() {
        assertEquals(listener.getPackageName(), "com.ning.billing.entitlement.api.user");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 3);

        final ClassEnumOrInterface testInterface = getClassEnumOrInterface("Subscription", classesorInterfaces);
        assertNotNull(testInterface);
        assertEquals(testInterface.isInterface(), true);
        assertEquals(testInterface.getSuperInterfaces().size(), 2);
        assertTrue(isSuperInterfaceDefined("com.ning.billing.junction.api.Blockable", testInterface.getSuperInterfaces()));
        assertTrue(isSuperInterfaceDefined("com.ning.billing.util.entity.Entity", testInterface.getSuperInterfaces()));
        assertEquals(testInterface.getMethodOrDecls().size(), 26);

        final ClassEnumOrInterface testEnum1 = getClassEnumOrInterface("SubscriptionSourceType", classesorInterfaces);
        assertNotNull(testEnum1);
        assertEquals(testEnum1.isEnum(), true);

        final ClassEnumOrInterface testEnum2 = getClassEnumOrInterface("SubscriptionState", classesorInterfaces);
        assertNotNull(testEnum2);
        assertEquals(testEnum2.isEnum(), true);
    }

    @Override
    public String getResourceName() {
        return "InterfaceWithEnums";
    }

}
