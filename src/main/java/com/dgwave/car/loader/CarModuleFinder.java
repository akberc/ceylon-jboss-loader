package com.dgwave.car.loader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ModuleSpec.AliasBuilder;
import org.jboss.modules.ModuleSpec.Builder;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.filter.MultiplePathFilterBuilder;
import org.jboss.modules.filter.PathFilters;

import com.dgwave.lahore.api.Description$annotation$;
import com.dgwave.lahore.api.Id$annotation$;
import com.dgwave.lahore.api.Name$annotation$;
import com.redhat.ceylon.compiler.java.metadata.Import;
import com.redhat.ceylon.compiler.java.metadata.Module;

/**
 * Only finds 'car' modules in the Ceylon modules layer.
 * Does not support arbitrary layers and add-ons at this time
 * 
 * @author Akber Choudhry
 */
public final class CarModuleFinder implements ModuleFinder {
    
    /**
     * The supported Ceylon language version.
     */
    private static final String CEYLON_LANGUAGE_VERSION = "1.0.0";
    
    /**
     * The root of the Ceylon JBoss/Wildfly add-on repository.
     */
    private static String ceylonLayerRoot = System.getProperty("module.path")
        + File.separator + "system" + File.separator + "add-ons" + File.separator + "ceylon";

    
    /** Default package-private constructor.
     * @param local Handle to default local module finder
     */
    CarModuleFinder() {
        org.jboss.modules.Module.getModuleLogger().trace("Ceylon Module Loader v0.5 activated");
        System.out.println("Ceylon Module Loader v0.5 activated");
    }

    /**
     * Utility method to convert a module identifier to a relative path within the add-on repository.
     * @param moduleIdentifier The module identifier
     * @return String The relaive path
     */
    private static String toPathString(final ModuleIdentifier moduleIdentifier) {
      StringBuilder builder = new StringBuilder(40);
      builder.append(moduleIdentifier.getName().replace('.', File.separatorChar));
      builder.append(File.separatorChar).append(moduleIdentifier.getSlot());
      builder.append(File.separatorChar);
      return builder.toString();
    }
    
    
    /**
     * Method called by JBoss Modules to find a Ceylon module.
     * @param identifier The module identifier
     * @param delegateLoader Delegate loader, which is not useful in this context
     * @return ModuleSpec The module spec
     * @throws ModuleLoadException In case of error
     */
    @Override
    public ModuleSpec findModule(final ModuleIdentifier identifier, final ModuleLoader delegateLoader) 
            throws ModuleLoadException {
        final String child = toPathString(identifier);
        File file = new File(ceylonLayerRoot, child);
        File moduleXml = new File(file, "module.xml");
        if (moduleXml.exists()) {
            if (!specialHandling(identifier)) {
                return null; // nothing to do wth Ceylon
            } else {
                AliasBuilder aliasBuilder = ModuleSpec.buildAlias(identifier, 
                    ModuleIdentifier.create("com.dgwave.car.loader", "main"));
                return aliasBuilder.create();
            }
        } else {
            File[] files = file.listFiles(new FileFilter() {      
                @Override
                public boolean accept(final File candidate) {
                    return candidate.getName().endsWith(".car");
                }
            });
            
            if (files != null && files.length == 1) {
                return generateModuleSpec(files[0], identifier);
            }
        }
        return null;
    }
    
    /**
     * Deserves special consideration.
     */
    private boolean specialHandling(ModuleIdentifier identifier) {
        return "com.redhat.ceylon.module-resolver".equals(identifier.getName());
    }

