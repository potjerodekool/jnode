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
 
package org.jnode.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.jnode.build.loader.ClassDefinition;
import org.jnode.build.loader.FieldDefinition;
import org.jnode.build.loader.VMAsmClassLoader;
import org.jnode.vm.BaseVmArchitecture;
import org.jnode.vm.VmImpl;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.facade.Vm;

/**
 * TODO describe the class.
 *
 * @author epr
 */
public abstract class AbstractAsmConstBuilder {

    private File destFile;
    private String classesURL;
    private ArrayList<ClassName> classes = new ArrayList<ClassName>();

    /**
     * Execute this task.
     *
     * @throws BuildException
     */
    public void execute() throws BuildException {
        try {
            doExecute();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new BuildException(ex);
        }
    }

    /**
     * Execute this task.
     *
     * @throws BuildException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    private void doExecute()
            throws BuildException, ClassNotFoundException, IllegalAccessException, IOException, InstantiationException {
    	doExecuteV2();
    }
    
    private void doExecuteV2() 
    		throws BuildException, ClassNotFoundException, IllegalAccessException, IOException, InstantiationException{
    	final BaseVmArchitecture arch = getArchitecture();
        final String[] urls = classesURL.split(",");
        final URL[] urla = new URL[urls.length];
        for (int i = 0; i < urls.length; i++) {
        	urla[i] = new URL(urls[i].trim());
        }

        final VMAsmClassLoader cl = new VMAsmClassLoader(urla, arch);
        
        long lastModified = 0;

        final FileWriter fw = new FileWriter(destFile);
        final PrintWriter out = new PrintWriter(fw);
        out.println("; " + destFile.getPath());
        out.println("; THIS file has been generated automatically on " + new Date());
        out.println();
        
        for (ClassName cn : classes) {
            lastModified = Math.max(lastModified, cl.findResource(cn.getClassFileName()).
                openConnection().getLastModified());
            out.println("; Constants for " + cn.getClassName());

            if (cn.isStatic()) {
                final ClassDefinition cls = cl.loadClass(cn.getClassName());
                final FieldDefinition fields[] = cls.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    final FieldDefinition f = fields[i];
                    if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
                        Object value = f.get(null);
                        if (value instanceof Number) {
                            String cname = cls.getName();
                            int idx = cname.lastIndexOf('.');
                            if (idx > 0) {
                                cname = cname.substring(idx + 1);
                            }
                            String name = cname + "_" + f.getName();
                            out.println(name + " equ " + value);
                        }
                    }
                }
                out.println();
            } else {

                out.println("; VmClass: " + cn.getClassName());
                
                final ClassDefinition cls = cl.loadClass(cn.getClassName());
                
                String cname = cls.getName().replace('/', '.');
                int idx = cname.lastIndexOf('.');
                if (idx > 0) {
                    cname = cname.substring(idx + 1);
                }
                
                final FieldDefinition[] fields = cls.getDeclaredFields();
                int cnt = fields.length;
                for (int i = 0; i < cnt; i++) {
                    final FieldDefinition f = fields[i];
                    if (!Modifier.isStatic(f.getModifiers())) {
                        String name = cname + "_" + f.getName().toUpperCase();
                        final int typeSize = getTypeSize(f, arch);
                        if (typeSize < 4) {
                            name = name + "_S" + typeSize;
                        }
                        name = name + "_OFS";
                        out.println(name + " equ " + f.getOffset());
                    }
                }
                // The size
                out.println(cname + "_SIZE equ " + cls.getObjectSize());
                //
                out.println();
            }
        }

        out.flush();
        fw.flush();
        out.close();
        fw.close();
        destFile.setLastModified(lastModified);
    }
    
    private int getTypeSize(final FieldDefinition field,
    		final BaseVmArchitecture arch) {
    	final String descriptor = field.getDescriptor();
    	
    	switch (descriptor.charAt(0)) {
	    	case 'Z': // Boolean
	        case 'B': // Byte
	            return 1;
	        case 'C': // Character
	        case 'S': // Short
	            return 2;
	        case 'I': // Integer
	        case 'F': // Float
	            return 4;
	        case 'L': // Object
	        case '[': // Array
	            return arch.getReferenceSize();
	        case 'J': // Long
	        case 'D': // Double
	            return 8;
	        default:
	            throw new IllegalArgumentException("Unknown type");
    	}
    }
    
    private void doExecuteClassic()
        throws BuildException, ClassNotFoundException, IllegalAccessException, IOException, InstantiationException {

        final BaseVmArchitecture arch = getArchitecture();
        String[] urls = classesURL.split(",");
        URL[] urla = new URL[urls.length];
        for (int i = 0; i < urls.length; i++)
            urla[i] = new URL(urls[i].trim());

        final VmSystemClassLoader cl = new VmSystemClassLoader(urla, arch);
        final Vm vm = new VmImpl("?", arch, cl.getSharedStatics(), false, cl, null);
        vm.toString(); // Just to avoid compiler warnings
        VmType.initializeForBootImage(cl);
        long lastModified = 0;

        FileWriter fw = new FileWriter(destFile);
        PrintWriter out = new PrintWriter(fw);
        out.println("; " + destFile.getPath());
        out.println("; THIS file has been generated automatically on " + new Date());
        out.println();

        for (ClassName cn : classes) {
            lastModified = Math.max(lastModified, cl.findResource(cn.getClassFileName()).
                openConnection().getLastModified());
            out.println("; Constants for " + cn.getClassName());

            if (cn.isStatic()) {
                Class<?> cls = Class.forName(cn.getClassName());
                Field fields[] = cls.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
                        Object value = f.get(null);
                        if (value instanceof Number) {
                            String cname = cls.getName();
                            int idx = cname.lastIndexOf('.');
                            if (idx > 0) {
                                cname = cname.substring(idx + 1);
                            }
                            String name = cname + "_" + f.getName();
                            out.println(name + " equ " + value);
                        }
                    }
                }
                out.println();
            } else {

                out.println("; VmClass: " + cn.getClassName());
                VmType<?> vmClass = cl.loadClass(cn.getClassName(), true);
                vmClass.link();
                String cname = vmClass.getName().replace('/', '.');
                int idx = cname.lastIndexOf('.');
                if (idx > 0) {
                    cname = cname.substring(idx + 1);
                }
                int cnt = vmClass.getNoDeclaredFields();
                for (int i = 0; i < cnt; i++) {
                    final VmField f = vmClass.getDeclaredField(i);
                    if (!f.isStatic()) {
                        final VmInstanceField instF = (VmInstanceField) f;
                        String name = cname + "_" + f.getName().toUpperCase();
                        if (f.getTypeSize() < 4) {
                            name = name + "_S" + f.getTypeSize();
                        }
                        name = name + "_OFS";
                        out.println(name + " equ " + instF.getOffset());
                    }
                }
                // The size
                if (vmClass instanceof VmNormalClass) {
                    final VmNormalClass<?> cls = (VmNormalClass<?>) vmClass;
                    out.println(cname + "_SIZE equ " + cls.getObjectSize());
                }
                //
                out.println();
            }
        }

        out.flush();
        fw.flush();
        out.close();
        fw.close();
        destFile.setLastModified(lastModified);
    }

    public void addClass(ClassName cn) {
        classes.add(cn);
    }

    /**
     * Returns the destFile.
     *
     * @return File
     */
    public File getDestFile() {
        return destFile;
    }

