package com.dgwave.car.loader;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;

import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ModuleSpec.Builder;

/**
 * This class is not active.
 * Placeholder in case looking up Ceylon annotations at runtime is not possible
 * Inspired by scannotation by Bill Burke (http://scannotation.sourceforge.net/)
 * A module loader must be as self-contained as possible and reduce external dependencies
 * 
 * @author Akber Choudhry
 */
public class CarScanner {
    
    /**
     * Generates a module spec by scanning annotations from a class.
     * @param file File to scan
     * @param pkg Package in which to find the class
     * @return ModuleSpec The module spec
     * @throws ModuleLoadException In case of IO error
     */
    ModuleSpec generateModuleSpec(final File file, final String pkg) throws ModuleLoadException {
        
        JarFile car = null;
        
        try {
            car = new JarFile(file);
            InputStream is = car.getInputStream(car.getJarEntry(pkg.replace('.', '/') + "/module_.class"));
            for (Annotation ann : scanClass(is)) {
                Builder builder = ModuleSpec.build(ModuleIdentifier.create(pkg));
                builder.addProperty(ann.getTypeName(), ann.toString()); // place holder
            }
        } catch (Exception e) {
            throw new ModuleLoadException(
                "Error loading CAR module. Malformed or does not contain a module descriptor (module.ceylon)", e);
        } finally {
            if (car != null) {
                try {
                    car.close();
                } catch (IOException e) {
                    throw new ModuleLoadException("IO error reading class file", e);
                }
            }            
        }
        return null;
    }

    /**
     * Scans by reading a class from an input stream.
     * @param bits The input stream
     * @return A lit of annotations
     * @throws IOException In case of IO error
     */
    private List<Annotation> scanClass(final InputStream bits) throws IOException {
       
       DataInputStream dstream = new DataInputStream(new BufferedInputStream(bits));
       
       ClassFile cf = null;
       
       try {
          cf = new ClassFile(dstream);
          return scanClass(cf);
       } finally {
          dstream.close();
          bits.close();
       }
    }
    
    /**
     * Scans a class representation for annotations.
     * @param cf The ClassFile
     * @return A lit of annotations
     */
    private List<Annotation> scanClass(final ClassFile cf) {

       AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
       AnnotationsAttribute invisible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.invisibleTag);
       
       ArrayList<Annotation> list = new ArrayList<Annotation>();

       if (visible != null) {
           list.addAll(Arrays.asList(visible.getAnnotations()));
       }
       
       if (invisible != null) {
           list.addAll(Arrays.asList(invisible.getAnnotations()));
       }
       
       return list;
    }   
}