    /**
     * Produces a module spec from an identified 'car' file.
     * @param file The Ceylon module 'car' file
     * @param moduleIdentifier The module identifier
     * @return ModuleSpec The module identifier
     * @throws ModuleLoadException In case of error
     */
    private ModuleSpec generateModuleSpec(final File file, final ModuleIdentifier moduleIdentifier) 
            throws ModuleLoadException {
        try {
            URLClassLoader cl = new URLClassLoader(new URL[]{ file.toURI().toURL() });
            Class<?> moduleClass = Class.forName(moduleIdentifier.getName() + ".module_", true, cl);
            if (moduleClass == null || !moduleClass.isAnnotationPresent(Module.class)) {
                return null; // quick exit
            }
            
            Module ceylonModule = moduleClass.getAnnotation(Module.class);
            
            if (ceylonModule != null) {
                
                MultiplePathFilterBuilder pathFilterBuilder = PathFilters.multiplePathFilterBuilder(true);
                pathFilterBuilder.addFilter(PathFilters.isOrIsChildOf(
                    moduleIdentifier.getName().replace('.', '/')), true);
                
                ModuleSpec.Builder builder = ModuleSpec.build(moduleIdentifier);
                builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(
                    ResourceLoaders.createJarResourceLoader(file.getName(), new JarFile(file, true)), 
                    pathFilterBuilder.create()));
                
                for (Import imp : ceylonModule.dependencies()) {
                    // Skips java and javax. oracle and sun modules wil not be found. 
                    // Attempting to be JVM vendor agnostic
                    if (!imp.name().startsWith("java")) {
                        String name = wildflyName(imp.name());
                        String version = wildflyVersion(name, imp.version());
                        DependencySpec mds = DependencySpec.createModuleDependencySpec(
                            ModuleIdentifier.create(name, version), imp.export(), imp.optional());
                        builder.addDependency(mds); 
                    }
                }
                
                builder.addProperty("ceylon.module", "true");

                builder.addDependency(DependencySpec.createModuleDependencySpec(
                        ModuleIdentifier.create("ceylon.language", CEYLON_LANGUAGE_VERSION), true, false));

                builder.addDependency(DependencySpec.createLocalDependencySpec());
 
                addLahoreProperties(builder, moduleClass);
                addRunClass(builder, cl, moduleIdentifier); 
                
                return builder.create();        
            }    
            
        } catch (MalformedURLException e) {
            throw new ModuleLoadException("Error loading CAR module: ", e);
        } catch (ClassNotFoundException e) {
            throw new ModuleLoadException(
                "Error loading CAR module. Does not contain a module descriptor (module.ceylon)", e);
        } catch (IOException e) {
            throw new ModuleLoadException("Error loading CAR module. Module not packaged propertly", e);
        }
        return null;
    }

    /**
     * Add a run class if one exists.
     * @param builder The ModuleSpec builder
     * @param cl The classloader for the car file
     * @param moduleIdentifier The module identifier
     */
    private void addRunClass(final Builder builder, final URLClassLoader cl, final ModuleIdentifier moduleIdentifier) {
        try {
            Class<?> runClass = Class.forName(moduleIdentifier.getName() + ".run_", true, cl);
            if (runClass == null) {
                return;
            } else {
                builder.setMainClass(moduleIdentifier.getName() + ".run_");
            }
        } catch (ClassNotFoundException e) {
            org.jboss.modules.Module.getModuleLogger()
                .trace("run_ class not found in Ceylon module: " + moduleIdentifier);
        }   
    }

    /**
     * Attaches Lahore properties to the loaded module.
     * @param builder The moduleSpec builder
     * @param moduleCls The module class
     */
    private void addLahoreProperties(final Builder builder, final Class<?> moduleCls) {
        
        Id$annotation$ lahoreId = moduleCls.getAnnotation(Id$annotation$.class);
        
        if (lahoreId != null) {
            builder.addProperty("lahore.id", lahoreId.id());
        
            Name$annotation$ lahoreName = moduleCls.getAnnotation(Name$annotation$.class);
            if (lahoreName != null) {
                builder.addProperty("lahore.name", lahoreName.name()); 
            }
            
            Description$annotation$ lahoreDescription = moduleCls.getAnnotation(Description$annotation$.class);
            if (lahoreDescription != null) {
                builder.addProperty("lahore.description", lahoreDescription.description()); 
            }            
        } 
    }

    /**
     * Override Ceylon Java module dependencies to JBoss/Wildfly module names.
     * Although Ceylon Herd repository may serve Java modules, it attempts to rename them from their published names
     * Not every module needs to be listed here. Other modules with new names or aliases can be added to the
     * Ceylon layer. This override is required only when interaction with existing JBoss subsystems is needed.
     * @param name Name of the module
     * @return String The canonical JBoss/Wildfly name
     */
    private String wildflyName(final String name) {
        if ("org.jboss.xnio.api".equals(name)) {
            return "org.jboss.xnio";
        }
        return name;
    }

    /**
     * Override Ceylon Java module versions to JBoss/Wildfly versions, generally 'main'.
     * @param name The name of the module
     * @param version The vesion
     * @return String The canonical JBoss/Wildfly version
     */
    private String wildflyVersion(final String name, final String version) {
        if (name.startsWith("io.undertow")
            || name.startsWith("org.jboss.xnio")) {
            return "main";
        }
        return version;
    }
}
