/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.hybrid.gateway.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration DTO
 */
public class ConfigDTO {

    private boolean advanced_throttling_query_param_conditions_enabled;

    private boolean advanced_throttling_jwt_claim_conditions_enabled;

    private String url_gateway;

    private String url_initialization_api;

    private boolean multi_tenant_enabled;

    private String usage_upload_task_cron;

    private String url_usage_upload_service;

    private String throttling_synchronization_task_cron;

    private boolean api_update_task_enabled;

    private ArrayList meta_info_metadata;

    private List meta_info_custom;

    private ArrayList meta_info_labels;

    private String gov_registry_path;

    private String url_management_console;

    private String api_update_rest_api_version;

    private boolean throttling_synchronization_task_enabled;

    private boolean usage_upload_task_enabled;

    private boolean usage_upload_cleanup_task_enabled;

    private String status_unique_identifier;

    private boolean public_cloud_is_public_cloud;

    private String url_publisher;

    private long usage_upload_retention_days;

    private boolean usage_upload_publish_task_enabled;

    private String api_update_task_cron;

    private String status_ping_api_url;

    private boolean advanced_throttling_header_conditions_enabled;

    private ArrayList multi_tenant_users;

    private String usage_upload_cleanup_task_cron;

    private String url_api_information_service;

    private String url_key_manager;

    private String url_admin;

    private int api_update_api_info_retrieval_duration;

    private int usage_upload_publish_frequency;

    private boolean optional_analytics_enabled;

    private String optional_key_validation_client_type;

    private String usage_upload_data_publisher_class;

    private String usage_upload_file_data_upload_task_class;

    private String usage_upload_cleanup_task_class;

    private String throttling_synchronization_task_class;

    private String api_update_task_class;

    private String email;
    private String tenant_domain;
    private String password;
    private String username;

    public String getUsage_upload_data_publisher_class() {

        return usage_upload_data_publisher_class;
    }

    public void setUsage_upload_data_publisher_class(String usage_upload_data_publisher_class) {

        this.usage_upload_data_publisher_class = usage_upload_data_publisher_class;
    }

    public String getOptional_key_validation_client_type() {

        return optional_key_validation_client_type;
    }

    public void setOptional_key_validation_client_type(String optional_key_validation_client_type) {

        this.optional_key_validation_client_type = optional_key_validation_client_type;
    }

    public Boolean isOptional_analytics_enabled() {

        return optional_analytics_enabled;
    }

    public void setOptional_analytics_enabled(boolean optional_analytics_enabled) {

        this.optional_analytics_enabled = optional_analytics_enabled;
    }

    public int getUsage_upload_max_usage_file_size() {

        return usage_upload_max_usage_file_size;
    }

    public void setUsage_upload_max_usage_file_size(int usage_upload_max_usage_file_size) {

        this.usage_upload_max_usage_file_size = usage_upload_max_usage_file_size;
    }

    private int usage_upload_max_usage_file_size;

    public boolean isAdvanced_throttling_query_param_conditions_enabled() {

        return advanced_throttling_query_param_conditions_enabled;
    }

    public void setAdvanced_throttling_query_param_conditions_enabled(
            boolean advanced_throttling_query_param_conditions_enabled) {

        this.advanced_throttling_query_param_conditions_enabled = advanced_throttling_query_param_conditions_enabled;
    }

    public boolean isAdvanced_throttling_jwt_claim_conditions_enabled() {

        return advanced_throttling_jwt_claim_conditions_enabled;
    }

    public void setAdvanced_throttling_jwt_claim_conditions_enabled(
            boolean advanced_throttling_jwt_claim_conditions_enabled) {

        this.advanced_throttling_jwt_claim_conditions_enabled = advanced_throttling_jwt_claim_conditions_enabled;
    }

    public String getUrl_gateway() {

        return url_gateway;
    }

    public void setUrl_gateway(String url_gateway) {

        this.url_gateway = url_gateway;
    }

    public String getUrl_initialization_api() {

        return url_initialization_api;
    }

    public void setUrl_initialization_api(String url_initialization_api) {

        this.url_initialization_api = url_initialization_api;
    }

