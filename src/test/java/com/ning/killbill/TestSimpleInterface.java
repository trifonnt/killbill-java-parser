package com.ning.killbill;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;


import org.testng.annotations.Test;

import com.google.common.io.Resources;

public class TestSimpleInterface extends TestBase {

    @Test(groups = "fast")
    public void testSimpleInterface() {

    }

    @Override
    public String getResourceFileName() throws IOException, URISyntaxException {
        URL resource = Resources.getResource("SimpleInterface");
        File resourceFile = new File(resource.toURI());
        return resourceFile.getAbsolutePath();
    }
}
