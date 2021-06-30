/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

public class Pagination {
    private Integer offset = null;
    private Integer limit = null;
    private Integer total = null;
    private String next = null;
    private String previous = null;

    public Integer getOffset() {return offset; }

    public void setOffset(Integer offset) {this.offset = offset; }

    public Integer getLimit() {return limit; }

    public void setLimit(Integer limit) {this.limit = limit; }

    public Integer getTotal() {return total; }

    public void setTotal(Integer total) {this.total = total; }

    public String getNext() {return next; }

    public void setNext(String next) {this.next = next; }

    public String getPrevious() {return previous; }

    public void setPrevious(String previous) {this.previous = previous; }

}
