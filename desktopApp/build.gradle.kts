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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.AppImage)
            packageName = "dev.buecherregale.ebook_reader"
            packageVersion = "1.0.0"
        }
        buildTypes {
            release {
                proguard {
                    configurationFiles.from(
                        project.file("desktop-proguard-rules.pro")
                    )
                }
            }
        }

    }
}