    /**
     * Sets the destFile.
     *
     * @param destFile The destFile to set
     */
    public void setDestFile(File destFile) {
        this.destFile = destFile;
    }

    public static class ClassName {
        private String className;
        private boolean _static = false;

        public ClassName() {
        }

        /**
         * Returns the className.
         *
         * @return String
         */
        public String getClassName() {
            return className;
        }

        /**
         * Sets the className.
         *
         * @param className The className to set
         */
        public void setClassName(String className) {
            this.className = className;
        }

        /**
         * Returns the _static.
         *
         * @return boolean
         */
        public boolean isStatic() {
            return _static;
        }

        /**
         * Sets the _static.
         *
         * @param _static The _static to set
         */
        public void setStatic(boolean _static) {
            this._static = _static;
        }

        public URL getURL(URL root) throws MalformedURLException {
            return new URL(root.toExternalForm() + "/" + className.replace('.', '/') + ".class");
        }

        public String getClassFileName() {
            return "/" + className.replace('.', '/') + ".class";
        }
    }

    /**
     * Returns the classesURL.
     *
     * @return URL
     */
    public String getClassesURL() {
        return classesURL;
    }

    /**
     * Sets the classesURL.
     *
     * @param classesURL The classesURL to set
     */
    public void setClassesURL(String classesURL) {
        this.classesURL = classesURL;
    }

    protected abstract BaseVmArchitecture getArchitecture();
}
