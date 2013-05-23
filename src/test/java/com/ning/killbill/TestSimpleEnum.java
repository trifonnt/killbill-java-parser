package com.ning.killbill;

import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestSimpleEnum extends TestBase {


    @Test(groups = "fast")
    public void testSimpleInterface() {


        assertEquals(listener.getPackageName(), "com.ning.billing.payment.api");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testClass = classesorInterfaces.get(0);
        assertEquals(testClass.getName(), "PaymentStatus");
        assertEquals(testClass.isEnum(), true);
        assertEquals(testClass.getEnumValues().size(), 8);

        assertTrue(isEnumValueDefined("SUCCESS", testClass.getEnumValues()));
        assertTrue(isEnumValueDefined("UNKNOWN", testClass.getEnumValues()));
        assertTrue(isEnumValueDefined("AUTO_PAY_OFF", testClass.getEnumValues()));
        assertTrue(isEnumValueDefined("PAYMENT_FAILURE", testClass.getEnumValues()));
        assertTrue(isEnumValueDefined("PAYMENT_FAILURE_ABORTED", testClass.getEnumValues()));
        assertTrue(isEnumValueDefined("PLUGIN_FAILURE_ABORTED", testClass.getEnumValues()));
        assertTrue(isEnumValueDefined("PLUGIN_FAILURE_ABORTED", testClass.getEnumValues()));
        assertTrue(isEnumValueDefined("PAYMENT_SYSTEM_OFF", testClass.getEnumValues()));

    }

    protected boolean isEnumValueDefined(String value, List<String> enumValues) {
        for (String cur : enumValues) {
            if (cur.equals(value)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String getResourceName() {
        return "SimpleEnum";
    }

}
