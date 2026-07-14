package com.example.attendance;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.example.attendance";

    private final com.tngtech.archunit.core.domain.JavaClasses classes =
            new ClassFileImporter()
                    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .importPackages(BASE_PACKAGE);

    @Test
    @DisplayName("commonパッケージは他のドメインパッケージに依存しない")
    void common_shouldNotDependOnDomainPackages() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..common..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..department..",
                        "..employee..",
                        "..config..");

        rule.check(classes);
    }

    @Test
    @DisplayName("ドメインパッケージはconfigに依存しない")
    void domain_shouldNotDependOnConfig() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "..department..",
                        "..employee..")
                .should().dependOnClassesThat()
                .resideInAPackage("..config..");

        rule.check(classes);
    }
}
