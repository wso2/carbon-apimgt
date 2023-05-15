package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.HashSet;
import java.util.Set;

public class GatewayArtifactSynchronizerProperties {

    private boolean saveArtifactsEnabled = false;
    private boolean retrieveFromStorageEnabled = false;
    private String saverName = APIConstants.GatewayArtifactSynchronizer.DB_SAVER_NAME;
    private String retrieverName = APIConstants.GatewayArtifactSynchronizer.DB_RETRIEVER_NAME;
    private Set<String> gatewayLabels = new HashSet<>();
    private String artifactSynchronizerDataSource = "jdbc/WSO2AM_DB";
    private long retryDuartion = 15000 ;
    private int maxRetryCount = 5;
    private double retryProgressionFactor = 2.0;
    private String gatewayStartup = "sync";
    private long eventWaitingTime = 1;
    private boolean onDemandLoading;


    public String getSaverName() {

        return saverName;
    }

    public long getEventWaitingTime() {

        return eventWaitingTime;
    }

    public boolean hasEventWaitingTime() {

        return eventWaitingTime > 1;
    }

    public void setEventWaitingTime(long eventWaitingTime) {

        this.eventWaitingTime = eventWaitingTime;
    }


    public void setSaverName(String saverName) {

        this.saverName = saverName;
    }

    public String getRetrieverName() {

        return retrieverName;
    }

    public String getArtifactSynchronizerDataSource() {

        return artifactSynchronizerDataSource;
    }

    public void setArtifactSynchronizerDataSource(String artifactSynchronizerDataSource) {

        this.artifactSynchronizerDataSource = artifactSynchronizerDataSource;
    }

    public void setRetrieverName(String retrieverName) {

        this.retrieverName = retrieverName;
    }

    public Set<String> getGatewayLabels() {

        return gatewayLabels;
    }

    public void setGatewayLabels(Set<String> gatewayLabels) {

        this.gatewayLabels = gatewayLabels;
    }

    public void setPublishDirectlyToGatewayEnabled(boolean publishDirectlyToGatewayEnabled) {

    }

    public boolean isRetrieveFromStorageEnabled() {

        return retrieveFromStorageEnabled;
    }

    public void setRetrieveFromStorageEnabled(boolean retrieveFromStorageEnabled) {

        this.retrieveFromStorageEnabled = retrieveFromStorageEnabled;
    }

    public boolean isSaveArtifactsEnabled() {

        return saveArtifactsEnabled;
    }

    public void setSaveArtifactsEnabled(boolean saveArtifactsEnabled) {

        this.saveArtifactsEnabled = saveArtifactsEnabled;
    }

    public long getRetryDuartion() {

        return retryDuartion;
    }

    public void  setRetryDuartion(long retryDuartion) {

        this.retryDuartion = retryDuartion;
    }

    public int getMaxRetryCount() {

        return maxRetryCount;
    }

    public void  setMaxRetryCount(int maxRetryCount) {

        this.maxRetryCount = maxRetryCount;
    }

    public double getRetryProgressionFactor() {

        return retryProgressionFactor;
    }

    public void  setRetryProgressionFactor(double retryProgressionFactor) {

        this.retryProgressionFactor = retryProgressionFactor;
    }

    public String getGatewayStartup() {

        return gatewayStartup;
    }

    public void  setGatewayStartup(String gatewayStartup) {

        this.gatewayStartup = gatewayStartup;
    }

    public void setOnDemandLoading(boolean onDemandLoading) {
        this.onDemandLoading = onDemandLoading;
    }

    public boolean isOnDemandLoading() {
        return onDemandLoading;
    }
}
