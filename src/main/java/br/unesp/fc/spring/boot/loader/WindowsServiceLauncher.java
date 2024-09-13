package br.unesp.fc.spring.boot.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WindowsServiceLauncher {

    private volatile boolean active = true;

    private Method stopMethod;

    protected void start(ClassLoader classLoader, String mainClassName, String[] args) throws Exception {
		Thread.currentThread().setContextClassLoader(classLoader);
		Class<?> mainClass = Class.forName(mainClassName, false, classLoader);
		Method startMethod = mainClass.getDeclaredMethod("start", String[].class);
        stopMethod = mainClass.getDeclaredMethod("stop", String[].class);
		startMethod.setAccessible(true);
        stopMethod.setAccessible(true);
		startMethod.invoke(null, new Object[] { args });
        // Start thread should not return while service is active
        synchronized (this) {
            while (active) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public synchronized void stop(String[] args) {
        try {
            active = false;
            stopMethod.invoke(null, new Object[]{ args });
        } catch (IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(WindowsServiceJarLauncher.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            notifyAll();
        }
    }

}
