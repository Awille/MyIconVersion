package com.will.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class NewPlugin implements Plugin<Project>{


//冲突解决

    @Override
    void apply(Project project) {
        println("This is will's plugin")
        println("xxxxx")

    }
}