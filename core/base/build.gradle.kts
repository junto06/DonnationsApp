plugins {
    alias(libs.plugins.donnations.jvm.library)
    // Test doubles shared with other modules via testFixtures(projects.core.base).
    `java-test-fixtures`
}

dependencies {
    testFixturesImplementation(libs.kotlinx.coroutines.core)
}
