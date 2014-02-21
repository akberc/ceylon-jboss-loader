package com.redhat.ceylon.cmr.impl;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.redhat.ceylon.cmr.api.ArtifactResult;
import com.redhat.ceylon.cmr.api.ArtifactResultType;
import com.redhat.ceylon.cmr.api.RepositoryException;

public class WildflyArtifactResult extends AbstractArtifactResult {

    ArtifactResultType type;
    File file;
    
    protected WildflyArtifactResult(String name, String version, ArtifactResultType type) {
        super(name, version);
        this.type = type;
    }

    @Override
    public ArtifactResultType type() {
        return type;
    }

    @Override
    public List<ArtifactResult> dependencies() throws RepositoryException {
        return Collections.emptyList();
    }

    @Override
    protected File artifactInternal() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
