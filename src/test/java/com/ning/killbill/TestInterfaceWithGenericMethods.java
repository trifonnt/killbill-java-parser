package com.ning.killbill;

import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Method;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

public class TestInterfaceWithGenericMethods extends TestBase {


    @Test(groups = "fast")
    public void testInterfaceWithGenericMethods() {


        assertEquals(listener.getPackageName(), "com.ning.billing.overdue");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testClass = classesorInterfaces.get(0);
        assertEquals(testClass.getName(), "OverdueUserApi");
        assertEquals(testClass.isInterface(), true);
        assertEquals(testClass.getMethods().size(), 4);

        Method refreshOverdueStateFor = getMethod("refreshOverdueStateFor", testClass.getMethods());
        assertNotNull(refreshOverdueStateFor);
        assertFalse(refreshOverdueStateFor.isGetter());
        assertEquals(refreshOverdueStateFor.getReturnValueType(), "com.ning.billing.overdue.OverdueState");
        assertEquals(refreshOverdueStateFor.getOrderedArguments().size(), 2);

        assertEquals(refreshOverdueStateFor.getOrderedArguments().get(0).getName(), "overdueable");
        assertEquals(refreshOverdueStateFor.getOrderedArguments().get(0).getType(), "com.ning.billing.junction.api.Blockable");
        assertEquals(refreshOverdueStateFor.getOrderedArguments().get(1).getName(), "context");
        assertEquals(refreshOverdueStateFor.getOrderedArguments().get(1).getType(), "com.ning.billing.util.callcontext.CallContext");

        // T overdueable, BillingState<T> state, CallContext context
        Method setOverrideBillingStateForAccount = getMethod("setOverrideBillingStateForAccount", testClass.getMethods());
        assertNotNull(setOverrideBillingStateForAccount);
        assertFalse(setOverrideBillingStateForAccount.isGetter());
        assertEquals(setOverrideBillingStateForAccount.getReturnValueType(), "void");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().size(), 3);

        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(0).getName(), "overdueable");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(0).getType(), "com.ning.billing.junction.api.Blockable");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(1).getName(), "state");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(1).getType(), "com.ning.billing.overdue.config.api.BillingState");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(2).getName(), "context");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(2).getType(), "com.ning.billing.util.callcontext.CallContext");

    }

    @Override
    public String getResourceName() {
        return "InterfaceWithGenericMethods";
    }
}
