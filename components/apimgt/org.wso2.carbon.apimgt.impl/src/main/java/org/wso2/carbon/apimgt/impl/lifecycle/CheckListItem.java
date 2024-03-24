/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.lifecycle;
public class CheckListItem implements Comparable {
    private String lifeCycleStatus;
    private String name;
    private String value;
    private String order;
    private String propertyName;
    private String isVisible;
    private static final Object HASH_CODE_OBJECT = new Object();

    public String getVisible() {
        return this.isVisible;
    }

    public void setVisible(String visible) {
        this.isVisible = visible;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getLifeCycleStatus() {
        return this.lifeCycleStatus;
    }

    public void setLifeCycleStatus(String lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOrder() {
        return this.order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public CheckListItem(String lifeCycleStatus, String name, String value, String order) {
        this.lifeCycleStatus = lifeCycleStatus;
        this.name = name;
        this.value = value;
        this.order = order;
    }

    public CheckListItem() {
    }

    public boolean matchLifeCycleStatus(String status, boolean ignoreCase) {
        if (this.lifeCycleStatus != null && status != null) {
            return ignoreCase ? this.lifeCycleStatus.equalsIgnoreCase(status) : this.lifeCycleStatus.equals(status);
        } else {
            return false;
        }
    }

    public boolean matchLifeCycleStatus(String status) {
        return this.matchLifeCycleStatus(status, true);
    }

    public int hashCode() {
        int hashCode = HASH_CODE_OBJECT.hashCode();
        if (this.order != null) {
            hashCode &= this.order.hashCode();
        }

        if (this.name != null) {
            hashCode &= this.name.hashCode();
        }

        if (this.value != null) {
            hashCode &= this.value.hashCode();
        }

        if (this.lifeCycleStatus != null) {
            hashCode &= this.lifeCycleStatus.hashCode();
        }

        if (this.propertyName != null) {
            hashCode &= this.propertyName.hashCode();
        }

        return hashCode;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof CheckListItem)) {
            return false;
        } else {
            CheckListItem item = (CheckListItem)obj;
            return (this.order != null && this.order.equals(item.order) || this.order == null && item.order == null) && (this.lifeCycleStatus != null && this.lifeCycleStatus.equals(item.lifeCycleStatus) || this.lifeCycleStatus == null && item.lifeCycleStatus == null) && (this.name != null && this.name.equals(item.name) || this.name == null && item.name == null) && (this.value != null && this.value.equals(item.value) || this.value == null && item.value == null) && (this.propertyName != null && this.propertyName.equals(item.propertyName) || this.propertyName == null && item.propertyName == null);
        }
    }

    public int compareTo(Object anotherItem) {
        if (this.equals(anotherItem)) {
            return 0;
        } else {
            CheckListItem item = (CheckListItem)anotherItem;
            int otherItemOrder = Integer.parseInt(item.getOrder());
            int itemOrder = Integer.parseInt(this.order);
            return itemOrder - otherItemOrder;
        }
    }
}
