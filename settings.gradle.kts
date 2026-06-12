pluginManagement {
    repositories {
        google(); mavenCentral(); gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories { google(); mavenCentral() }
}
rootProject.name = "NyumbaHub"
include(
    ":app",
    ":core:ui", ":core:network", ":core:data", ":core:domain",
    ":feature:auth", ":feature:listings", ":feature:search",
    ":feature:post", ":feature:chat", ":feature:subscription", ":feature:profile"
)
include(":feature:motors")