    public boolean isMulti_tenant_enabled() {

        return multi_tenant_enabled;
    }

    public void setMulti_tenant_enabled(boolean multi_tenant_enabled) {

        this.multi_tenant_enabled = multi_tenant_enabled;
    }

    public String getUsage_upload_task_cron() {

        return usage_upload_task_cron;
    }

    public void setUsage_upload_task_cron(String usage_upload_task_cron) {

        this.usage_upload_task_cron = usage_upload_task_cron;
    }

    public String getUrl_usage_upload_service() {

        return url_usage_upload_service;
    }

    public void setUrl_usage_upload_service(String url_usage_upload_service) {

        this.url_usage_upload_service = url_usage_upload_service;
    }

    public String getThrottling_synchronization_task_cron() {

        return throttling_synchronization_task_cron;
    }

    public void setThrottling_synchronization_task_cron(String throttling_synchronization_task_cron) {

        this.throttling_synchronization_task_cron = throttling_synchronization_task_cron;
    }

    public boolean isApi_update_task_enabled() {

        return api_update_task_enabled;
    }

    public void setApi_update_task_enabled(boolean api_update_task_enabled) {

        this.api_update_task_enabled = api_update_task_enabled;
    }

    public ArrayList getMeta_info_metadata() {

        return meta_info_metadata;
    }

    public void setMeta_info_metadata(ArrayList meta_info_metadata) {

        this.meta_info_metadata = meta_info_metadata;
    }

    public String getGov_registry_path() {

        return gov_registry_path;
    }

    public void setGov_registry_path(String gov_registry_path) {

        this.gov_registry_path = gov_registry_path;
    }

    public String getUrl_management_console() {

        return url_management_console;
    }

    public void setUrl_management_console(String url_management_console) {

        this.url_management_console = url_management_console;
    }

    public String getApi_update_rest_api_version() {

        return api_update_rest_api_version;
    }

    public void setApi_update_rest_api_version(String api_update_rest_api_version) {

        this.api_update_rest_api_version = api_update_rest_api_version;
    }

    public boolean isThrottling_synchronization_task_enabled() {

        return throttling_synchronization_task_enabled;
    }

    public void setThrottling_synchronization_task_enabled(boolean throttling_synchronization_task_enabled) {

        this.throttling_synchronization_task_enabled = throttling_synchronization_task_enabled;
    }

    public boolean isUsage_upload_task_enabled() {

        return usage_upload_task_enabled;
    }

    public void setUsage_upload_task_enabled(boolean usage_upload_task_enabled) {

        this.usage_upload_task_enabled = usage_upload_task_enabled;
    }

    public boolean isUsage_upload_cleanup_task_enabled() {

        return usage_upload_cleanup_task_enabled;
    }

    public void setUsage_upload_cleanup_task_enabled(boolean usage_upload_cleanup_task_enabled) {

        this.usage_upload_cleanup_task_enabled = usage_upload_cleanup_task_enabled;
    }

    public String getStatus_unique_identifier() {

        return status_unique_identifier;
    }

    public void setStatus_unique_identifier(String status_unique_identifier) {

        this.status_unique_identifier = status_unique_identifier;
    }

    public boolean isPublic_cloud_is_public_cloud() {

        return public_cloud_is_public_cloud;
    }

    public void setPublic_cloud_is_public_cloud(boolean public_cloud_is_public_cloud) {

        this.public_cloud_is_public_cloud = public_cloud_is_public_cloud;
    }

    public String getUrl_publisher() {

        return url_publisher;
    }

    public void setUrl_publisher(String url_publisher) {

        this.url_publisher = url_publisher;
    }

    public long getUsage_upload_retention_days() {

        return usage_upload_retention_days;
    }

    public void setUsage_upload_retention_days(long usage_upload_retention_days) {

        this.usage_upload_retention_days = usage_upload_retention_days;
    }

    public String getApi_update_task_cron() {

        return api_update_task_cron;
    }

    public void setApi_update_task_cron(String api_update_task_cron) {

        this.api_update_task_cron = api_update_task_cron;
    }

    public String getStatus_ping_api_url() {

        return status_ping_api_url;
    }

