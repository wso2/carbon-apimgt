package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi;

public class AsyncApiParseOptions {

    private boolean defaultAsyncApiParserVersion = true;

    public AsyncApiParseOptions() {
    }

    public boolean getDefaultAsyncApiParserVersion() {
        return defaultAsyncApiParserVersion;
    }

    public void setDefaultAsyncApiParserVersion(boolean defaultAsyncApiParserVersion) {
        this.defaultAsyncApiParserVersion = defaultAsyncApiParserVersion;
    }
}
