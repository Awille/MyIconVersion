package com.will.plugin

import com.google.common.collect.Lists
import groovy.io.FileType

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List

import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON


class IconUtils {



    static {
        // We want our font to come out looking pretty
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Fix for Android Studio issue: Could not find class: apple.awt.CGraphicsEnvironment
        try {
            Class.forName(System.getProperty("java.awt.graphicsenv"))
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] java.awt.graphicsenv: " + e)
            System.setProperty("java.awt.graphicsenv", "sun.awt.CGraphicsEnvironment")
        }

        //  Fix for AS issue: Toolkit not found: apple.awt.CToolkit
        try {
            Class.forName(System.getProperty("awt.toolkit"))
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] awt.toolkit: " + e)
            System.setProperty("awt.toolkit", "sun.lwawt.macosx.LWCToolkit")
        }
    }



    /**
     * Icon name to search for in the app drawable folders
     * if none can be found in the app manifest
     */
    static final String DEFAULT_ICON_NAME = "ic_launcher";

    /**
     * Retrieve the app icon from the application manifest
     *
     * @param manifestFile The file pointing to the AndroidManifest
     * @return The icon name specified in the {@code <application/ >} node
     */
    static String getIconName(File manifestFile) { //找到manifest文件
        if (manifestFile == null || manifestFile.isDirectory() || !manifestFile.exists()) {
            return null;
        }

        def manifestXml = new XmlSlurper().parse(manifestFile)
        def fileName = manifestXml?.application?.@'android:icon'?.text()
        return fileName ? fileName?.split("/")[1] : null
    }

    /**
     * Retrieve the round app icon from the application manifest
     *
     * @param manifestFile The file pointing to the AndroidManifest
     * @return The round icon name specified in the {@code <application/ >} node
     */
    static String getRoundIconName(File manifestFile) {
        if (manifestFile == null || manifestFile.isDirectory() || !manifestFile.exists()) {
            return null;
        }

        def manifestXml = new XmlSlurper().parse(manifestFile)
        def fileName = manifestXml?.application?.@'android:roundIcon'?.text()
        return fileName ? fileName?.split("/")[1] : null
    }

    /**
     * Finds all icon files matching the icon specified in the given manifest.
     *
     * If no icon can be found in the manifest, a default of {@link IconUtils#DEFAULT_ICON_NAME} will be used
     */
    static List<File> findIcons(File where, File manifest) {
        List<File> result = Lists.newArrayList();

        final String iconName = getIconName(manifest) ?: DEFAULT_ICON_NAME
        findIconFiles(where, iconName, result)

        final String foregrondIconName = null
        where.eachDirMatch(~/^drawable.*|^mipmap.*/) { dir ->
            dir.eachFileMatch(FileType.FILES, ~".+.xml") { file ->
                def resXml = new XmlSlurper().parse(file)
                if (resXml.name() == "adaptive-icon") {
                    def fileName = resXml?.foreground?.@'android:drawable'?.text()
                    if (fileName != null) {
                        findIconFiles(where, fileName?.split("/")[1], result)
                    }
                }
            }
        }

        if (foregrondIconName != null) {
            findIconFiles(where, foregrondIconName, result)
        }

        final roundIconName = getRoundIconName(manifest)
        if (roundIconName != null) {
            findIconFiles(where, roundIconName, result)
        }

        return result
    }

    private static void findIconFiles(File where, String iconName, List<File> resultList) {
        where.eachDirMatch(~/^drawable.*|^mipmap.*/) { dir ->
            dir.eachFileMatch(FileType.FILES, ~"^${iconName}.png") { file ->
                resultList.add(file)
            }
        }
    }

    /**
     * Draws the given text over an image
     *
     * @param image The image file which will be written too
     * @param config The configuration which controls how the overlay will appear
     * @param lines The lines of text to be displayed
     */
    static void addTextToImage(File image, IconVersionConfig config = IconVersionConfig.DEFAULT, String... lines) {
        final BufferedImage bufferedImage = ImageIO.read(image);

        final Color backgroundOverlayColor = config.getBackgroundOverlayColor();
        final Color textColor = config.getTextColor();
        final int linePadding = config.verticalLinePadding;
        final int imgWidth = bufferedImage.width;
        final int imgHeight = bufferedImage.width;
        final int lineCount = lines.length;
        final int fontSize = imgHeight / 2 / lineCount
        //final int fontSize = config.fontSize
        final int totalLineHeight = (fontSize * lineCount) + ((linePadding + 1) * lineCount);

        GraphicsEnvironment.localGraphicsEnvironment.createGraphics(bufferedImage).with { g ->
            g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);

            // Draw our background overlay
            g.setColor(backgroundOverlayColor);
            g.fillRect(0, (int) ((imgHeight - totalLineHeight) / 2), imgWidth, totalLineHeight);

            // Draw each line of our text
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
            g.setColor(textColor)
            lines.reverse().eachWithIndex { String line, int i ->
                final int strWidth = g.getFontMetrics().stringWidth(line);

                int x = 0;
                if (imgWidth >= strWidth) {
                    x = ((imgWidth - strWidth) / 2);
                }

                int y = imgHeight - imgHeight / 4 - (fontSize * i) - ((i + 1) * linePadding);

                g.drawString(line, x, y);
            }
        }

        ImageIO.write(bufferedImage, "png", image);
    }
}