    public void setStatus_ping_api_url(String status_ping_api_url) {

        this.status_ping_api_url = status_ping_api_url;
    }

    public boolean isAdvanced_throttling_header_conditions_enabled() {

        return advanced_throttling_header_conditions_enabled;
    }

    public void setAdvanced_throttling_header_conditions_enabled(
            boolean advanced_throttling_header_conditions_enabled) {

        this.advanced_throttling_header_conditions_enabled = advanced_throttling_header_conditions_enabled;
    }

    public ArrayList getMulti_tenant_users() {

        return multi_tenant_users;
    }

    public void setMulti_tenant_users(ArrayList multi_tenant_users) {

        this.multi_tenant_users = multi_tenant_users;
    }

    public String getUsage_upload_cleanup_task_cron() {

        return usage_upload_cleanup_task_cron;
    }

    public void setUsage_upload_cleanup_task_cron(String usage_upload_cleanup_task_cron) {

        this.usage_upload_cleanup_task_cron = usage_upload_cleanup_task_cron;
    }

    public String getUrl_api_information_service() {

        return url_api_information_service;
    }

    public void setUrl_api_information_service(String url_api_information_service) {

        this.url_api_information_service = url_api_information_service;
    }

    public String getUrl_key_manager() {

        return url_key_manager;
    }

    public void setUrl_key_manager(String url_key_manager) {

        this.url_key_manager = url_key_manager;
    }

    public String getUrl_admin() {

        return url_admin;
    }

    public void setUrl_admin(String url_admin) {

        this.url_admin = url_admin;
    }

    public int getApi_update_api_info_retrieval_duration() {

        return api_update_api_info_retrieval_duration;
    }

    public void setApi_update_api_info_retrieval_duration(int api_update_api_info_retrieval_duration) {

        this.api_update_api_info_retrieval_duration = api_update_api_info_retrieval_duration;
    }

    public ArrayList getMeta_info_labels() {

        return meta_info_labels;
    }

    public void setMeta_info_labels(ArrayList meta_info_labels) {

        this.meta_info_labels = meta_info_labels;
    }

    public int getUsage_upload_publish_frequency() {

        return usage_upload_publish_frequency;
    }

    public void setUsage_upload_publish_frequency(int usage_upload_publish_frequency) {

        this.usage_upload_publish_frequency = usage_upload_publish_frequency;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public String getTenant_domain() {

        return tenant_domain;
    }

    public void setTenant_domain(String tenant_domain) {

        this.tenant_domain = tenant_domain;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getUsage_upload_cleanup_task_class() {

        return usage_upload_cleanup_task_class;
    }

    public void setUsage_upload_cleanup_task_class(String usage_upload_cleanup_task_class) {

        this.usage_upload_cleanup_task_class = usage_upload_cleanup_task_class;
    }

    public String getThrottling_synchronization_task_class() {

        return throttling_synchronization_task_class;
    }

    public void setThrottling_synchronization_task_class(String throttling_synchronization_task_class) {

        this.throttling_synchronization_task_class = throttling_synchronization_task_class;
    }

    public String getApi_update_task_class() {

        return api_update_task_class;
    }

    public void setApi_update_task_class(String api_update_task_class) {

        this.api_update_task_class = api_update_task_class;
    }

    public List getMeta_info_custom() {

        return meta_info_custom;
    }

    public void setMeta_info_custom(
            List meta_info_custom) {

        this.meta_info_custom = meta_info_custom;
    }

    public String getUsage_upload_file_data_upload_task_class() {

        return usage_upload_file_data_upload_task_class;
    }

    public void setUsage_upload_file_data_upload_task_class(String usage_upload_file_data_upload_task_class) {

        this.usage_upload_file_data_upload_task_class = usage_upload_file_data_upload_task_class;
    }

    public boolean isUsage_upload_publish_task_enabled() {

        return usage_upload_publish_task_enabled;
    }

    public void setUsage_upload_publish_task_enabled(boolean usage_upload_publish_task_enabled) {

        this.usage_upload_publish_task_enabled = usage_upload_publish_task_enabled;
    }
}
