/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

/**
 * This class represents metadata related to a Large Language Model (LLM).
 */
public class LlmMetadata {

    private int totalTokens;
    private int promptTokens;
    private int completionTokens;
    private String model;

    /**
     * Constructs a new {@code LlmMetadata} with the specified model, total tokens,
     * prompt tokens, and completion tokens.
     *
     * @param model            the model name
     * @param totalTokens      the total number of tokens
     * @param promptTokens     the number of tokens in the prompt
     * @param completionTokens the number of tokens in the completion
     */
    public LlmMetadata(String model, int totalTokens, int promptTokens, int completionTokens) {
        this.model = model;
        this.totalTokens = totalTokens;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
    }

    /**
     * Constructs a new {@code LlmMetadata} with default values.
     */
    public LlmMetadata() {
        // Default constructor
    }

    /**
     * Gets the total number of tokens.
     *
     * @return the total number of tokens
     */
    public int getTotalTokens() {
        return totalTokens;
    }

    /**
     * Gets the number of tokens in the prompt.
     *
     * @return the number of prompt tokens
     */
    public int getPromptTokens() {
        return promptTokens;
    }

    /**
     * Gets the number of tokens in the completion.
     *
     * @return the number of completion tokens
     */
    public int getCompletionTokens() {
        return completionTokens;
    }

    /**
     * Sets the total number of tokens.
     *
     * @param totalTokens the total number of tokens to set
     */
    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }

    /**
     * Sets the number of tokens in the prompt.
     *
     * @param promptTokens the number of prompt tokens to set
     */
    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
    }

    /**
     * Sets the number of tokens in the completion.
     *
     * @param completionTokens the number of completion tokens to set
     */
    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
    }

    /**
     * Gets the model name.
     *
     * @return the model name as a {@link String}
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model name.
     *
     * @param model the model name to set
     */
    public void setModel(String model) {
        this.model = model;
    }
}
