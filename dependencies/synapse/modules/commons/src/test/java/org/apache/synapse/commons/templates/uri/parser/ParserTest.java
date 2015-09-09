/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.synapse.commons.templates.uri.parser;

import junit.framework.TestCase;
import org.apache.synapse.commons.templates.uri.URITemplate;
import java.util.HashMap;
import java.util.Map;

public class ParserTest extends TestCase {

    Map<String, String> variables = new HashMap<String, String>();

    @Override
    protected void setUp() throws Exception {
        variables.put("dom", "example.com");
        variables.put("dub", "me/too");
        variables.put("hello", "Hello World!");
        variables.put("half", "50%");
        variables.put("var", "value");
        variables.put("who", "fred");
        variables.put("base", "http://example.com/home/");
        variables.put("path", "/foo/bar");
        variables.put("v", "6");
        variables.put("x", "1024");
        variables.put("y", "768");
        variables.put("empty", "");
    }

    public void testSimpleStringExpansion() throws Exception {
        assertEquals("value", new SimpleStringExpression("var").expand(variables));
        assertEquals("Hello%20World%21", new SimpleStringExpression("hello").expand(variables));
        assertEquals("50%25", new SimpleStringExpression("half").expand(variables));
        assertEquals("", new SimpleStringExpression("empty").expand(variables));
        assertNull(new SimpleStringExpression("undef").expand(variables));
        assertEquals("1024,768", new SimpleStringExpression("x,y").expand(variables));
        assertEquals("1024,Hello%20World%21,768", new SimpleStringExpression("x,hello,y").expand(variables));
        assertEquals("1024,", new SimpleStringExpression("x,empty").expand(variables));
        assertEquals("1024", new SimpleStringExpression("x,undef").expand(variables));
        assertEquals("768", new SimpleStringExpression("undef,y").expand(variables));
        assertEquals("768", new SimpleStringExpression("undef,y").expand(variables));
        assertEquals("val", new SimpleStringExpression("var:3").expand(variables));
        assertEquals("value", new SimpleStringExpression("var:30").expand(variables));
        assertEquals("http%3A%2F%2Fexample.com%2Fhome%2F", new SimpleStringExpression("base").expand(variables));
    }

