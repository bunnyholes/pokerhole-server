package dev.xiyo.pokerhole.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Hexagonal Architecture 규칙을 검증하는 테스트
 * ArchUnit을 사용하여 아키텍처 의존성 규칙을 강제합니다.
 */
class HexagonalArchitectureTest {
    
    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("dev.xiyo.pokerhole");
    
    @Test
    void domainShouldNotDependOnAdapter() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..core.domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter..");
        
        rule.check(classes);
    }
    
    @Test
    void domainShouldNotDependOnApplication() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..core.domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..core.application..");
        
        rule.check(classes);
    }
    
    @Test
    void domainShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..core.domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..");
        
        rule.check(classes);
    }
    
    @Test
    void applicationShouldNotDependOnAdapter() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..core.application..")
            .and().resideOutsideOfPackage("..core.application.port..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter..");
        
        rule.check(classes);
    }
    
    // Note: Layered architecture test is commented out as we are migrating from legacy code
    // This will be enabled once the migration to hexagonal architecture is complete
    /*
    @Test
    void layeredArchitectureShouldBeRespected() {
        ArchRule rule = layeredArchitecture()
            .consideringAllDependencies()
            
            .layer("Domain").definedBy("..core.domain..")
            .layer("Application").definedBy("..core.application..")
            .layer("Adapter").definedBy("..adapter..")
            .layer("UI").definedBy("..ui..")
            .layer("Config").definedBy("..configuration..")
            .layer("Server").definedBy("..server..")  // Legacy server layer
            
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter", "UI", "Config", "Server")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter", "UI", "Config", "Server");
        
        rule.check(classes);
    }
    */
}
