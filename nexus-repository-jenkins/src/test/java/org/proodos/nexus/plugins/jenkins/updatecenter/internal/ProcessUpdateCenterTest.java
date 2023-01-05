package org.proodos.nexus.plugins.jenkins.updatecenter.internal;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class ProcessUpdateCenterTest {

    private final UpdateCenterModifier updateCenterModifier = new UpdateCenterModifier("http://nexusHost/jenkinsmirror");

    @Test
    public void verifyIfUpdateCenterUrlsWereChanged() throws IOException {
        Path targetTestFile = Paths.get("target/output.json");
        Path sourceFile = Paths.get("src/test/resources/updatecenter/update-center.json");
        Path expectedUpdateCenter = Paths.get("src/test/resources/updatecenter/wantedUpdateCenter.json");
        try (InputStream stream = Files.newInputStream(sourceFile);
             OutputStream output =  Files.newOutputStream(targetTestFile)) {
            updateCenterModifier.modifyUpdateCenterJson(stream, output);
            Iterator<String> wantedContent = Files.readAllLines(expectedUpdateCenter).iterator();
            Iterator<String> resultingContent = Files.readAllLines(targetTestFile).iterator();
            wantedContent.forEachRemaining(l -> {
                String result = resultingContent.next();
                Assert.assertArrayEquals(l.split(","),result.split(","));
            });
        }
    }

}