    public void testSimpleStringMatch() throws Exception {
        URITemplate template = new URITemplate("/admin/~{user}");
        Map<String, String> var = new HashMap<String, String>();
        assertTrue(template.matches("/admin/~hiranya", var));
        assertEquals("hiranya", var.get("user"));
        assertFalse(template.matches("/admi/~hiranya", var));
        assertFalse(template.matches("/admin/hiranya", var));
        assertFalse(template.matches("/admin/~hiranya/foo", var));
        assertFalse(template.matches("/admin/~hirany.a", var));
        var.clear();

        template = new URITemplate("/dictionary/{char:1}/{word}");
        assertTrue(template.matches("/dictionary/c/cat", var));
        assertEquals("c", var.get("char"));
        assertEquals("cat", var.get("word"));
        assertFalse(template.matches("/dictionry/c/cat", var));
        assertFalse(template.matches("/dictionary/c", var));
        assertFalse(template.matches("/dictionary/co/cat", var));
        var.clear();

        assertTrue(template.matches("/dictionary/h/hello%20world", var));
        assertEquals("h", var.get("char"));
        assertEquals("hello world", var.get("word"));
        var.clear();

        template = new URITemplate("/dictionary/{char}/{+word}");
        assertTrue(template.matches("/dictionary/h/hello+world", var));
        assertEquals("h", var.get("char"));
        assertEquals("hello+world", var.get("word"));
        var.clear();

        assertTrue(template.matches("/dictionary/h/hello world", var));
        assertEquals("h", var.get("char"));
        assertEquals("hello world", var.get("word"));
        var.clear();

        assertTrue(template.matches("/dictionary/h/hello%2Bworld", var));
        assertEquals("h", var.get("char"));
        assertEquals("hello+world", var.get("word"));
        var.clear();

        template = new URITemplate("/dictionary/{char}/{word,count}");
        assertTrue(template.matches("/dictionary/c/cat,5", var));
        assertEquals("c", var.get("char"));
        assertEquals("cat", var.get("word"));
        assertEquals("5", var.get("count"));
        var.clear();

        assertTrue(template.matches("/dictionary/c/cat", var));
        assertEquals("c", var.get("char"));
        assertEquals("cat", var.get("word"));
        assertEquals("", var.get("count"));
        var.clear();

        assertTrue(template.matches("/dictionary/c/,5", var));
        assertEquals("c", var.get("char"));
        assertEquals("", var.get("word"));
        assertEquals("5", var.get("count"));
        var.clear();

        template = new URITemplate("/dictionary/{char,word}/{count}");
        assertTrue(template.matches("/dictionary/c,cat/5", var));
        assertEquals("c", var.get("char"));
        assertEquals("cat", var.get("word"));
        assertEquals("5", var.get("count"));
        var.clear();

        assertTrue(template.matches("/dictionary/c/5", var));
        assertEquals("c", var.get("char"));
        assertEquals("", var.get("word"));
        assertEquals("5", var.get("count"));
        assertFalse(template.matches("/dictionary/c,ca,cat/5", var));
        var.clear();

        template = new URITemplate("/dictionary/{user}/test?a={user}");
        assertTrue(template.matches("/dictionary/hiranya/test?a=hiranya", var));
        assertEquals("hiranya", var.get("user"));
        assertFalse(template.matches("/dictionary/hiranya/test?a=foo", var));
        var.clear();

        template = new URITemplate("/dictionary/foo-{user}-bar");
        assertTrue(template.matches("/dictionary/foo-hiranya-bar", var));
        assertEquals("hiranya", var.get("user"));
        var.clear();
        assertTrue(template.matches("/dictionary/foo--bar", var));
        assertFalse(template.matches("/dictionary/foo-bar", var));

        template = new URITemplate("/alert/{id}.json");
        assertTrue(template.matches("/alert/foo.json", var));
        assertEquals("foo", var.get("id"));
        var.clear();

        template = new URITemplate("/");
        assertTrue(template.matches("/", var));

        template = new URITemplate("/*");
        assertTrue(template.matches("/sanjeewa?test=done", var));
        assertTrue(template.matches("/sanjeewa", var));
        assertTrue(template.matches("/", var));

        template = new URITemplate("/sanjeewa/*");
        assertTrue(template.matches("/sanjeewa/admin?test=done", var));
        assertTrue(template.matches("/sanjeewa/test", var));

        template = new URITemplate("/sanjeewa*");
        assertTrue(template.matches("/sanjeewa/admin?test=done", var));
        assertTrue(template.matches("/sanjeewa/test", var));
        assertTrue(template.matches("/sanjeewa/", var));
        assertTrue(template.matches("/sanjeewa", var));

        template = new URITemplate("/{sanjeewa}/*");
        assertTrue(template.matches("/sanjeewa/admin?test=done", var));
        assertTrue(template.matches("/sanjeewa/?test=done", var));
        assertTrue(template.matches("/sanjeewa/test", var));
        assertTrue(template.matches("/sanjeewa/", var));

        template = new URITemplate("/dictionary/{char}/{word}");
        assertTrue(template.matches("/dictionary/d/dog/", var));
        assertTrue(template.matches("/dictionary/d/dog", var));

        template = new URITemplate("/test{format}*");
        assertTrue(template.matches("/test.json?test", var));
        assertTrue(template.matches("/test.json/", var));
        assertTrue(template.matches("/test.json", var));


        template = new URITemplate("/test{format}/*");
        assertTrue(template.matches("/test.json/test", var));
        assertTrue(template.matches("/test.json/", var));
        assertTrue(template.matches("/test.json", var));


        template = new URITemplate("/sanjeewa/~{test}?*");
        var.put("test", "tester");
        assertTrue(template.matches("/sanjeewa/~tester?test", var));
        var.clear();

        template = new URITemplate("/sanjeewa/{name,id}/*");
        var.put("name", "user");
        var.put("id", "190");
        // matching resource urls
        assertTrue(template.matches("/sanjeewa/user,190/test", var));
        assertTrue(template.matches("/sanjeewa/user,190/test?year=2012", var));
        var.clear();

        template = new URITemplate("/{name,id}/*");
        var.put("name", "user");
        var.put("id", "190");
        // matching resource urls
        assertTrue(template.matches("/user,190/test", var));
        assertTrue(template.matches("/user,190/test?year=2012", var));
        // un matching resource urls
        assertFalse(template.matches("/sanjeewa/user,190", var));
        assertFalse(template.matches("/sanjeewa/user,190,11/test", var));
        assertFalse(template.matches("/sanjeewa/user/test", var));
        assertFalse(template.matches("/rangana/user,190/", var));
        assertFalse(template.matches("/sanjeewa/test", var));
        var.clear();

    }

    public void testReservedStringExpansion() throws Exception {
        assertEquals("value", new ReservedStringExpression("var").expand(variables));
        assertEquals("Hello%20World!", new ReservedStringExpression("hello").expand(variables));
        assertEquals("50%25", new ReservedStringExpression("half").expand(variables));
        assertEquals("http://example.com/home/", new ReservedStringExpression("base").expand(variables));
        assertEquals("", new ReservedStringExpression("empty").expand(variables));
        assertNull(new ReservedStringExpression("undef").expand(variables));
        assertEquals("/foo/bar", new ReservedStringExpression("path").expand(variables));
        assertEquals("1024,Hello%20World!,768", new ReservedStringExpression("x,hello,y").expand(variables));
        assertEquals("/foo/bar,1024", new ReservedStringExpression("path,x").expand(variables));
        assertEquals("/foo/b", new ReservedStringExpression("path:6").expand(variables));
    }

