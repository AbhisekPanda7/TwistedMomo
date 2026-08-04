package com.twistedmomos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTest {

    private static final ApplicationModules MODULES = ApplicationModules.of(BackendApplication.class);

    /** Fails the build on a cycle or a reference into another module's internals. */
    @Test
    void verifiesModuleStructure() {
        MODULES.verify();
    }

    /** Writes module diagrams and canvases to target/spring-modulith-docs. */
    @Test
    void writesDocumentation() {
        new Documenter(MODULES).writeDocumentation();
    }
}
