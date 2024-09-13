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
import org.springframework.boot.loader.tools.LoaderImplementation;
import org.springframework.util.ClassUtils;

public class SpringLayoutFactory implements LayoutFactory {

    @Parameter
    private String lancherClassName = null;

    private static final String WINDOWS_SERVICE_JAR_LAUNCHER = SpringLayoutFactory.class.getPackageName() + ".WindowsServiceJarLauncher";
    private static final String WINDOWS_SERVICE_WAR_LAUNCHER = SpringLayoutFactory.class.getPackageName() + ".WindowsServiceWarLauncher";
    private static final String WINDOWS_SERVICE_LAUNCHER = SpringLayoutFactory.class.getPackageName() + ".WindowsServiceLauncher";
    private static final String WINDOWS_SERVICE_INSTALL = SpringLayoutFactory.class.getPackageName() + ".WindowsServiceInstall";
    private static final String WINDOWS_SERVICE_INSTALL_POM_INFO = SpringLayoutFactory.class.getPackageName() + ".WindowsServiceInstall$PomInfo";
    private static final String LANCHER_SPRING = "SPRING_DEFAULT";

    public String getLancherClassName() {
        return lancherClassName;
    }

    public void setLancherClassName(String lancherClassName) {
        this.lancherClassName = lancherClassName;
    }

    @Override
    public Layout getLayout(File file) {
		if (file == null) {
			throw new IllegalArgumentException("File must not be null");
		}
		String lowerCaseFileName = file.getName().toLowerCase(Locale.ENGLISH);
		if (lowerCaseFileName.endsWith(".jar")) {
			var layout = new Jar();
            layout.setLancherClassName(lancherClassName);
            return layout;
		}
		if (lowerCaseFileName.endsWith(".war")) {
			var layout = new War();
            layout.setLancherClassName(lancherClassName);
            return layout;
		}
        throw new IllegalStateException("Unable to deduce layout for '" + file + "'");
    }

    public static void writeLoadedClasses(LoaderClassesWriter writer) throws IOException {
        writer.writeLoaderClasses(LoaderImplementation.DEFAULT);
        var classes = List.of(WINDOWS_SERVICE_JAR_LAUNCHER, WINDOWS_SERVICE_WAR_LAUNCHER,
                WINDOWS_SERVICE_LAUNCHER, WINDOWS_SERVICE_INSTALL, WINDOWS_SERVICE_INSTALL_POM_INFO);
        for (String klass : classes) {
            String path = ClassUtils.convertClassNameToResourcePath(klass) + ".class";
            writer.writeEntry(path, Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
        }
    }

    public static class Jar extends Layouts.Jar implements CustomLoaderLayout {

        @Parameter
        private String lancherClassName = null;

        public String getLancherClassName() {
            return lancherClassName;
        }

        public void setLancherClassName(String lancherClassName) {
            this.lancherClassName = lancherClassName;
        }

        @Override
        public String getLauncherClassName() {
            if (lancherClassName == null) {
                return WINDOWS_SERVICE_JAR_LAUNCHER;
            }
            if (lancherClassName.equalsIgnoreCase(LANCHER_SPRING)) {
                return super.getLauncherClassName();
            }
            return lancherClassName;
        }

        @Override
        public void writeLoadedClasses(LoaderClassesWriter writer) throws IOException {
            SpringLayoutFactory.writeLoadedClasses(writer);
        }

    }

    public static class War extends Layouts.War implements CustomLoaderLayout {

        @Parameter
        private String lancherClassName = null;

        public String getLancherClassName() {
            return lancherClassName;
        }

        public void setLancherClassName(String lancherClassName) {
            this.lancherClassName = lancherClassName;
        }

        @Override
        public String getLauncherClassName() {
            if (lancherClassName == null) {
                return WINDOWS_SERVICE_WAR_LAUNCHER;
            }
            if (lancherClassName.equalsIgnoreCase(LANCHER_SPRING)) {
                return super.getLauncherClassName();
            }
            return lancherClassName;
        }

        @Override
        public void writeLoadedClasses(LoaderClassesWriter writer) throws IOException {
            SpringLayoutFactory.writeLoadedClasses(writer);
        }

    }

}
