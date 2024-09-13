package br.unesp.fc.spring.boot.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class WindowsServiceInstall {

    private static Logger logger = Logger.getLogger(WindowsServiceInstall.class.getName());

    private static class PomInfo {
        final String name;
        final String description;

        public PomInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public static void printHelp() {
        System.out.println(""
                + "Spring Windows Service:\n"
                + "\n"
                + "Commands:\n"
                + "\n"
                + "run [ARG]...            Run application\n"
                + "install [ARG]...        Install service via procrun\n"
                + "delete [ARG]...         Delete service via procrun\n"
                + "\n"
                + "Install and Delete commands use prunsrv.exe. All arguments are passed to it."
        );
    }

    public static int install(Class launcher, String args[]) throws IOException, InterruptedException {
        if (!Paths.get("prunsrv.exe").toFile().exists()) {
            throw new RuntimeException("prunsrv.exe not found");
        }
        final Path currentDir = Paths.get("").toAbsolutePath();
        logger.log(Level.INFO, "CurrentDir={0}", currentDir);
        HashSet<String> options = new HashSet<>();
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add("prunsrv.exe");
        parameters.add("install");
        int index = 0;
        if (args.length > 0 && !args[0].startsWith("--")) {
            parameters.add(args[0]);
            index = 1;
        } else {
            parameters.add(getServiceName());
        }
        boolean skipNext = false;
        for (; index < args.length; index++) {
            if (!args[index].startsWith("--") && !args[index].startsWith("++") && !skipNext) {
                throw new RuntimeException("Invalid arg: " + args[index]);
            }
            skipNext = !skipNext && !args[index].contains("=");
            options.add(args[index].substring(2));
            parameters.add(args[index]);
        }
        Path javaHome = Paths.get(System.getProperty("java.home"));
        if (javaHome.startsWith(currentDir)) {
            javaHome = currentDir.relativize(javaHome);
        }
        if (!options.contains("Jvm")) {
            Path jvm = javaHome.resolve("bin/server/jvm.dll");
            if (jvm.toFile().exists()) {
                logger.log(Level.INFO, "Jvm={0}", jvm);
                parameters.add("--Jvm=" + jvm);
            } else {
                throw new RuntimeException("Unable to find jvm.dll");
            }
        }
        Path packagePath = getPackagePath();
        if (packagePath.startsWith(currentDir)) {
            packagePath = currentDir.relativize(packagePath);
        }
        logger.log(Level.INFO, "PackagePath={0}", packagePath);
        PomInfo pomInfo = getPomInfo();
        if (!options.contains("DisplayName")) {
            if (pomInfo.name != null) {
                logger.log(Level.INFO, "DisplayName={0}", pomInfo.name);
                parameters.add("--DisplayName=" + pomInfo.name);
            }
        }
        if (!options.contains("Description")) {
            if (pomInfo.description != null) {
                logger.log(Level.INFO, "Description={0}", pomInfo.description);
                parameters.add("--Description=" + pomInfo.description);
            }
        }
        if (!options.contains("Startup")) {
            parameters.add("--Startup=auto");
        }
        if (!options.contains("StdOutput")) {
            parameters.add("--StdOutput=auto");
        }
        if (!options.contains("StdError")) {
            parameters.add("--StdError=auto");
        }
        if (!options.contains("LogPath")) {
            // LogPath don't work if relative
            Path log = Paths.get("logs").toAbsolutePath();
            logger.log(Level.INFO, "LogPath={0}", log);
            parameters.add("--LogPath=" + log.toString());
            log.toFile().mkdirs();
        }
        parameters.add("--Classpath=" + packagePath.getFileName().toString());
        parameters.add("--StartMode=jvm");
        parameters.add("--StartClass=" + launcher.getCanonicalName());
        parameters.add("--StartMethod=start");
        parameters.add("--StopMode=jvm");
        parameters.add("--StopClass=" + launcher.getCanonicalName());
        parameters.add("--StopMethod=stop");
        Process process = new ProcessBuilder(parameters)
            .inheritIO()
            .start();
        return process.waitFor();
    }

    public static int delete(String args[]) throws IOException, InterruptedException {
        if (!Paths.get("prunsrv.exe").toFile().exists()) {
            throw new RuntimeException("prunsrv.exe not found");
        }
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add("prunsrv.exe");
        parameters.add("delete");
        if (args.length > 0) {
            parameters.add(args[0]);
        } else {
            parameters.add(getServiceName());
        }
        Process process = new ProcessBuilder(parameters)
            .inheritIO()
            .start();
        return process.waitFor();
    }

    private static Path getPackagePath() {
        String className = convertClassNameToResourcePath(WindowsServiceInstall.class.getName()) + ".class";
        String path = Thread.currentThread().getContextClassLoader().getResource(className).getPath();
        path = path.replaceFirst("^file:/", "");
        path = path.split("!", 2)[0];
        path = path.replace('/', File.separatorChar);
        return Paths.get(path);
    }

    private static String getServiceName() {
        String filename = getPackagePath().getFileName().toString();
        return filename.substring(0, filename.length() - 4);
    }

    private static PomInfo getPomInfo() {
        try {
            String name = null;
            String description = null;
            String className = convertClassNameToResourcePath(WindowsServiceInstall.class.getName()) + ".class";
            String path = Thread.currentThread().getContextClassLoader().getResource(className).getPath();
            if (path.lastIndexOf(':') >= 0) {
                var split = path.split(":");
                path = split[split.length - 1];
            }
            if (path.indexOf('!') >= 0) {
                path = path.split("!", 2)[0];
            }
            JarFile jar = new JarFile(path);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith("pom.xml")) {
                    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(entry.getName());
                    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
                    XMLEventReader reader = xmlInputFactory.createXMLEventReader(is);
                    try {
                        while (reader.hasNext()) {
                            XMLEvent event = reader.nextEvent();
                            if (event.isStartElement()) {
                                StartElement element = event.asStartElement();
                                if (element.getName().getLocalPart().equals("name")) {
                                    event = reader.nextEvent();
                                    name = event.asCharacters().getData();
                                } else if (element.getName().getLocalPart().equals("description")) {
                                    event = reader.nextEvent();
                                    description = event.asCharacters().getData();
                                }
                            }
                        }
                        return new PomInfo(name, description);
                    } finally {
                        reader.close();
                    }
                }
            }
        } catch (Exception ex) {
        }
        return null;
    }

    private static String convertClassNameToResourcePath(String className) {
        return className.replace('.', '/');
    }

}
