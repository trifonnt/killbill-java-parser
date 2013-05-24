package com.ning.killbill;

import java.util.List;

import org.testng.annotations.Test;

import com.ning.killbill.objects.ClassEnumOrInterface;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestEnumWithFields extends TestBase {

    @Test(groups = "fast")
    public void testSimpleInterface() {


        assertEquals(listener.getPackageName(), "com.ning.billing.util.dao");
        final List<ClassEnumOrInterface> classesorInterfaces = listener.getAllClassesEnumOrInterfaces();
        assertEquals(classesorInterfaces.size(), 1);

        final ClassEnumOrInterface testClass = classesorInterfaces.get(0);
        assertEquals(testClass.getName(), "TableName");
        assertEquals(testClass.isEnum(), true);
        assertEquals(testClass.getEnumValues().size(), 27);

        assertTrue(isEnumValueDefined("ACCOUNT_HISTORY", testClass.getEnumValues()));
        assertTrue(isEnumValueDefined("ACCOUNT", testClass.getEnumValues()));
        assertTrue(isEnumValueDefined("ACCOUNT_EMAIL_HISTORY", testClass.getEnumValues()));
        assertTrue(isEnumValueDefined("TAG", testClass.getEnumValues()));
    }
        @Override
    public String getResourceName() {
        return "EnumWithFields";
    }
}
