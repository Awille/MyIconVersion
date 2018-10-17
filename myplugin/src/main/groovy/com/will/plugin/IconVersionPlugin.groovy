package com.will.plugin

import com.android.build.gradle.AppPlugin
import com.android.tools.r8.utils.AndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ProcessAndroidResources


import static com.will.plugin.IconUtils.addTextToImage
import static com.will.plugin.IconUtils.findIcons

class IconVersionPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin)) {
            throw new IllegalStateException("'android' plugin required.")
        }

        //在工程的extension容器当中，
        IconVersionConfig config = project.extensions.create("iconVersionConfig", IconVersionConfig)

        def log = project.logger

        project.android.applicationVariants.all { BaseVariant variant ->


            //println("variant folder "+ variant.getDirName()+"xxxx:"+variant.sourceSets +"ccccc  "+variant.getOutputs())

            // release版本不打标记
            if (!(config.buildTypes.contains(variant.buildType.name) || variant.buildType.debuggable) ) {
                log.info "IconVersionPlugin. Skipping variant: $variant.name"
                println("Skipping variant: $variant.name")
                return
            }
            println("variant: $variant.name")

            def lines = []
            if (config.shouldDisplayBuildName) {
                lines.push(variant.flavorName + " " + variant.buildType.name)
                println("Buildname :  "+"$variant.flavorName + \" \" + $variant.buildType.name")
            }
            if (config.shouldDisplayVersionName) {
                lines.push(variant.versionName)
            }
            if (config.shouldDisplayVersionCode) {
                lines.push(String.valueOf(variant.versionCode))
            }

            println "IconVersionPlugin. Processing variant: $variant.name"
            variant.outputs.each { BaseVariantOutput output ->
                output.processResources.doFirst { ProcessAndroidResources task ->
                    variant.outputs.each { BaseVariantOutput variantOutput ->
                        File manifest = new File(output.processManifest.manifestOutputDirectory, "AndroidManifest.xml")

                        println(output.processManifest.manifestOutputDirectory)//输出

                        ArrayList<File> resDirs = new ArrayList<>()
                        variant.sourceSets.forEach { set ->
                            println("setdir name:" + set.getResDirectories().toString())
                            set.getResDirectories().forEach { resDir ->
                                if (resDir.exists()) {
                                    resDirs.add(resDir)
                                }
                            }
                        }

                        resDirs.each { File resDir ->
                            println "IconVersionPlugin. Looking for icons in dir: ${resDir}"

                            findIcons(resDir, manifest).each { File icon ->
                                log.info "Adding build information to: " + icon.absolutePath

                                addTextToImage(icon, config, *lines)
                            }
                        }
                    }
                }
            }
        }
    }
}
