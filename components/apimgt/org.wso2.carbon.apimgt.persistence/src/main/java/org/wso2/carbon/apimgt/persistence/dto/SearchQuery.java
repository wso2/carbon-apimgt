package org.wso2.carbon.apimgt.persistence.dto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SearchQuery {
    private static final Log log = LogFactory.getLog(SearchQuery.class);
    private String content;
    private String type;

    public SearchQuery(String content, String type) {
        log.debug("Creating SearchQuery with content: " + content + " and type: " + type);
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
