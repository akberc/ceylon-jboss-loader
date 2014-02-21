package com.dgwave.car.loader;

import static org.junit.Assert.*;

import java.io.File;

import org.jboss.modules.ConcreteModuleSpec;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleSpec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CarModuleFinderTestCase {

    CarModuleFinder finder;
    
    @Before
    public void setUp() throws Exception {
        System.setProperty("module.path", 
            new File(".", "./src/test/resources/modules").getAbsolutePath());
        finder = new CarModuleFinder();
    }

    @After
    public void tearDown() throws Exception {
        finder = null;
        System.clearProperty("module.path");
    }

    @Test
    public void testFindModule() {
        try {
            ModuleSpec spec = finder.findModule(ModuleIdentifier.create("test.mod", "1.0.0"), null);
            assertNotNull(spec);
            assertEquals("test.mod", spec.getModuleIdentifier().getName());
            assertEquals("1.0.0", spec.getModuleIdentifier().getSlot());
            assertTrue(spec instanceof ConcreteModuleSpec);
            ConcreteModuleSpec concreteSpec = (ConcreteModuleSpec) spec;
            concreteSpec.getMainClass();
        } catch (ModuleLoadException e) {
            fail("Module should have been found");
        }
    }

}
