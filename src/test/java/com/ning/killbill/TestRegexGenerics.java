package com.ning.killbill;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class TestRegexGenerics {


    @Test(groups = "fast")
    public void testGenericRegex() {
        Matcher m = KillbillListener.GENERIC_PATTERN.matcher("Integer");
        assertTrue(m.matches());
        Assert.assertEquals(m.group(1), "Integer");

        m = KillbillListener.GENERIC_PATTERN.matcher("Blockable<T>");
        assertTrue(m.matches());
        Assert.assertEquals(m.group(1), "Blockable");
    }
}
