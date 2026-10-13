/*
 *   Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.spec.parser.definitions;

import io.swagger.models.Swagger;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.refs.RefFormat;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.SwaggerResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.parser.OpenAPIResolver;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.processors.ComponentsProcessor;
import io.swagger.v3.parser.processors.OperationProcessor;
import io.swagger.v3.parser.processors.PathsProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

/**
 * Resolves the {@code $ref}s of an extracted OpenAPI archive when a network access control policy is configured.
 * <p>
 * For OpenAPI 3.0 and Swagger 2.0 the parser's resolver cache passes every external reference, including references
 * to sibling files in the archive, to {@code checkUrlIsPermitted} once {@code safelyResolveURL} is enabled, so a
 * multi-file archive cannot be resolved with the parser's own resolver. This class supplies a resolver cache that
 * validates HTTP(S) references with the parser's URL checker and keeps other references within the archive, and
 * drives the parser's processors with it in the same order as {@link OpenAPIResolver} and {@link SwaggerResolver}.
 * References inside fetched remote documents go through the same cache, so they are validated as well.
 * <p>
 * OpenAPI 3.1 definitions validate only HTTP(S) references in safe mode, so they are resolved by the parser directly.
 */
final class ArchiveRefResolver {

    private static final Log log = LogFactory.getLog(ArchiveRefResolver.class);

    private ArchiveRefResolver() {
    }

