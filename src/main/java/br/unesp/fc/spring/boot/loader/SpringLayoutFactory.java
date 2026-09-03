package br.unesp.fc.spring.boot.loader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.boot.loader.tools.CustomLoaderLayout;
import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;
import org.springframework.boot.loader.tools.Layouts;
import org.springframework.boot.loader.tools.LoaderClassesWriter;
import org.springframework.util.ClassUtils;

public class SpringLayoutFactory implements LayoutFactory {

    @Parameter
    private String launcherClassName = null;

    private static final String WINDOWS_SERVICE_JAR_LAUNCHER = SpringLayoutFactory.class.getPackageName() + ".WindowsServiceJarLauncher";
    private static final String WINDOWS_SERVICE_WAR_LAUNCHER = SpringLayoutFactory.class.getPackageName() + ".WindowsServiceWarLauncher";
    private static final String WINDOWS_SERVICE_LAUNCHER = SpringLayoutFactory.class.getPackageName() + ".WindowsServiceLauncher";
    private static final String WINDOWS_SERVICE_INSTALL = SpringLayoutFactory.class.getPackageName() + ".WindowsServiceInstall";
    private static final String WINDOWS_SERVICE_INSTALL_POM_INFO = SpringLayoutFactory.class.getPackageName() + ".WindowsServiceInstall$PomInfo";
    private static final String LAUNCHER_SPRING = "SPRING_DEFAULT";
    private static final String DEFAULT_LOADER_IMPLEMENTATION = "META-INF/loader/spring-boot-loader.jar";

    public String getLauncherClassName() {
        return launcherClassName;
    }

    public void setLauncherClassName(String launcherClassName) {
        this.launcherClassName = launcherClassName;
    }

    @Override
    public Layout getLayout(File file) {
		if (file == null) {
			throw new IllegalArgumentException("File must not be null");
		}
		String lowerCaseFileName = file.getName().toLowerCase(Locale.ENGLISH);
		if (lowerCaseFileName.endsWith(".jar")) {
			var layout = new Jar();
            layout.setLauncherClassName(launcherClassName);
            return layout;
		}
		if (lowerCaseFileName.endsWith(".war")) {
			var layout = new War();
            layout.setLauncherClassName(launcherClassName);
            return layout;
		}
        throw new IllegalStateException("Unable to deduce layout for '" + file + "'");
    }

    public static void writeLoadedClasses(LoaderClassesWriter writer) throws IOException {
        writer.writeLoaderClasses(DEFAULT_LOADER_IMPLEMENTATION);
        var classes = List.of(WINDOWS_SERVICE_JAR_LAUNCHER, WINDOWS_SERVICE_WAR_LAUNCHER,
                WINDOWS_SERVICE_LAUNCHER, WINDOWS_SERVICE_INSTALL, WINDOWS_SERVICE_INSTALL_POM_INFO);
        for (String klass : classes) {
            String path = ClassUtils.convertClassNameToResourcePath(klass) + ".class";
            writer.writeEntry(path, Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
        }
    }

    public static class Jar extends Layouts.Jar implements CustomLoaderLayout {

        @Parameter
        private String launcherClassName = null;

        public void setLauncherClassName(String launcherClassName) {
            this.launcherClassName = launcherClassName;
        }

        @Override
        public String getLauncherClassName() {
            if (launcherClassName == null) {
                return WINDOWS_SERVICE_JAR_LAUNCHER;
            }
            if (launcherClassName.equalsIgnoreCase(LAUNCHER_SPRING)) {
                return super.getLauncherClassName();
            }
            return launcherClassName;
        }

        @Override
        public void writeLoadedClasses(LoaderClassesWriter writer) throws IOException {
            SpringLayoutFactory.writeLoadedClasses(writer);
        }

    }

    public static class War extends Layouts.War implements CustomLoaderLayout {

        @Parameter
        private String launcherClassName = null;

        public void setLauncherClassName(String launcherClassName) {
            this.launcherClassName = launcherClassName;
        }

        @Override
        public String getLauncherClassName() {
            if (launcherClassName == null) {
                return WINDOWS_SERVICE_WAR_LAUNCHER;
            }
            if (launcherClassName.equalsIgnoreCase(LAUNCHER_SPRING)) {
                return super.getLauncherClassName();
            }
            return launcherClassName;
        }

        @Override
        public void writeLoadedClasses(LoaderClassesWriter writer) throws IOException {
            SpringLayoutFactory.writeLoadedClasses(writer);
        }

    }

}
