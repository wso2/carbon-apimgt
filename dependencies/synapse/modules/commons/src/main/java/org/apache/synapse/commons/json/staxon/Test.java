package org.apache.synapse.commons.json.staxon;

import de.odysseus.staxon.json.stream.JsonStreamToken;

import java.io.IOException;
import java.io.StringReader;

final class Test {
    public void testCase3() {
        String zero = "{\"jsonArray\":{\"jsonElement\":{\"vale\":1}}}";
        String one = "{\"array\":[1]}";          // COLON   =>   START_ARRAY
        String two = "[1, 2, 3]";                // null   =>   START_ARRAY
        String three = "{\"array\":[[1]]}";      // START_ARRAY   =>   START_ARRAY
        String four = "{\"type\":\"Polygon\",\"coordinates\":[[[116.0865381,-8.608804],[116.127196,-8.608804],[116.127196,-8.554822],[116.0865381,-8.554822]]]}";
        // START_ARRAY   =>   START_ARRAY and COMMA   =>   START_ARRAY
        StringReader reader = new StringReader(four);
        JsonStreamSourceImpl source = new JsonStreamSourceImpl(new JsonScanner(reader), true);
        try {
            JsonStreamToken token;
            while ((token = source.peek()) != JsonStreamToken.NONE) {
                source.poll(token);
            }
            source.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
