package it.uniroma3.LLMOracle.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IntrospectionUtils {
    public static List<Class<?>> getClassesInPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
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
        return classes;
    }
}
