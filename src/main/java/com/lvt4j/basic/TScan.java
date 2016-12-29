package com.lvt4j.basic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

/**
 * java包扫描类<br>
 * 山寨自org.apache.ibatis.io.ResolverUtil
 * @author LV
 *
 */
public class TScan {

    /** The magic header that indicates a JAR (ZIP) file. */
    private static final byte[] JAR_MAGIC = {'P', 'K', 3, 4};
    
    /** Regular expression that matches a Java identifier. */
    private static final Pattern JAVA_IDENTIFIER_PATTERN = Pattern
                    .compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");
    
    public static List<Class<?>> scanClass(String packageName) throws IOException{
        String path = packagePath(packageName);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<Class<?>> classes = new LinkedList<Class<?>>();
        List<URL> urls = Collections.list(classLoader.getResources(path));
        for (URL url : urls) {
            List<String> children = listClassResources(url, path);
            for (String child : children) {
                Class<?> cls = loadClass(classLoader, child);
                if (cls==null) continue;
                classes.add(cls);
            }
        }
        return classes;
    }
    
    /**
     * Recursively list all resources under the given URL that appear to define a Java class.
     * Matching resources will have a name that ends in ".class" and have a relative path such that
     * each segment of the path is a valid Java identifier. The resource paths returned will be
     * relative to the URL and begin with the specified path.
     *
     * @param url  The URL of the parent resource to search.
     * @param path The path with which each matching resource path must begin, relative to the URL.
     * @return A list of matching resources. The list may be empty.
     * @throws IOException
     */
    private static List<String> listClassResources(URL url, String path) throws IOException {
        InputStream is = null;
        try {
            List<String> resources = new ArrayList<String>();

            // First, try to find the URL of a JAR file containing the requested resource. If a JAR
            // file is found, then we'll list child resources by reading the JAR.
            URL jarUrl = findJarForResource(url, path);
            if (jarUrl != null) {
                is = jarUrl.openStream();
                resources = listClassResources(new JarInputStream(is), path);
            } else {
                List<String> children = new ArrayList<String>();
                try {
                    if (isJar(url)) {
                        // Some versions of JBoss VFS might give a JAR stream even if the resource
                        // referenced by the URL isn't actually a JAR
                        is = url.openStream();
                        JarInputStream jarInput = new JarInputStream(is);
                        for (JarEntry entry; (entry=jarInput.getNextJarEntry())!=null; ) {
                            if (isRelevantResource(entry.getName()))
                                children.add(entry.getName());
                        }
                        jarInput.close();
                    } else {
                        // Some servlet containers allow reading from "directory" resources like a
                        // text file, listing the child resources one per line.
                        is = url.openStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        for (String line; (line = reader.readLine()) != null;) {
                            if (isRelevantResource(line))
                                children.add(line);
                        }
                    }
                } catch (FileNotFoundException e) {
                    // For file URLs the openStream() call might fail, depending on the servlet
                    // container, because directories can't be opened for reading. If that happens,
                    // then list the directory directly instead.
                    if ("file".equals(url.getProtocol())) {
                        File file = new File(url.getFile());
                        if (file.isDirectory()) {
                            children = Arrays.asList(file.list(new FilenameFilter() {
                                public boolean accept(File dir, String name) {
                                    return isRelevantResource(name);
                                }
                            }));
                        }
                    } else {
                        // No idea where the exception came from so rethrow it
                        throw e;
                    }
                }

                // The URL prefix to use when recursively listing child resources
                String prefix = url.toExternalForm();
                if (!prefix.endsWith("/")) prefix = prefix + "/";

                // Iterate over each immediate child, adding classes and recursing into directories
                for (String child : children) {
                    String resourcePath = path + "/" + child;
                    if (child.endsWith(".class")) {
                        resources.add(resourcePath);
                    } else {
                        URL childUrl = new URL(prefix + child);
                        resources.addAll(listClassResources(childUrl, resourcePath));
                    }
                }
            }
            return resources;
        } finally {
            try {
                is.close();
            } catch (Exception ignore) {}
        }
    }
    
