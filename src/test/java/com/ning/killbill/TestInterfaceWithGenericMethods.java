package com.ning.killbill;

import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.MethodOrDecl;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class TestInterfaceWithGenericMethods extends TestBase {

    @Test(groups = "fast")
    public void testInterfaceWithGenericMethods() {
        assertEquals(listener.getPackageName(), "com.ning.billing.overdue");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testClass = classesorInterfaces.get(0);
        assertEquals(testClass.getName(), "OverdueUserApi");
        assertEquals(testClass.isInterface(), true);
        assertEquals(testClass.getMethodOrDecls().size(), 5);

        MethodOrDecl refreshOverdueStateFor = getMethod("refreshOverdueStateFor", testClass.getMethodOrDecls());
        assertNotNull(refreshOverdueStateFor);
        assertFalse(refreshOverdueStateFor.isGetter());
        assertEquals(refreshOverdueStateFor.getReturnValueType().getBaseType(), "com.ning.billing.overdue.OverdueState");
        // TODO This currently fails as the KillbillListener evaluates the generic too late and the mapping is not present at the time  (enterTypeParameters is called too late)
        //assertEquals(refreshOverdueStateFor.getReturnValueType().getGenericType(), "com.ning.billing.overdue.Blockable");
        assertEquals(refreshOverdueStateFor.getOrderedArguments().size(), 2);

        assertEquals(refreshOverdueStateFor.getOrderedArguments().get(0).getName(), "overdueable");
        assertEquals(refreshOverdueStateFor.getOrderedArguments().get(0).getType().getBaseType(), "com.ning.billing.junction.api.Blockable");
        assertEquals(refreshOverdueStateFor.getOrderedArguments().get(1).getName(), "context");
        assertEquals(refreshOverdueStateFor.getOrderedArguments().get(1).getType().getBaseType(), "com.ning.billing.util.callcontext.CallContext");

        MethodOrDecl setOverrideBillingStateForAccount = getMethod("setOverrideBillingStateForAccount", testClass.getMethodOrDecls());
        assertNotNull(setOverrideBillingStateForAccount);
        assertFalse(setOverrideBillingStateForAccount.isGetter());
        assertEquals(setOverrideBillingStateForAccount.getReturnValueType().getBaseType(), "void");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().size(), 3);

        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(0).getName(), "overdueable");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(0).getType().getBaseType(), "com.ning.billing.junction.api.Blockable");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(1).getName(), "state");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(1).getType().getBaseType(), "com.ning.billing.overdue.config.api.BillingState");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(2).getName(), "context");
        assertEquals(setOverrideBillingStateForAccount.getOrderedArguments().get(2).getType().getBaseType(), "com.ning.billing.util.callcontext.CallContext");

        MethodOrDecl getMap = getMethod("getMap", testClass.getMethodOrDecls());
        assertNotNull(getMap);
        assertEquals(getMap.getReturnValueType().getBaseType(), "java.util.Map");
        assertEquals(getMap.getReturnValueType().getGenericSubTypes().size(), 2);
        assertEquals(getMap.getReturnValueType().getGenericSubTypes().get(0).getBaseType(), "java.lang.String");
        assertNull(getMap.getReturnValueType().getGenericSubTypes().get(0).getGenericType());
        assertEquals(getMap.getReturnValueType().getGenericSubTypes().get(1).getBaseType(), "java.util.List");
        assertEquals(getMap.getReturnValueType().getGenericSubTypes().get(1).getGenericType(), "java.lang.Integer");
    }

    @Override
    public String getResourceName() {
        return "InterfaceWithGenericMethods";
    }
}
