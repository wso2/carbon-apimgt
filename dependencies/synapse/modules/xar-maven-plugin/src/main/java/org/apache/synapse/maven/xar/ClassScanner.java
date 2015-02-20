/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.maven.xar;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ClassScanner {
    private final File dir;
    private List<File> classpath = new LinkedList<File>();
    private List<ClassVisitor> visitors = new LinkedList<ClassVisitor>();
    private ClassLoader loader;
    
    public ClassScanner(File dir) {
        this.dir = dir;
        addToClasspath(dir);
    }
    
    public void addToClasspath(File file) {
        classpath.add(file);
    }
    
    public void addVisitor(ClassVisitor visitor) {
        visitors.add(visitor);
    }
    
    public void scan() throws ClassScannerException {
        if (visitors.isEmpty()) {
            return;
        }
        List<URL> urls = new ArrayList<URL>(classpath.size());
        for (File classpathEntry : classpath) {
            try {
                urls.add(classpathEntry.toURL());
            } catch (MalformedURLException e) {
                throw new ClassScannerException("Unable to build classpath", e);
            }
        }
        loader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
        try {
            for (ClassVisitor visitor : visitors) {
                visitor.init(loader);
            }
            scan(dir, null);
        } finally {
            loader = null;
        }
    }
    
    private void scan(File dir, String packageName) throws ClassScannerException {
        File[] children = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() || (file.isFile() && file.getName().endsWith(".class"));
            }
        });
        for (File child : children) {
            String name = child.getName();
            if (child.isDirectory()) {
                scan(child, packageName == null ? name : packageName + "." + name);
            } else {
                StringBuilder className = new StringBuilder();
                if (packageName != null) {
                    className.append(packageName).append('.');
                }
                className.append(name.substring(0, name.length()-6));
                Class<?> clazz;
                try {
                    clazz = loader.loadClass(className.toString());
                } catch (ClassNotFoundException e) {
                    throw new ClassScannerException("Unable to load class " + className, e);
                }
                for (ClassVisitor visitor : visitors) {
                    visitor.visit(clazz);
                }
            }
        }
    }
}