    /**
     * List the names of the entries in the given {@link JarInputStream} that begin with the
     * specified {@code path}. Entries will match with or without a leading slash.
     *
     * @param jar  The JAR input stream
     * @param path The leading path to match
     * @return The names of all the matching entries
     * @throws IOException
     */
    private static List<String> listClassResources(JarInputStream jar, String path) throws IOException {
        // Include the leading and trailing slash when matching names
        if (!path.startsWith("/")) path = "/" + path;
        if (!path.endsWith("/")) path = path + "/";

        // Iterate over the entries and collect those that begin with the requested path
        List<String> resources = new ArrayList<String>();
        for (JarEntry entry; (entry=jar.getNextJarEntry())!=null; ) {
            if (entry.isDirectory()) continue;
            // Add leading slash if it's missing
            String name = entry.getName();
            if (!name.startsWith("/")) name = "/" + name;

            // Check file name
            if (name.endsWith(".class") && name.startsWith(path))
                resources.add(name.substring(1)); // Trim leading slash
        }
        return resources;
    }
    
    /**
     * Attempts to deconstruct the given URL to find a JAR file containing the resource referenced
     * by the URL. That is, assuming the URL references a JAR entry, this method will return a URL
     * that references the JAR file containing the entry. If the JAR cannot be located, then this
     * method returns null.
     *
     * @param url  The URL of the JAR entry.
     * @param path The path by which the URL was requested from the class loader.
     * @return The URL of the JAR file, if one is found. Null if not.
     * @throws MalformedURLException
     */
    private static URL findJarForResource(URL url, String path) throws MalformedURLException {
        // If the file part of the URL is itself a URL, then that URL probably points to the JAR
        try {
            for (; ;) {
                url = new URL(url.getFile());
            }
        } catch (MalformedURLException e) {
            // This will happen at some point and serves a break in the loop
        }
        // Look for the .jar extension and chop off everything after that
        StringBuilder jarUrl = new StringBuilder(url.toExternalForm());
        int index = jarUrl.lastIndexOf(".jar");
        if (index<0) return null;
        jarUrl.setLength(index + 4);
        // Try to open and test it
        try {
            URL testUrl = new URL(jarUrl.toString());
            if (isJar(testUrl)) return testUrl;
            // WebLogic fix: check if the URL's file exists in the filesystem.
            jarUrl.replace(0, jarUrl.length(), testUrl.getFile());
            File file = new File(jarUrl.toString());

            // File name might be URL-encoded
            // if (!file.exists()) {
            //     file = new File(StringUtil.urlDecode(jarUrl.toString()));
            // }

            if (file.exists()) {
                testUrl = file.toURI().toURL();
                if (isJar(testUrl)) return testUrl;
            }
        }
        catch (MalformedURLException ignore) {}
        return null;
    }
    
    /**
     * Returns true if the resource located at the given URL is a JAR file.
     *
     * @param url The URL of the resource to test.
     */
    private static boolean isJar(URL url) {
            return isJar(url, new byte[JAR_MAGIC.length]);
    }

    /**
     * Returns true if the resource located at the given URL is a JAR file.
     *
     * @param url   The URL of the resource to test.
     * @param buffer A buffer into which the first few bytes of the resource are read. The buffer
     *               must be at least the size of {@link #JAR_MAGIC}. (The same buffer may be reused
     *               for multiple calls as an optimization.)
     */
    private static  boolean isJar(URL url, byte[] buffer) {
        InputStream is = null;
        try {
            is = url.openStream();
            is.read(buffer, 0, JAR_MAGIC.length);
            if (Arrays.equals(buffer, JAR_MAGIC)) return true;
        } catch (Exception e) {
            // Failure to read the stream means this is not a JAR
        } finally {
            try {
                if (is!=null) is.close();
            } catch (Exception ignore) {}
        }
        return false;
    }
    
    /**
     * Returns true if the name of a resource (file or directory) is one that matters in the search
     * for classes. Relevant resources would be class files themselves (file names that end with
     * ".class") and directories that might be a Java package name segment (java identifiers).
     *
     * @param resourceName The resource name, without path information
     */
    private static boolean isRelevantResource(String resourceName) {
        return resourceName!=null
                && (resourceName.endsWith(".class")
                || JAVA_IDENTIFIER_PATTERN.matcher(resourceName).matches());
    }
    
    private static Class<?> loadClass(ClassLoader classLoader, String fqn) {
        String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
        try {
            return classLoader.loadClass(externalName);
        } catch (Throwable ignore) {}
        return null;
    }
    
    private static String packagePath(String packageName) {
        return packageName==null?null:packageName.replace('.', '/');
    }
    
}
