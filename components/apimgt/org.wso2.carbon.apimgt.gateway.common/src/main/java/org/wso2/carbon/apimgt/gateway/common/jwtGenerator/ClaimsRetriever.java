package org.wso2.carbon.apimgt.gateway.common.jwtGenerator;

import org.wso2.carbon.apimgt.gateway.common.exception.JWTGeneratorException;

import java.util.SortedMap;

/**
 * This interface encapsulates a user claims retriever.
 * The retrieved claims are encoded to the JWT during subscriber validation
 * in the order defined by the SortedMap.
 * Anyone trying to add custom user properties to the JWT should implement this interface
 * and mention the fully qualified class name in api-manager.xml ->
 * JWTConfiguration -> ClaimsRetrieverImplClass
 */
public interface ClaimsRetriever {

    String DEFAULT_DIALECT_URI = "http://wso2.org/claims";

    /**
     * Initialization method that runs only once.
     *
     * @throws JWTGeneratorException
     */
    void init() throws JWTGeneratorException;

    /**
     * Method that retrieves user claims
     *
     * @return a sorted map
     * keys - claimURIs
     * values - claim values.
     * @throws JWTGeneratorException
     */
    SortedMap<String, String> getClaims(String endUserName) throws JWTGeneratorException;

    /**
     * Must return the dialect URI of the user ClaimURIs.
     *
     * @throws JWTGeneratorException
     */
    String getDialectURI(String endUserName) throws JWTGeneratorException;

}
