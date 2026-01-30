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
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
                TargetFormat.AppImage
            )
            packageName = "dev.buecherregale.ebook_reader"
            packageVersion = libs.versions.projectVersion.get()

            macOS {
                iconFile.set(project.file("resources/icons/icon_colored_256x256.icns"))
            }
            windows {
                iconFile.set(project.file("resources/icons/icon_colored_256x256.ico"))
            }
            linux {
                iconFile.set(project.file("resources/icons/icon_colored_256x256.png"))
            }
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
