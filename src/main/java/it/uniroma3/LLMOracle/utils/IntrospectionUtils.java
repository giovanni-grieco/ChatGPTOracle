package it.uniroma3.LLMOracle.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class IntrospectionUtils {

    private static Boolean isJarExecution = null;

    public static boolean checkJarExecution() {
        if (isJarExecution == null) {
            isJarExecution = IntrospectionUtils.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm().endsWith(".jar") || IntrospectionUtils.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm().endsWith(".exe");
        }
        return isJarExecution;
    }


    public static List<Class<?>> getClassesInPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        if (checkJarExecution()) {
            try {
                String jarPath = IntrospectionUtils.class.getProtectionDomain().getCodeSource().getLocation().toString().replace("file:/", "");
                System.out.println(jarPath);
                JarFile jarFile = new JarFile(jarPath);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (entryName.startsWith(packageName.replace('.', '/')) && entryName.endsWith(".class")) {
                        String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                        classes.add(Class.forName(className));
                    }
                }
                jarFile.close();
            } catch (Exception e) {
                throw new RuntimeException("Error while reading jar file: " + e.getMessage());
            }
        } else {
            String packagePath = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL packageUrl = classLoader.getResource(packagePath);
            if (packageUrl != null) {
                File packageDirectory = new File(packageUrl.getFile());
                if (packageDirectory.exists()) {
                    String[] files = packageDirectory.list();
                    assert files != null;
                    for (String fileName : files) {
                        if (fileName.endsWith(".class")) {
                            try {
                                String className = packageName + '.' + fileName.substring(0, fileName.length() - 6);
                                Class<?> clazz = Class.forName(className);
                                classes.add(clazz);
                            } catch (ClassNotFoundException e) {
                                // Gestisci l'eccezione in base alle tue esigenze
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    // Gestisci l'assenza del package directory
                }
            } else {
                // Gestisci l'assenza dell'URL del package
            }
        }
        return classes;
    }
}