    /**
     * Resolve an OpenAPI 3.x archive master file under the given network access control policy.
     *
     * @param masterFilePath   absolute path of the archive master file
     * @param archiveRoot      root directory of the extracted archive
     * @param oasParserOptions parser options carrying the network access control policy
     * @return the resolved definition, or {@code null} if the master file could not be parsed
     * @throws APIManagementException with {@link ExceptionCodes#UNTRUSTED_URL_IN_DEFINITION} if a reference is not
     *                                permitted by the policy or does not stay within the archive
     */
    static OpenAPI resolveOpenAPI(String masterFilePath, File archiveRoot, OASParserOptions oasParserOptions)
            throws APIManagementException {

        ParseOptions safeOptions = new ParseOptions();
        safeOptions.setSafelyResolveURL(true);
        safeOptions.setRemoteRefAllowList(oasParserOptions.getRemoteRefAllowList());
        safeOptions.setRemoteRefBlockList(oasParserOptions.getRemoteRefBlockList());

        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        ParseOptions noResolveOptions = new ParseOptions();
        noResolveOptions.setResolve(false);
        OpenAPI openAPI = openAPIV3Parser.readLocation(masterFilePath, null, noResolveOptions).getOpenAPI();
        if (openAPI == null) {
            return null;
        }
        if (SpecVersion.V31.equals(openAPI.getSpecVersion())) {
            safeOptions.setResolve(true);
            SwaggerParseResult result = openAPIV3Parser.readLocation(masterFilePath, null, safeOptions);
            if (result.getMessages() != null) {
                for (String message : result.getMessages()) {
                    if (OASParserUtil.isUntrustedUrlInDefinition(message)) {
                        throw buildUntrustedUrlException(message, null);
                    }
                }
            }
            return result.getOpenAPI();
        }

        String canonicalRoot = canonicalPath(archiveRoot);
        File baseDirectory = new File(masterFilePath).getParentFile();
        ArchiveResolverCache cache = new ArchiveResolverCache(openAPI, masterFilePath, safeOptions, canonicalRoot,
                baseDirectory);
        try {
            new PathsProcessor(cache, openAPI, new OpenAPIResolver.Settings()).processPaths();
            new ComponentsProcessor(openAPI, cache).processComponents();
            if (openAPI.getPaths() != null) {
                OperationProcessor operationProcessor = new OperationProcessor(cache, openAPI);
                for (PathItem pathItem : openAPI.getPaths().values()) {
                    if (pathItem.readOperations() != null) {
                        for (Operation operation : pathItem.readOperations()) {
                            operationProcessor.processOperation(operation);
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            handleResolutionFailure(e);
        }
        return openAPI;
    }

    /**
     * Resolve a Swagger 2.0 archive master file under the given network access control policy.
     *
     * @param masterFilePath   absolute path of the archive master file
     * @param archiveRoot      root directory of the extracted archive
     * @param oasParserOptions parser options carrying the network access control policy
     * @return the resolved definition, or {@code null} if the master file could not be parsed
     * @throws APIManagementException with {@link ExceptionCodes#UNTRUSTED_URL_IN_DEFINITION} if a reference is not
     *                                permitted by the policy or does not stay within the archive
     */
    static Swagger resolveSwagger(String masterFilePath, File archiveRoot, OASParserOptions oasParserOptions)
            throws APIManagementException {

        io.swagger.parser.util.ParseOptions safeOptions = new io.swagger.parser.util.ParseOptions();
        safeOptions.setSafelyResolveURL(true);
        safeOptions.setRemoteRefAllowList(oasParserOptions.getRemoteRefAllowList());
        safeOptions.setRemoteRefBlockList(oasParserOptions.getRemoteRefBlockList());

        Swagger swagger = new SwaggerParser().read(masterFilePath, null, false);
        if (swagger == null) {
            return null;
        }
        String canonicalRoot = canonicalPath(archiveRoot);
        File baseDirectory = new File(masterFilePath).getParentFile();
        SwaggerArchiveResolverCache cache = new SwaggerArchiveResolverCache(swagger, masterFilePath, safeOptions,
                canonicalRoot, baseDirectory);
        try {
            if (swagger.getParameters() != null) {
                io.swagger.parser.processors.ParameterProcessor parameterProcessor =
                        new io.swagger.parser.processors.ParameterProcessor(cache, swagger);
                for (String name : swagger.getParameters().keySet()) {
                    io.swagger.models.parameters.Parameter parameter = swagger.getParameters().get(name);
                    if (parameter instanceof RefParameter
                            && ((RefParameter) parameter).getRefFormat() == RefFormat.RELATIVE) {
                        swagger.getParameters().put(name,
                                parameterProcessor.processParameters(Collections.singletonList(parameter)).get(0));
                    }
                }
            }
            new io.swagger.parser.processors.PathsProcessor(cache, swagger, new SwaggerResolver.Settings())
                    .processPaths();
            new io.swagger.parser.processors.DefinitionsProcessor(cache, swagger).processDefinitions();
            if (swagger.getPaths() != null) {
                io.swagger.parser.processors.OperationProcessor operationProcessor =
                        new io.swagger.parser.processors.OperationProcessor(cache, swagger);
                for (io.swagger.models.Path path : swagger.getPaths().values()) {
                    if (path.getOperations() != null) {
                        for (io.swagger.models.Operation operation : path.getOperations()) {
                            operationProcessor.processOperation(operation);
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            handleResolutionFailure(e);
        }
        return swagger;
    }

    /**
     * Map a reference rejected by the policy or by the archive check to
     * {@link ExceptionCodes#UNTRUSTED_URL_IN_DEFINITION}. Any other resolution failure leaves the definition as far
     * as it was resolved, matching the parser, which records such failures as messages and continues.
     */
    private static void handleResolutionFailure(RuntimeException e) throws APIManagementException {
        if (e instanceof ArchiveRefRejectedException || OASParserUtil.isUntrustedUrlInDefinition(e.getMessage())) {
            throw buildUntrustedUrlException(e.getMessage(), e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Error while resolving references in the OpenAPI archive", e);
        }
    }

    private static APIManagementException buildUntrustedUrlException(String reason, Throwable cause) {
        if (log.isDebugEnabled()) {
            log.debug("Rejected OpenAPI archive with a reference that is not permitted: " + reason, cause);
        }
        return new APIManagementException(ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorMessage(),
                ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION);
    }

    private static String canonicalPath(File directory) throws APIManagementException {
        try {
            return directory.getCanonicalPath();
        } catch (IOException e) {
            throw new APIManagementException("Could not resolve the OpenAPI archive directory for reference "
                    + "validation.", e);
        }
    }

    /**
     * Check a reference passed to the resolver cache. An HTTP(S) reference is reported to the caller for validation
     * against the policy. Any other reference must be a relative path that stays within the archive.
     *
     * @param ref           the reference, without its fragment
     * @param archiveRoot   canonical path of the archive root
     * @param baseDirectory directory relative references are resolved against
     * @return {@code true} if the reference is an HTTP(S) URL to be validated against the policy
     * @throws ArchiveRefRejectedException if the reference is not a relative path within the archive
     */
    private static boolean isRemoteRef(String ref, String archiveRoot, File baseDirectory) {
        String lowerCaseRef = ref.toLowerCase(Locale.ROOT);
        if (lowerCaseRef.startsWith("http://") || lowerCaseRef.startsWith("https://")) {
            return true;
        }
        // A reference with another scheme (file:, jar:, a drive letter) or an absolute path does not point to a file
        // in the archive.
        if (lowerCaseRef.matches("^[a-z][a-z0-9+.-]*:.*") || ref.startsWith("/") || ref.startsWith("\\")) {
            throw new ArchiveRefRejectedException("Reference is not a relative path within the archive: " + ref);
        }
        String target;
        try {
            target = new File(baseDirectory, ref).getCanonicalPath();
        } catch (IOException e) {
            throw new ArchiveRefRejectedException("Could not validate the archive reference: " + ref);
        }
        if (!target.equals(archiveRoot) && !target.startsWith(archiveRoot + File.separator)) {
            throw new ArchiveRefRejectedException("Reference resolves outside the archive: " + ref);
        }
        return false;
    }

    /**
     * Signals a reference that the archive resolver does not permit.
     */
    private static final class ArchiveRefRejectedException extends RuntimeException {

        ArchiveRefRejectedException(String message) {
            super(message);
        }
    }

    /**
     * OpenAPI 3.0 resolver cache that validates HTTP(S) references with the parser's URL checker and keeps other
     * references within the archive.
     */
    private static final class ArchiveResolverCache extends io.swagger.v3.parser.ResolverCache {

        private final String archiveRoot;
        private final File baseDirectory;

        ArchiveResolverCache(OpenAPI openAPI, String location, ParseOptions parseOptions, String archiveRoot,
                             File baseDirectory) {
            super(openAPI, new ArrayList<>(), location, new HashSet<String>(), parseOptions);
            this.archiveRoot = archiveRoot;
            this.baseDirectory = baseDirectory;
        }

        @Override
        protected void checkUrlIsPermitted(String ref) {
            if (isRemoteRef(ref, archiveRoot, baseDirectory)) {
                super.checkUrlIsPermitted(ref);
            }
        }
    }

    /**
     * Swagger 2.0 resolver cache that validates HTTP(S) references with the parser's URL checker and keeps other
     * references within the archive.
     */
    private static final class SwaggerArchiveResolverCache extends io.swagger.parser.ResolverCache {

        private final String archiveRoot;
        private final File baseDirectory;

        SwaggerArchiveResolverCache(Swagger swagger, String location, io.swagger.parser.util.ParseOptions parseOptions,
                                    String archiveRoot, File baseDirectory) {
            super(swagger, new ArrayList<>(), location, parseOptions);
            this.archiveRoot = archiveRoot;
            this.baseDirectory = baseDirectory;
        }

        @Override
        protected void checkUrlIsPermitted(String ref) {
            if (isRemoteRef(ref, archiveRoot, baseDirectory)) {
                super.checkUrlIsPermitted(ref);
            }
        }
    }
}
