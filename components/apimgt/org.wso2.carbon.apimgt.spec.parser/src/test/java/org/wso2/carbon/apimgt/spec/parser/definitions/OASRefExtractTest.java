/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.spec.parser.definitions;

import org.junit.Test;
import java.util.Set;
import static org.junit.Assert.*;

public class OASRefExtractTest {

    @Test
    public void extractsRefsWithFilePartIgnoresPureFragments() {
        String yaml =
            "openapi: 3.0.1\n" +
            "components:\n" +
            "  schemas:\n" +
            "    A: { $ref: 'http://h/a.yaml#/X' }\n" +
            "    B: { $ref: './b.yaml' }\n" +
            "    C: { $ref: '#/components/schemas/A' }\n";
        Set<String> refs = OASParserUtil.extractRefStrings(yaml);
        assertTrue(refs.contains("http://h/a.yaml#/X"));
        assertTrue(refs.contains("./b.yaml"));
        assertFalse("in-document fragments are ignored",
                refs.contains("#/components/schemas/A"));
    }

    @Test
    public void resolvesAbsoluteRelativeAndRejectsNonHttp() {
        assertEquals("http://h/a.yaml",
                OASParserUtil.resolveToHttpUrl("http://h/a.yaml#/X", null));
        assertEquals("http://h/dir/b.yaml",
                OASParserUtil.resolveToHttpUrl("./b.yaml", "http://h/dir/main.yaml"));
        assertEquals("http://h/c.yaml",
                OASParserUtil.resolveToHttpUrl("../c.yaml", "http://h/dir/main.yaml"));
        assertNull(OASParserUtil.resolveToHttpUrl("./b.yaml", null));
        assertNull(OASParserUtil.resolveToHttpUrl("./b.yaml", "file:/tmp/main.yaml")); // local base
        assertNull(OASParserUtil.resolveToHttpUrl("#/x", "http://h/m.yaml"));
        assertNull(OASParserUtil.resolveToHttpUrl("file:///etc/passwd", null));
    }

    @Test
    public void malformedContentReturnsEmptyNeverThrows() {
        assertTrue(OASParserUtil.extractRefStrings(":::not yaml or json:::").isEmpty());
        assertTrue(OASParserUtil.extractRefStrings(null).isEmpty());
    }
}