    public void testReservedStringMatch() throws Exception {
        URITemplate template = new URITemplate("/admin/~{+user}");
        Map<String, String> var = new HashMap<String, String>();
        assertTrue(template.matches("/admin/~foo!bar", var));
        assertEquals("foo!bar", var.get("user"));
        assertFalse(template.matches("/admi/~hiranya", var));
        assertFalse(template.matches("/admin/hiranya", var));
        assertFalse(template.matches("/admin/~hiranya/foo", var));
        var.clear();

        template = new URITemplate("/words?{+query}");
        assertTrue(template.matches("/words?a=5", var));
        assertEquals("a=5", var.get("query"));
        var.clear();

        template = new URITemplate("/{symbol}/feed.rss{+queryStr}");
        assertTrue(template.matches("/APPLE/feed.rss?max=30", var));
        assertEquals("?max=30", var.get("queryStr"));
        var.clear();
        assertFalse(template.matches("/APPLE?max=30", var));
    }

    public void testFragmentExpansion() throws Exception {
        assertEquals("#value", new FragmentExpression("var").expand(variables));
        assertEquals("#Hello%20World!", new FragmentExpression("hello").expand(variables));
        assertEquals("#50%25", new FragmentExpression("half").expand(variables));
        assertEquals("#", new FragmentExpression("empty").expand(variables));
        assertNull(new FragmentExpression("undef").expand(variables));
        assertEquals("#1024,Hello%20World!,768", new FragmentExpression("x,hello,y").expand(variables));
        assertEquals("#/foo/bar,1024", new FragmentExpression("path,x").expand(variables));
        assertEquals("#/foo/b", new FragmentExpression("path:6").expand(variables));
    }

    public void testFragmentMatch() throws Exception {
        URITemplate template = new URITemplate("/admin{#foo}");
        Map<String, String> var = new HashMap<String, String>();
        assertTrue(template.matches("/admin#test", var));
        assertEquals("test", var.get("foo"));
        var.clear();

        assertFalse(template.matches("/admin/test", var));
        assertTrue(template.matches("/admin#test,value", var));
        assertEquals("test,value", var.get("foo"));
    }

    public void testLabelExpansion() throws Exception {
        assertEquals(".fred", new LabelExpression("who").expand(variables));
        assertEquals(".fred.fred", new LabelExpression("who,who").expand(variables));
        assertEquals(".50%25.fred", new LabelExpression("half,who").expand(variables));
        assertEquals(".example.com", new LabelExpression("dom").expand(variables));
        assertEquals(".", new LabelExpression("empty").expand(variables));
        assertNull(new LabelExpression("undef").expand(variables));
        assertEquals(".val", new LabelExpression("var:3").expand(variables));
    }

    public void testLabelMatch() throws Exception {
        URITemplate template = new URITemplate("/admin{.action}");
        Map<String, String> var = new HashMap<String, String>();
        assertTrue(template.matches("/admin.do", var));
        assertEquals("do", var.get("action"));
        assertFalse(template.matches("/admin.do.bad", var));
        var.clear();

        template = new URITemplate("/admin{.action,sub}");
        assertTrue(template.matches("/admin.do.view", var));
        assertEquals("do", var.get("action"));
        assertEquals("view", var.get("sub"));
    }

    public void testPathSegmentExpansion() throws Exception {
        assertEquals("/fred", new PathSegmentExpression("who").expand(variables));
        assertEquals("/fred/fred", new PathSegmentExpression("who,who").expand(variables));
        assertEquals("/50%25/fred", new PathSegmentExpression("half,who").expand(variables));
        assertEquals("/fred/me%2Ftoo", new PathSegmentExpression("who,dub").expand(variables));
        assertEquals("/value/", new PathSegmentExpression("var,empty").expand(variables));
        assertEquals("/value", new PathSegmentExpression("var,undef").expand(variables));
        assertEquals("/value/1024", new PathSegmentExpression("var,x").expand(variables));
        assertEquals("/v/value", new PathSegmentExpression("var:1,var").expand(variables));
    }

    public void testPathSegmentMatch() throws Exception {
        URITemplate template = new URITemplate("/admin{/context}");
        Map<String, String> var = new HashMap<String, String>();
        assertTrue(template.matches("/admin/foo", var));
        assertEquals("foo", var.get("context"));
        assertFalse(template.matches("/admin.do.bad", var));
        var.clear();

        template = new URITemplate("/admin{/action,sub}");
        assertTrue(template.matches("/admin/do/view", var));
        assertEquals("do", var.get("action"));
        assertEquals("view", var.get("sub"));
    }
}
