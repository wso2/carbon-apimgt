package org.wso2.carbon.apimgt.ballerina.threatprotection;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.ballerina.threatprotection.analyzer.XMLAnalyzer;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.JSONConfig;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.XMLConfig;

public class XMLAnalyzerTestCase {
    private XMLConfig xmlConfig;

    @BeforeTest
    public void init() {
        xmlConfig = new XMLConfig();
        xmlConfig.setMaxAttributeCount(1);
        xmlConfig.setMaxChildrenPerElement(5);
        xmlConfig.setEntityExpansionLimit(5);
        xmlConfig.setMaxAttributeLength(1);
        xmlConfig.setMaxElementCount(5);
        xmlConfig.setMaxDepth(5);
        xmlConfig.setDtdEnabled(false);
        xmlConfig.setExternalEntitiesEnabled(false);
    }


    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testConfigureAnalyzerException() throws Exception {
        XMLAnalyzer analyzer = new XMLAnalyzer();
        JSONConfig config = new JSONConfig();
        analyzer.configure(config);
    }

    @Test
    public void testConfigureAnalyzer() throws Exception {
        init();
        XMLAnalyzer analyzer = new XMLAnalyzer();
        analyzer.configure(xmlConfig);
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testAnalyzerDTDDisabled() throws Exception {
        init();
        XMLAnalyzer analyzer = new XMLAnalyzer();
        analyzer.configure(xmlConfig);
        String xmlString = "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE lolz [\n" +
                " <!ENTITY lol \"lol\">\n" +
                " <!ELEMENT lolz (#PCDATA)>\n" +
                " <!ENTITY lol1 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
                " <!ENTITY lol2 \"&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;\">\n" +
                " <!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n" +
                " <!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n" +
                " <!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n" +
                " <!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n" +
                " <!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n" +
                " <!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n" +
                " <!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n" +
                "]>\n" +
                "<lolz>&lol9;</lolz>";
        analyzer.analyze(xmlString, "/foo");
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testMaxEntityExpansionLimit() throws Exception {
        init();
        XMLAnalyzer analyzer = new XMLAnalyzer();
        xmlConfig.setEntityExpansionLimit(100);
        xmlConfig.setDtdEnabled(true);
        analyzer.configure(xmlConfig);
        String xmlString = "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE lolz [\n" +
                " <!ENTITY lol \"lol\">\n" +
                " <!ELEMENT lolz (#PCDATA)>\n" +
                " <!ENTITY lol1 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
                " <!ENTITY lol2 \"&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;\">\n" +
                " <!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n" +
                " <!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n" +
                " <!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n" +
                " <!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n" +
                " <!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n" +
                " <!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n" +
                " <!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n" +
                "]>\n" +
                "<lolz>&lol9;</lolz>";
        analyzer.analyze(xmlString, "/foo");
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testMaxAttributeLength() throws Exception {
        init();
        String xmlString = "<root attribute1111111111='someValue111111111' attribute2='1'></root>";
        XMLAnalyzer analyzer = new XMLAnalyzer();
        xmlConfig.setMaxAttributeLength(1);
        xmlConfig.setMaxAttributeCount(1);
        analyzer.configure(xmlConfig);
        analyzer.analyze(xmlString, "/foo");
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testMaxAttributeCount() throws Exception {
        init();
        String xmlString = "<a><root aaaaaaaaaa='aaaaaaa' b='b' c='c' d='d' e='e' f='f' g='g'></root></a>";
        XMLAnalyzer analyzer = new XMLAnalyzer();
        analyzer.configure(xmlConfig);
        analyzer.analyze(xmlString, "/foo");
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testMaxChildrenPerElement() throws Exception {
        init();
        xmlConfig.setMaxChildrenPerElement(2);
        XMLAnalyzer analyzer = new XMLAnalyzer();
        analyzer.configure(xmlConfig);

        String xmlString = "<root><c1></c1><c2></c2><c3></c3></root>";
        analyzer.analyze(xmlString, "/foo");
    }
}
