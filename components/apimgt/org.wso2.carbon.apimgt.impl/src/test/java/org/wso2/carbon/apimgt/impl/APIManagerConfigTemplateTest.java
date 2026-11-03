/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.ElseIfTag;
import com.hubspot.jinjava.lib.tag.ElseTag;
import com.hubspot.jinjava.lib.tag.ForTag;
import com.hubspot.jinjava.lib.tag.IfTag;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Checks that the api-manager.xml.j2 template renders well-formed XML for every if/elif/else branch. Most branches are
 * rendered only when a specific deployment.toml config is set, so a malformed tag inside one of them (e.g. a wrong
 * closing tag) is not visible with the default configs but breaks server startup once that config is used. The
 * template is rendered with Jinjava, the engine used by the config mapper, while the branch taken by each if block is
 * selected in turn.
 */
public class APIManagerConfigTemplateTest {

    private static final String TEMPLATE_PATH_PROPERTY = "apim.config.template";

    @Test
    public void testAllBranchesRenderWellFormedXML() throws Exception {

        String template = new String(Files.readAllBytes(Paths.get(System.getProperty(TEMPLATE_PATH_PROPERTY))),
                StandardCharsets.UTF_8);
        BranchSelectingIfTag ifTag = new BranchSelectingIfTag();
        Jinjava jinjava = new Jinjava();
        jinjava.getGlobalContext().registerTag(ifTag);
        jinjava.getGlobalContext().registerTag(new AtLeastOnceForTag());
        DocumentBuilder documentBuilder = newDocumentBuilder();

        Map<Map<String, Integer>, String> branchSelections = new LinkedHashMap<>();
        collectBranchSelections(jinjava.newInterpreter().parse(template), Collections.<String, Integer>emptyMap(),
                branchSelections);
        Set<String> failures = new LinkedHashSet<>();
        for (Map.Entry<Map<String, Integer>, String> branchSelection : branchSelections.entrySet()) {
            ifTag.selectedBranches = branchSelection.getKey();
            String rendered = jinjava.render(template, new HashMap<String, Object>());
            try {
                documentBuilder.parse(new InputSource(new StringReader(rendered)));
            } catch (SAXParseException e) {
                failures.add(branchSelection.getValue() + ": " + e.getMessage() + " Rendered line "
                        + e.getLineNumber() + ": " + rendered.split("\n", -1)[e.getLineNumber() - 1].trim());
            }
        }
        Assert.assertTrue("Malformed XML rendered by api-manager.xml.j2 with:\n" + String.join("\n", failures),
                failures.isEmpty());
    }

