package com.podo;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = PodoApplication.class)
public class PackageDependencyTests {

    private static final String CREW = "..modules.crew..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    @ArchTest
    ArchRule crewPackageRule = classes().that().resideInAPackage("..modules.crew..")
            .should().onlyBeAccessed().byClassesThat().resideInAnyPackage(CREW, EVENT);

    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage(CREW)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(CREW, EVENT);

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(CREW, ACCOUNT, EVENT);

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, ACCOUNT);

    @ArchTest
    ArchRule cycleCheck = slices().matching("com.podo.modules.(*)..")
            .should().beFreeOfCycles();
}
