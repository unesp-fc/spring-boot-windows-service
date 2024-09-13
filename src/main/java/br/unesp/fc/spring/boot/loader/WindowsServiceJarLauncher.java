package br.unesp.fc.spring.boot.loader;

import java.util.Arrays;
import org.springframework.boot.loader.launch.JarLauncher;

public class WindowsServiceJarLauncher extends JarLauncher {

    private static final WindowsServiceLauncher windowsServiceLauncher = new WindowsServiceLauncher();

    public WindowsServiceJarLauncher() throws Exception {
    }

    @Override
    protected void launch(ClassLoader classLoader, String mainClassName, String[] args) throws Exception {
        windowsServiceLauncher.start(classLoader, mainClassName, args);
    }

    public static void start(String[] args) throws Exception {
        // process service start function
        new WindowsServiceJarLauncher().launch(args);
    }

    public static void stop(String[] args) throws Exception {
        // process service stop function
        windowsServiceLauncher.stop(args);
    }

    public static void main(String args[]) throws Exception {
        if (args.length == 0) {
            WindowsServiceInstall.printHelp();
            return;
        }
        String option = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);
        switch (option) {
            case "run":
                JarLauncher.main(args);
                break;
            case "install":
                System.exit(WindowsServiceInstall.install(WindowsServiceJarLauncher.class, args));
                break;
            case "delete":
                System.exit(WindowsServiceInstall.delete(args));
                break;
            default:
                WindowsServiceInstall.printHelp();
        }
    }

}
