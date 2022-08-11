package org.sonatype.nexus.plugins.jenkins.updatecenter.internal;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class UpdateCenterModifier {
    private static final byte[] UPDATESITE_END = "\n);".getBytes(StandardCharsets.UTF_8);
    private static final byte[] UPDATE_CENTER_BEGIN = "updateCenter.post(\n".getBytes(StandardCharsets.UTF_8);
    public static final char START_JSON = '(';
    public static final String PLUGINS_START_TAG = "plugins";
    private final URI nexusHost;

    public UpdateCenterModifier(String nexusUrl){
        this.nexusHost = URI.create(nexusUrl.endsWith("/") ? nexusUrl :  nexusUrl + "/");
    }
    public void modifyUpdateCenterJson(InputStream stream, OutputStream output) throws IOException {
        skipTillStartOfJson(stream);
        try (JsonParser parser = Json.createParser(stream);
             JsonGenerator generator = Json.createGenerator(output);) {
            output.write(UPDATE_CENTER_BEGIN);
            try {
                replaceUpdateCenterJsonWithNexus(parser, generator);
            } catch (JsonParsingException e) {
                // in the update site JSON, there is some javascript code causing an exception, ignore it
            }
            generator.flush();
            output.write(UPDATESITE_END);
        }
    }

    private static void skipTillStartOfJson(InputStream stream) throws IOException {
        for (int pos = 0; pos < 20; pos++) {
            if (stream.read() == START_JSON) {
                break;
            }
        }
    }

    private String modifyUpdateSitePluginUrls(JsonParser parser, JsonGenerator generator) {
        generator.writeStartObject();
        parser.getObjectStream().forEach(o -> parseIndividualPlugin(generator, o));
        generator.writeEnd();
        return null;
    }

    private void parseIndividualPlugin(JsonGenerator generator, Map.Entry<String, JsonValue> o) {
        generator.writeKey(o.getKey());
        Set<Map.Entry<String, JsonValue>> entries = o.getValue().asJsonObject().entrySet();
        generator.writeStartObject();
        for (Map.Entry<String, JsonValue> entry : entries) {
            String key = entry.getKey();
            if ("url".equals(key))
                generator.write(key, replaceURL(entry.getValue()));
            else
                generator.write(key, entry.getValue());
        }
        generator.writeEnd();
    }
    // need to check if the urls are correctly generated
    private String replaceURL(JsonValue url) {
        URI sourceUrl = URI.create(((JsonString) url).getString());
        String[] splitted = sourceUrl.getPath().split("/");
        // keep the last 4 parts of the url for the relative jenkins url
        String lastPath = String.join("/",Arrays.copyOfRange(splitted,splitted.length-4,splitted.length));
        return nexusHost.resolve("./"+lastPath).toString();
    }

    private void replaceUpdateCenterJsonWithNexus(JsonParser parser, JsonGenerator generator) {
        String key = null;
        parser.next();
        generator.writeStartObject();
        while (parser.hasNext()) {
            final JsonParser.Event event = parser.next();
            switch (event) {
                case KEY_NAME:
                    key = parser.getString();
                    break;
                case START_OBJECT:
                    if (PLUGINS_START_TAG.equals(key)) {
                        generator.writeKey(key);
                        modifyUpdateSitePluginUrls(parser, generator);
                    } else {
                        generator.write(key,parser.getObject());
                    }
                    break;
                case VALUE_STRING:
                    generator.write(key, parser.getString());
                    break;
                case START_ARRAY:
                    generator.write(key,parser.getArray());
                    break;
                case END_OBJECT:
                    generator.writeEnd();
                    break;
            }
        }
    }
}