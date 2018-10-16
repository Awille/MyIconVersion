package com.will.plugin

import java.awt.Color

class IconVersionConfig {  //图标版本基本配置
    static DEFAULT = new IconVersionConfig(); //内部实例

    static TRANSPARENT = new Color(0,0,0,0);

    int fontSize = 12;  //图标文字大小

    String[] buildTypes = ["debug"] //debug 版本

    int verticalLinePadding = 4  //

    int[] backgroundOverlayCocor = [0, 0, 0, 136] //背景色


    int[] textColor =[255,255,255,255] //字体颜色

    boolean shouldDisplayBuildName = true;
    boolean shouldDisplayVersionCode = false;
    boolean shouldDisplayVersionName = true;



    public Color getBackgroundOverlayColor(){
        return intArrayToColor(backgroundOverlayCocor) ?: TRANSPARENT;
    }

    public Color getTextColor(){
        return intArrayToColor(textColor) ?: TRANSPARENT;
    }



    private static Color intArrayToColor(int[] colorMatrix){
        if(colorMatrix == null || colorMatrix.length != 4){
            return null;
        }else{
            return new Color(colorMatrix[0], colorMatrix[1], colorMatrix[2], colorMatrix[3]);
        }
    }



}