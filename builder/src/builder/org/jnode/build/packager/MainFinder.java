/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.build.packager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Class for searching main methods in a jar.
 * 
 * @author fabien
 *
 */
public class MainFinder {
    
    /**
     * Search for the main classes in the jars/resources.
     * Starts by looking in the jars manifests and, if nothing is found,
     * then scans the jars/resources for main classes.  
     * 
     * @param userJar
     * @return the names of the main classes.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> searchMain(File userJar) throws FileNotFoundException, IOException {
        List<String> mainList = new ArrayList<String>();
        
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(userJar);
            
            // try to find the main class from the manifest
            Object value = null;
            
            // do we have a manifest ?
            if (jarFile.getManifest() != null) {
                value = jarFile.getManifest().getMainAttributes().get(Attributes.Name.MAIN_CLASS);
                if (value == null) {
                    String name = Attributes.Name.MAIN_CLASS.toString();
                    final Attributes attr = jarFile.getManifest().getAttributes(name);
                    
                    // we have a manifest but do we have a main class defined inside ?
                    if (attr != null) {
                        value = attr.get(Attributes.Name.MAIN_CLASS);
                    }
                }
            }
            
            if (value != null) {
                mainList.add(String.valueOf(value));
            } else {

                // scan the jar to find the main classes
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
                    final JarEntry entry = e.nextElement(); 
                    final String name = entry.getName();
                    InputStream is = null;
                    
                    try {
                        if (name.endsWith(".class")) {
                            String className = name.substring(0, name.length() - ".class".length());
                            className = className.replace('/', '.');

                            is = jarFile.getInputStream(entry);
                            if (isMainClass(is, entry, className)) {
                                mainList.add(className);
                            }
                        }
                    } catch (ClassNotFoundException cnfe) {
                        cnfe.printStackTrace();
                        // ignore
                    } catch (SecurityException se) {
                        se.printStackTrace();
                        // ignore
                    } catch (NoSuchMethodException nsme) {
                        // such error is expected for non-main classes => ignore
                    } catch (Throwable t) {
                        t.printStackTrace();
                        // ignore
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
            }
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }
        
        return mainList;
    }
    
    private static boolean isMainClass(InputStream classStream, JarEntry entry, String className) 
        throws ClassNotFoundException, SecurityException, NoSuchMethodException, IOException {
        ClassReader cr = new ClassReader(classStream);
        MainClassVisitor mcv = new MainClassVisitor(NullClassVisitor.INSTANCE);
        cr.accept(mcv, 0);        
        return mcv.hasMainMethod();
    }

    /**
     * Custom {@link ClassVisitor} used to parse a class from an InputStream. 
     * It helps finding a main class in a jar file. 
     * @author fabien
     *
     */
    static class MainClassVisitor extends ClassVisitor {
        private boolean mainMethod = false;
        
        public MainClassVisitor(ClassVisitor visitor) {
            super(Opcodes.ASM6, visitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        	if ("main".equals(name) && "([Ljava/lang/String;)V".equals(signature)) {
                mainMethod = true;
            }
            return null;
        }
        
        public boolean hasMainMethod() {
            return mainMethod;
        }
    }
    
    /**
     * ClassVisitor doing nothing but that's needed by MainClassVisitor constructor.
     * @author fabien
     *
     */
    static class NullClassVisitor extends ClassVisitor {

		private static final NullClassVisitor INSTANCE = new NullClassVisitor();
		
        public NullClassVisitor() {
			super(Opcodes.ASM6);
		}
    }
}
