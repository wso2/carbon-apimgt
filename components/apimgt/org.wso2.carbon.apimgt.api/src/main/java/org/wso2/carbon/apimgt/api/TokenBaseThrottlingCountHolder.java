package org.wso2.carbon.apimgt.api;

public class TokenBaseThrottlingCountHolder {

    private Long productionMaxPromptTokenCount = -1L;
    private Long productionMaxCompletionTokenCount = -1L;
    private Long productionMaxTotalTokenCount = -1L;
    private Long sandboxMaxPromptTokenCount = -1L;
    private Long sandboxMaxCompletionTokenCount = -1L;
    private Long sandboxMaxTotalTokenCount = -1L;
    private boolean isTokenBasedThrottlingEnabled = false;

    public TokenBaseThrottlingCountHolder() {

    }

    public TokenBaseThrottlingCountHolder(Long productionMaxPromptTokenCount, Long productionMaxCompletionTokenCount,
                                          Long productionMaxTotalTokenCount, Long sandboxMaxPromptTokenCount,
                                          Long sandboxMaxCompletionTokenCount, Long sandboxMaxTotalTokenCount,
                                          boolean isTokenBasedThrottlingEnabled) {
        this.productionMaxPromptTokenCount = productionMaxPromptTokenCount;
        this.productionMaxCompletionTokenCount = productionMaxCompletionTokenCount;
        this.productionMaxTotalTokenCount = productionMaxTotalTokenCount;
        this.sandboxMaxPromptTokenCount = sandboxMaxPromptTokenCount;
        this.sandboxMaxCompletionTokenCount = sandboxMaxCompletionTokenCount;
        this.sandboxMaxTotalTokenCount = sandboxMaxTotalTokenCount;
        this.isTokenBasedThrottlingEnabled = isTokenBasedThrottlingEnabled;
    }

    public Long getProductionMaxPromptTokenCount() {
        return productionMaxPromptTokenCount;
    }

    public void setProductionMaxPromptTokenCount(Long productionMaxPromptTokenCount) {
        this.productionMaxPromptTokenCount = productionMaxPromptTokenCount;
    }

    public Long getProductionMaxCompletionTokenCount() {
        return productionMaxCompletionTokenCount;
    }

    public void setProductionMaxCompletionTokenCount(Long productionMaxCompletionTokenCount) {
        this.productionMaxCompletionTokenCount = productionMaxCompletionTokenCount;
    }

    public Long getProductionMaxTotalTokenCount() {
        return productionMaxTotalTokenCount;
    }

    public void setProductionMaxTotalTokenCount(Long productionMaxTotalTokenCount) {
        this.productionMaxTotalTokenCount = productionMaxTotalTokenCount;
    }

    public Long getSandboxMaxPromptTokenCount() {
        return sandboxMaxPromptTokenCount;
    }

    public void setSandboxMaxPromptTokenCount(Long sandboxMaxPromptTokenCount) {
        this.sandboxMaxPromptTokenCount = sandboxMaxPromptTokenCount;
    }

    public Long getSandboxMaxCompletionTokenCount() {
        return sandboxMaxCompletionTokenCount;
    }

    public void setSandboxMaxCompletionTokenCount(Long sandboxMaxCompletionTokenCount) {
        this.sandboxMaxCompletionTokenCount = sandboxMaxCompletionTokenCount;
    }

    public Long getSandboxMaxTotalTokenCount() {
        return sandboxMaxTotalTokenCount;
    }

    public void setSandboxMaxTotalTokenCount(Long sandboxMaxTotalTokenCount) {
        this.sandboxMaxTotalTokenCount = sandboxMaxTotalTokenCount;
    }

    public boolean isTokenBasedThrottlingEnabled() {
        return isTokenBasedThrottlingEnabled;
    }

    public void setTokenBasedThrottlingEnabled(boolean tokenBasedThrottlingEnabled) {
        isTokenBasedThrottlingEnabled = tokenBasedThrottlingEnabled;
    }
}