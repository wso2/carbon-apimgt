package org.wso2.carbon.apimgt.impl.containermgt.k8scrd;

import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Arrays;

public class Interceptors implements KubernetesResource {

    private String[] ballerina;
    private String[] java;

    public String[] getBallerina() { return ballerina; }

    public void setBallerina(String[] ballerina) { this.ballerina = ballerina; }

    public String[] getJava() { return java; }

    public void setJava(String[] java) { this.java = java; }

    /**
     * This method is to create the following json object
     * {
     *   "ballerina": ["${balInterceptorsConfigmapsNames}"],
     *   "java": ["${javaInterceptorsConfigmapsNames}"],
     * },
     *
     * @return
     */
    @Override
    public String toString() {
        return "Interceptors{" +
                "ballerina=" + Arrays.toString(ballerina) +
                ", java=" + Arrays.toString(java) +
                '}';
    }
}
