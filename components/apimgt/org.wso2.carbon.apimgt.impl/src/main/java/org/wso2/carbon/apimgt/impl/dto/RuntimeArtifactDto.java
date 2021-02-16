package org.wso2.carbon.apimgt.impl.dto;

public class RuntimeArtifactDto {

    private Object artifact;
    private boolean file;

    public Object getArtifact() {

        return artifact;
    }

    public void setArtifact(Object artifact) {

        this.artifact = artifact;
    }

    public boolean isFile() {

        return file;
    }

    public void setFile(boolean file) {

        this.file = file;
    }
}
