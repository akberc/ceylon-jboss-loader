package com.redhat.ceylon.cmr.impl;

import com.redhat.ceylon.cmr.api.Logger;
import com.redhat.ceylon.cmr.api.RepositoryManager;
import com.redhat.ceylon.cmr.api.RepositoryManagerBuilder;

public class RepositoryManagerBuilderProxy extends RepositoryManagerBuilder {

    Logger log; // may be null

    RepositoryManagerProxy manager;
    
    RepositoryManagerBuilderProxy() {
 
        this.manager = (RepositoryManagerProxy) buildRepository();

    }
    
    public RepositoryManagerBuilderProxy(Logger log, boolean offline, String overrides) {
        this();
        this.log = log;
    }

    public RepositoryManager buildRepository() {
        if (this.manager == null) {
            this.manager = new RepositoryManagerProxy();
        }
        return this.manager;
    }
}
