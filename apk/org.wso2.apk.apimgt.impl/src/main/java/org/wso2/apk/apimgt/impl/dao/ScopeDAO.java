package org.wso2.apk.apimgt.impl.dao;

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.Scope;
import org.wso2.apk.apimgt.api.model.SharedScopeUsage;

import java.util.List;
import java.util.Set;

public interface ScopeDAO {

    /**
     * Checks whether the given shared scope name is already available under given tenant domain.
     *
     * @param scopeName Scope Name
     * @param tenantId  Tenant ID
     * @return scope name availability
     * @throws APIManagementException If an error occurs while checking the availability
     */
    boolean isSharedScopeExists(String scopeName, int tenantId) throws APIManagementException;

    /**
     * Add shared scope.
     *
     * @param scope        Scope Object to add
     * @param tenantDomain Tenant domain
     * @return UUID of the shared scope
     * @throws APIManagementException if an error occurs while adding shared scope
     */
    String addSharedScope(Scope scope, String tenantDomain) throws APIManagementException;

    /**
     * Delete shared scope.
     *
     * @param scopeName    shared scope name
     * @param tenantDomain tenant domain
     * @throws APIManagementException if an error occurs while removing shared scope
     */
    void deleteSharedScope(String scopeName, String tenantDomain) throws APIManagementException;

    /**
     * Get all shared scopes for tenant.
     *
     * @param tenantDomain Tenant Domain
     * @return shared scope list
     * @throws APIManagementException if an error occurs while getting all shared scopes for tenant
     */
    List<Scope> getAllSharedScopes(String tenantDomain) throws APIManagementException;

    /**
     * Get all scopes for tenant.
     *
     * @param tenantId Tenant Domain ID
     * @return scope list
     * @throws APIManagementException if an error occurs while getting all scopes for tenant
     */
    List<Scope> getScopes(int tenantId) throws APIManagementException;

    /**
     * Get scope by name for tenant.
     * @param name Scope Name
     * @param tenantId Tenant Domain ID
     * @return scope Object
     * @throws APIManagementException if an error occurs while getting scope object
     */
    Scope getScope(String name, int tenantId) throws APIManagementException;

    /**
     * Get all shared scope keys for tenant.
     *
     * @param tenantDomain Tenant Domain
     * @return shared scope list
     * @throws APIManagementException if an error occurs while getting all shared scopes for tenant
     */
    Set<String> getAllSharedScopeKeys(String tenantDomain) throws APIManagementException;

    /**
     * Get shared scope key by uuid.
     *
     * @param uuid UUID of shared scope
     * @return Shared scope key
     * @throws APIManagementException if an error occurs while getting shared scope
     */
    String getSharedScopeKeyByUUID(String uuid) throws APIManagementException;

    /***
     * Get the API and URI usages of the given shared scope
     *
     * @param uuid Id of the shared scope
     * @param tenantId tenant Id
     * @return usgaes ofr the shaerd scope
     * @throws APIManagementException If an error occurs while getting the usage details
     */
    SharedScopeUsage getSharedScopeUsage(String uuid, int tenantId) throws APIManagementException;

    /**
     * Get all scopes by Subscribed APIs
     *
     * @param identifiers Subscribed APIs
     * @return scope list
     * @throws APIManagementException if an error occurs while getting all scopes
     */
    Set<String> getScopesBySubscribedAPIs(List<String> identifiers) throws APIManagementException;

}
