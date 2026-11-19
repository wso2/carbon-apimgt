/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants;
import org.wso2.carbon.apimgt.api.gateway.CredentialDto;
import org.wso2.carbon.apimgt.impl.utils.GatewayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the secure-vault storage of a GCP service-account key. The key (base64) is too large to be a
 * single vault entry, so it is split into column-sized chunks, each registered under a per-chunk alias, and the
 * gateway mediator reassembles them (pipe-joined) before decoding.
 * <p>
 * This keeps the chunk sizing, alias generation and the {@code wso2:vault-lookup} expression - all secure-vault
 * storage details - out of {@link TemplateBuilderUtil} and off the endpoint model, which only needs the
 * resulting expression to emit.
 */
final class GCPServiceAccountKeyVaultStore {

    private GCPServiceAccountKeyVaultStore() {

    }

    /**
     * The credentials to register in the vault and the expression that reassembles them - the two outputs of
     * chunking a GCP service-account key for secure-vault storage.
     */
    static final class VaultChunks {

        private final List<CredentialDto> credentials;
        private final String vaultLookupExpression;

        private VaultChunks(List<CredentialDto> credentials, String vaultLookupExpression) {

            this.credentials = credentials;
            this.vaultLookupExpression = vaultLookupExpression;
        }

        List<CredentialDto> getCredentials() {

            return credentials;
        }

        String getVaultLookupExpression() {

            return vaultLookupExpression;
        }
    }

    /**
     * Splits the base64 key into vault-sized chunks, builds a per-chunk credential (alias + chunk value) for each,
     * and returns those credentials together with the {@code wso2:vault-lookup} expression that reassembles them.
     *
     * @param base64Key    the base64-encoded service-account key
     * @param apiName      the API name (for the per-chunk alias)
     * @param apiVersion   the API version (for the per-chunk alias)
     * @param endpointUuid the endpoint UUID (for the per-chunk alias)
     * @param stage        the deployment stage (PRODUCTION / SANDBOX)
     * @return the credentials to register and the vault-lookup expression to emit
     */
    static VaultChunks chunkForVault(String base64Key, String apiName, String apiVersion, String endpointUuid,
            String stage) {

        // The secure-vault column caps each stored value, so one large key cannot be a single vault entry.
        List<String> chunks = chunkString(base64Key, AIAPIConstants.GCP_SERVICE_ACCOUNT_KEY_VAULT_CHUNK_LENGTH);
        List<CredentialDto> credentials = new ArrayList<>(chunks.size());
        List<String> aliases = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            String alias = GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias(apiName, apiVersion, endpointUuid,
                    stage, i);
            aliases.add(alias);
            CredentialDto credentialDto = new CredentialDto();
            credentialDto.setAlias(alias);
            credentialDto.setPassword(chunks.get(i));
            credentials.add(credentialDto);
        }
        return new VaultChunks(credentials, buildVaultLookupExpression(aliases));
    }

    /**
     * Splits a string into ordered chunks of at most {@code chunkLength} characters. The input is base64, so it
     * can be split at any character boundary without corrupting multi-byte characters.
     *
     * @param value       the base64 string to split
     * @param chunkLength the maximum length of each chunk
     * @return the ordered list of chunks
     */
    private static List<String> chunkString(String value, int chunkLength) {

        List<String> chunks = new ArrayList<>();
        for (int offset = 0; offset < value.length(); offset += chunkLength) {
            chunks.add(value.substring(offset, Math.min(value.length(), offset + chunkLength)));
        }
        return chunks;
    }

    /**
     * Builds the synapse XPath expression that reassembles the GCP service-account key from its per-chunk vault
     * aliases. Each alias resolves via {@code wso2:vault-lookup} and the chunks are joined with {@code '|'} (the
     * mediator splits on that delimiter; base64 never contains it). A single alias is emitted as a bare lookup
     * because XPath {@code concat} requires at least two arguments.
     *
     * @param aliases the ordered per-chunk vault aliases
     * @return the {@code wso2:vault-lookup} / {@code concat(...)} expression
     */
    static String buildVaultLookupExpression(List<String> aliases) {

        if (aliases.size() == 1) {
            return "wso2:vault-lookup('" + aliases.get(0) + "')";
        }
        StringBuilder expression = new StringBuilder("concat(");
        for (int i = 0; i < aliases.size(); i++) {
            if (i > 0) {
                expression.append(", '|', ");
            }
            expression.append("wso2:vault-lookup('").append(aliases.get(i)).append("')");
        }
        return expression.append(")").toString();
    }
}
