package com.will.plugin

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project



class IconVersionPlugin implements Plugin<Project>{

    @Override
    void apply(Project target) {
        if(!target.plugins.hasPlugin(AppPlugin)){
            throw new IllegalStateException(
                    "android plugin required"
            )
        }

        //创建一个extension 让用户可以自己设定属性
        //extension是projet的容器
        IconVersionConfig config = target.extensions.create("iconVersionConfig",IconVersionConfig);






    }
}