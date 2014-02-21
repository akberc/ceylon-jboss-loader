package com.redhat.ceylon.cmr.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.redhat.ceylon.cmr.api.ArtifactContext;
import com.redhat.ceylon.cmr.api.ArtifactResult;
import com.redhat.ceylon.cmr.api.ArtifactResultType;
import com.redhat.ceylon.cmr.api.ModuleQuery;
import com.redhat.ceylon.cmr.api.ModuleSearchResult;
import com.redhat.ceylon.cmr.api.ModuleVersionQuery;
import com.redhat.ceylon.cmr.api.ModuleVersionResult;
import com.redhat.ceylon.cmr.api.RepositoryException;
import com.redhat.ceylon.cmr.api.RepositoryManager;

public class RepositoryManagerProxy implements RepositoryManager {

    String ceylonModulesDir = System.getProperty("jboss.modules.dir")
        + File.separator + "system" + File.separator + "add-ons" + File.separator + "ceylon";
    
    
    @Override
    public File[] resolve(String name, String version) throws RepositoryException {
        ArtifactResult result = getArtifactResult(name, version);
        if (result.artifact() != null) {
            return new File[]{result.artifact()};
        } else {
            return new File[0];
        }
    }

    @Override
    public File[] resolve(ArtifactContext context) throws RepositoryException {
        ArtifactResult result = getArtifactResult(context);
        if (result.artifact() != null) {
            return new File[]{result.artifact()};
        } else {
            return new File[0];
        }
    }

    @Override
    public File getArtifact(String name, String version) throws RepositoryException {
        ArtifactResult result = getArtifactResult(name, version);
        if (result.artifact() != null) {
            return result.artifact();
        } else {
            return null;
        }
    }

    @Override
    public File getArtifact(ArtifactContext context) throws RepositoryException {
        ArtifactResult result = getArtifactResult(context);
        if (result.artifact() != null) {
            return result.artifact();
        } else {
            return null;
        }
    }

    @Override
    public ArtifactResult getArtifactResult(String name, String version) throws RepositoryException {
        File dir = new File(ceylonModulesDir + File.separatorChar + name.replace('.', File.separatorChar) + File.separatorChar + version);
        ArtifactResultType type = ArtifactResultType.OTHER;
        File artifact = null;
        
        if (dir.isDirectory()) {
            artifact = findArtifactInDir(dir,".car");
            if (artifact.isFile()) {
                type = ArtifactResultType.CEYLON;
            } else {
                artifact = findArtifactInDir(dir,".jar");
                if (artifact.isFile()) {
                    type = ArtifactResultType.MAVEN;
                } else {
                    throw new RepositoryException("Artifact jar or car not found: " + name + "/" + version);
                }
            }
        } else {
            throw new RepositoryException("Repository path could not be found for artifact: " + name + "/" + version);
        }
        WildflyArtifactResult result = new WildflyArtifactResult(name, version, type);
        result.setFile(artifact);
        return result;
    }

    private File findArtifactInDir(final File dir, final String ext) {
        File[] children = dir.listFiles(new FileFilter() {  
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(ext)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        if (children.length > 0) {
            return children[0];
        } else {
            return null;
        }
    }

    @Override
    public ArtifactResult getArtifactResult(ArtifactContext context) throws RepositoryException {
        ArtifactResult result = getArtifactResult(context.getName(), context.getVersion());
        // TODO
        return result;
    }

    @Override
    public void putArtifact(String name, String version, InputStream content) throws RepositoryException {
        throw new UnsupportedOperationException("Operation not allowed in runtime");   
    }

    @Override
    public void putArtifact(String name, String version, File content) throws RepositoryException {
        throw new UnsupportedOperationException("Operation not allowed in runtime");
    }

    @Override
    public void putArtifact(ArtifactContext context, InputStream content) throws RepositoryException {
        throw new UnsupportedOperationException("Operation not allowed in runtime");
    }

    @Override
    public void putArtifact(ArtifactContext context, File content) throws RepositoryException {
        throw new UnsupportedOperationException("Operation not allowed in runtime");  
    }

    @Override
    public void removeArtifact(String name, String version) throws RepositoryException {
        throw new UnsupportedOperationException("Operation not allowed in runtime");   
    }

    @Override
    public void removeArtifact(ArtifactContext context) throws RepositoryException {
        throw new UnsupportedOperationException("Operation not allowed in runtime");    
    }

    @Override
    public List<String> getRepositoriesDisplayString() {
        return Arrays.asList(new String[]{"Repository: " + ceylonModulesDir});
    }

    @Override
    public ModuleSearchResult completeModules(ModuleQuery query) {
        throw new UnsupportedOperationException("Operation not allowed in runtime"); 
    }

    @Override
    public ModuleVersionResult completeVersions(ModuleVersionQuery query) {
        throw new UnsupportedOperationException("Operation not allowed in runtime"); 
    }

    @Override
    public ModuleSearchResult searchModules(ModuleQuery query) {
        throw new UnsupportedOperationException("Operation not allowed in runtime"); 
    }

    @Override
    public void refresh(boolean recurse) {
        // nothing to do
    }

}