    private DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        // Fail on parse errors without printing them to the console
        documentBuilder.setErrorHandler(new DefaultHandler());
        return documentBuilder;
    }

    /**
     * Records, for every branch of every if block, the branch selection that renders it: the branch itself along with
     * the enclosing branches it is nested in.
     */
    private void collectBranchSelections(Node node, Map<String, Integer> enclosingBranches,
                                         Map<Map<String, Integer>, String> branchSelections) {

        if (!(node instanceof TagNode) || !IfTag.TAG_NAME.equals(((TagNode) node).getName())) {
            for (Node child : node.getChildren()) {
                collectBranchSelections(child, enclosingBranches, branchSelections);
            }
            return;
        }
        TagNode ifNode = (TagNode) node;
        String ifBlock = ifBlockKey(ifNode);
        // An if block without an else can also render nothing, which is counted as an additional branch
        int branchCount = 2 + (int) ifNode.getChildren().stream().filter(child -> isBranch(child, ElseIfTag.TAG_NAME))
                .count();
        for (int branch = 0; branch < branchCount; branch++) {
            Map<String, Integer> branchSelection = new HashMap<>(enclosingBranches);
            branchSelection.put(ifBlock, branch);
            branchSelections.putIfAbsent(branchSelection, describe(ifNode, branch));
        }
        int branch = 0;
        for (Node child : ifNode.getChildren()) {
            if (isBranch(child, ElseIfTag.TAG_NAME) || isBranch(child, ElseTag.TAG_NAME)) {
                branch++;
                continue;
            }
            Map<String, Integer> branchSelection = new HashMap<>(enclosingBranches);
            branchSelection.put(ifBlock, branch);
            collectBranchSelections(child, branchSelection, branchSelections);
        }
    }

    private String describe(TagNode ifNode, int branch) {

        String branchName = "no branch";
        if (branch == 0) {
            branchName = "if branch";
        } else {
            TagNode branchNode = branchNode(ifNode, branch);
            if (branchNode != null) {
                branchName = branchNode.reconstructImage();
            }
        }
        return ("{% if " + ifNode.getHelpers().trim() + " %} at line " + ifNode.getLineNumber() + " -> " + branchName)
                .replaceAll("\\s+", " ");
    }

    private static TagNode branchNode(TagNode ifNode, int branch) {

        int index = 0;
        for (Node child : ifNode.getChildren()) {
            if ((isBranch(child, ElseIfTag.TAG_NAME) || isBranch(child, ElseTag.TAG_NAME)) && ++index == branch) {
                return (TagNode) child;
            }
        }
        return null;
    }

    private static boolean isBranch(Node node, String tagName) {

        return node instanceof TagNode && tagName.equals(((TagNode) node).getName());
    }

    /**
     * Identifies an if block by its conditions, so that blocks with the same conditions take the same branch. This
     * keeps templates that open an element in one block and close it in another with the same condition valid.
     */
    private static String ifBlockKey(TagNode ifNode) {

        StringBuilder key = new StringBuilder(ifNode.getHelpers().trim());
        for (Node child : ifNode.getChildren()) {
            if (isBranch(child, ElseIfTag.TAG_NAME) || isBranch(child, ElseTag.TAG_NAME)) {
                key.append(" | ").append(((TagNode) child).reconstructImage());
            }
        }
        return key.toString();
    }

    /**
     * If tag that takes the selected branch of the if blocks in the current branch selection and evaluates the
     * conditions of all other if blocks as usual.
     */
    private static class BranchSelectingIfTag extends IfTag {

        private Map<String, Integer> selectedBranches = Collections.emptyMap();

        @Override
        public boolean isPositiveIfElseNode(TagNode branchNode, JinjavaInterpreter interpreter) {

            TagNode ifNode = TAG_NAME.equals(branchNode.getName()) ? branchNode : (TagNode) branchNode.getParent();
            Integer selectedBranch = selectedBranches.get(ifBlockKey(ifNode));
            if (selectedBranch == null) {
                return super.isPositiveIfElseNode(branchNode, interpreter);
            }
            return selectedBranch == 0 ? branchNode == ifNode : branchNode == branchNode(ifNode, selectedBranch);
        }
    }

    /**
     * For tag that renders loops over undefined or empty collections once, so that the if blocks inside are rendered.
     */
    private static class AtLeastOnceForTag extends ForTag {

        @Override
        public String renderForCollection(TagNode tagNode, JinjavaInterpreter interpreter, List<String> loopVars,
                                          Object collection) {

            if (collection == null || collection instanceof Collection && ((Collection<?>) collection).isEmpty()
                    || collection instanceof Map && ((Map<?, ?>) collection).isEmpty()) {
                Object item = loopVars.size() > 1 ? new Placeholder("item").items().get(0) : new Placeholder("item");
                collection = Collections.singletonList(item);
            }
            return super.renderForCollection(tagNode, interpreter, loopVars, collection);
        }
    }

    /**
     * Stands in for a configured loop item. Its attributes are placeholders too and each one renders as its own name,
     * so templates that use an attribute as an element name (e.g. <{{property.name}}>) render a valid element. Being a
     * PyishSerializable, Jinjava uses it as is instead of wrapping it as a dict, and renders it with toString().
     */
    private static class Placeholder extends HashMap<String, Object> implements PyishSerializable {

        private final String name;

        private Placeholder(String name) {

            this.name = name;
        }

        @Override
        public boolean containsKey(Object key) {

            return true;
        }

        @Override
        public Object get(Object key) {

            return new Placeholder(String.valueOf(key));
        }

        /**
         * Supports {% for key, value in placeholder.items() %} loops.
         */
        public List<Map.Entry<String, Object>> items() {

            return Collections.singletonList(new AbstractMap.SimpleEntry<>("key", new Placeholder("value")));
        }

        /**
         * Supports list configs converted with toList(), e.g. {{ urls.toList() | join(',') }}.
         */
        public List<String> toList() {

            return Collections.singletonList(name);
        }

        @Override
        public String toString() {

            return name;
        }
    }
}
