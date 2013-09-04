package com.ning.killbill;

import java.util.List;

import com.ning.killbill.objects.Field;
import com.ning.killbill.objects.MethodOrDecl;
import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestSimpleInterface extends TestBase {

    @Test(groups = "fast")
    public void testSimpleInterface() {
        assertEquals(listener.getPackageName(), "com.ning.billing.account.api");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testInterface = classesorInterfaces.get(0);
        assertEquals(testInterface.getName(), "Account");

        assertEquals(testInterface.getSuperInterfaces().size(), 3);
        assertTrue(isSuperInterfaceDefined("com.ning.billing.junction.api.Blockable", testInterface.getSuperInterfaces()));
        assertTrue(isSuperInterfaceDefined("com.ning.billing.util.entity.Entity", testInterface.getSuperInterfaces()));
        assertTrue(isSuperInterfaceDefined("com.ning.billing.account.api.AccountData", testInterface.getSuperInterfaces()));

        assertEquals(testInterface.isInterface(), true);
        assertEquals(testInterface.isEnum(), false);
        assertEquals(testInterface.isClass(), false);

        assertEquals(testInterface.getMethodOrDecls().size(), 2);
        MethodOrDecl toMutableAccountData = getMethod("toMutableAccountData", testInterface.getMethodOrDecls());
        assertNotNull(toMutableAccountData);
        assertFalse(toMutableAccountData.isGetter());
        assertEquals(toMutableAccountData.getOrderedArguments().size(), 0);
        assertEquals(toMutableAccountData.getReturnValueType().getBaseType(), "com.ning.billing.account.api.MutableAccountData");

        assertEquals(toMutableAccountData.getExceptions().size(), 2);
        assertTrue(toMutableAccountData.getExceptions().contains("com.ning.billing.MyCrazyException"));
        assertTrue(toMutableAccountData.getExceptions().contains("com.ning.billing.account.api.NoSuchAccount"));

        MethodOrDecl mergeWithDelegate = getMethod("mergeWithDelegate", testInterface.getMethodOrDecls());
        assertNotNull(mergeWithDelegate);
        assertFalse(mergeWithDelegate.isGetter());
        assertEquals(mergeWithDelegate.getOrderedArguments().size(), 1);
        assertEquals(mergeWithDelegate.getReturnValueType().getBaseType(), "com.ning.billing.account.api.Account");

        Field argument = mergeWithDelegate.getOrderedArguments().get(0);
        assertEquals(argument.getType().getBaseType(), "com.ning.billing.account.api.Account");
        assertEquals(argument.getName(), "delegate");
    }


    @Override
    public String getResourceName() {
        return "SimpleInterface";
    }

}
