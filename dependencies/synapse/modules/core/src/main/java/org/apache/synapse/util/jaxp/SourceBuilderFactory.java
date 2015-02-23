package org.apache.synapse.util.jaxp;

import org.apache.synapse.core.SynapseEnvironment;

public interface SourceBuilderFactory {
    /**
     * Create a new {@link SourceBuilder} instance.
     * 
     * @param synEnv the Synapse environment
     * @return the newly created instance
     */
    SourceBuilder createSourceBuilder(SynapseEnvironment synEnv);
}
