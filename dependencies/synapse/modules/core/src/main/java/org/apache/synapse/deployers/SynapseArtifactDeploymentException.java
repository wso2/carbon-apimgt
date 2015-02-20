/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.deployers;

/**
 * Represents an exception for synapse artifact deployment issues. Throwing a SynapseArtifactDeploymentException
 * in the implementations of the {@link org.apache.synapse.deployers.AbstractSynapseArtifactDeployer} will cause the
 * respective artifact to be re-stored, if it is either being updated or undeployed
 *
 * @see org.apache.synapse.deployers.AbstractSynapseArtifactDeployer
 */
public class SynapseArtifactDeploymentException extends RuntimeException {

    public SynapseArtifactDeploymentException(String message) {
        super(message);
    }

    public SynapseArtifactDeploymentException(Throwable cause) {
        super(cause);
    }

    public SynapseArtifactDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
