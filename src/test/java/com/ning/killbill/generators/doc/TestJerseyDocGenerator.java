package com.ning.killbill.generators.doc;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Matcher;

public class TestJerseyDocGenerator {

    @Test(groups = "fast")
    public void testIdPattern() {
        Matcher m = JerseyDocGenerator.PATTERN_ID.matcher("{accountId:+UUID_PATTERN+}");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group(1), "accountId");

        m = JerseyDocGenerator.PATTERN_ID.matcher("{paymentMethodId:+UUID_PATTERN+}");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group(1), "paymentMethodId");

        m = JerseyDocGenerator.PATTERN_ID.matcher("{searchKey:+ANYTHING_PATTERN+}+");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group(1), "searchKey");
    }

    @Test(groups = "fast")
    public void testPathPattern() {
        Matcher m = JerseyDocGenerator.PATTERN_PATH.matcher("+PAYMENT_METHODS+");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group(1), "PAYMENT_METHODS");

        m = JerseyDocGenerator.PATTERN_PATH.matcher("JaxrsResource.ACCOUNTS_PATH");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group(1), "ACCOUNTS_PATH");

        m = JerseyDocGenerator.PATTERN_PATH.matcher("API_VERSION");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group(1), "API_VERSION");
    }


}
