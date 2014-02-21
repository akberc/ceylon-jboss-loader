package com.dgwave.car.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.modules.LocalModuleFinder;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;

import com.redhat.ceylon.cmr.api.RepositoryException;

/**
 * A module loader for Ceylon 'car' modules.
 * It just inserts a custom finder ahead of the local finder. The jar containing this
 * loader should be on the JBoss/Wildfly classpath with jboss-modules.jar
 * 
 * @author Akber Choudhry
 */
public final class CarModuleLoader extends ModuleLoader {
    
    private Module langModule;
    private Object ceylonLayerProxy;
    private Method getArtifactResult;
    private Method loadModule;
    private Object metaModel;
    
    private Module getLangModule() throws ModuleLoadException {
        if (langModule == null) {
            try {
                langModule = loadModule(ModuleIdentifier.create("ceylon.language", "1.0.0"));
                Class<?> clazz = langModule.getClassLoader()
                    .loadClass("com.redhat.ceylon.cmr.impl.RepositoryManagerProxy");
                ceylonLayerProxy = clazz.newInstance();
                getArtifactResult = clazz.getMethod("getArtifactResult", String.class, String.class);
                
                clazz = getLangModule().getClassLoader()
                    .loadClass("com.redhat.ceylon.compiler.java.runtime.metamodel.Metamodel");
                metaModel = clazz.newInstance();
                if (clazz != null) {
                    for (Method m : clazz.getMethods()) {
                        if (m.getName().equals("loadModule")) {
                            loadModule = m;
                        }
                    }
                }
                
                loadModule.invoke(metaModel, langModule.getIdentifier().getName(), langModule.getIdentifier().getSlot(),
                    getArtifactResult.invoke(
                        ceylonLayerProxy, langModule.getIdentifier().getName(), langModule.getIdentifier().getSlot()), 
                    langModule.getClassLoader());

            } catch (ClassNotFoundException e) {
                throw new ModuleLoadException("Error loading ceylon.language metamodel", e);
            } catch (InstantiationException e) {
                throw new ModuleLoadException("Error loading ceylon.language metamodel", e);
            } catch (IllegalAccessException e) {
                throw new ModuleLoadException("Modular classloading problem while loading Ceylon metamodel", e);
            } catch (NoSuchMethodException e) {
                throw new ModuleLoadException("Error loading ceylon.language metamodel", e);
            } catch (SecurityException e) {
                throw new ModuleLoadException("Security Manager prevented Ceylon metamodel pre-loading", e);
            } catch (IllegalArgumentException e) {
                throw new ModuleLoadException("While loading Ceylon metamodel, class being referenced across module classloaders", e);
            } catch (InvocationTargetException e) {
                throw new ModuleLoadException("Could not load Ceylon metamdodel", e);
            }
        }
        return langModule;
    }
    
    /**
     * Default public constructor.
     * Add the car finder in front
     */
    public CarModuleLoader() {
      super(new ModuleFinder[] {
          new CarModuleFinder(),
          new LocalModuleFinder()
          });
    }
    
    @Override
    protected Module preloadModule(ModuleIdentifier identifier) throws ModuleLoadException {
        Module module = super.preloadModule(identifier);
        if (module != null && Boolean.parseBoolean(module.getProperty("ceylon.module"))) {
            loadMetaModel(module);
        }
        return module;
    }

    private void loadMetaModel(Module module) throws ModuleLoadException {
        if (langModule == null) {
            langModule = getLangModule();
        }
        
        ModuleIdentifier identifier = module.getIdentifier();
        try {
            loadModule.invoke(metaModel, identifier.getName(), identifier.getSlot(), 
                getArtifactResult.invoke(ceylonLayerProxy, identifier.getName(), identifier.getSlot()), module.getClassLoader());
        } catch (SecurityException e) {
            throw new ModuleLoadException("Security Manager prevented Ceylon metamodel pre-loading", e);
        } catch (IllegalAccessException e) {
            throw new ModuleLoadException("Modular classloading problem while loading Ceylon metamodel", e);
        } catch (IllegalArgumentException e) {
            throw new ModuleLoadException("While loading Ceylon metamodel, class being referenced across module classloaders", e);
        } catch (InvocationTargetException e) {
            throw new ModuleLoadException("Could not load Ceylon metamdodel", e);
        } catch (RepositoryException e) {
            throw new ModuleLoadException("Error in bridge between Ceylon repository and JBoss/Wildfly repository", e);
        }
    }
}
