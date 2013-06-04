package com.ning.killbill;

import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Method;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestInterfaceWithVoidMethod extends TestBase {



    @Test(groups = "fast")
    public void testInterfaceWithVoidMethod() {
        assertEquals(listener.getPackageName(), "com.ning.billing.account.api");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testInterface = classesorInterfaces.get(0);
        assertEquals(testInterface.getName(), "AccountUserApi");

        assertEquals(testInterface.getMethods().size(), 10);
        Method updateAccount = getMethod("updateAccount", testInterface.getMethods());
        assertNotNull(updateAccount);
        assertFalse(updateAccount.isGetter());
        assertEquals(updateAccount.getOrderedArguments().size(), 2);
        assertEquals(updateAccount.getReturnValueType(), "void");
    }

        @Override
    public String getResourceName() {
        return "InterfaceWithVoidMethod";
    }
}
