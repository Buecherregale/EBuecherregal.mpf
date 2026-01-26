import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    dependencies {
        implementation(projects.composeApp)

        implementation(compose.desktop.currentOs)
        implementation(libs.compose.ui.tooling.preview)
    }
}

compose.desktop {
    application {
        mainClass = "dev.buecherregale.ebook_reader.EBuecherregalKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.buecherregale.ebook_reader"
            packageVersion = "1.0.0"
        }
    }
}