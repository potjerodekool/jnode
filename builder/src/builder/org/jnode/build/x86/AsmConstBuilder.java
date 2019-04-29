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
 
package org.jnode.build.x86;

import java.io.File;

import org.jnode.build.AbstractAsmConstBuilder;
import org.jnode.vm.BaseVmArchitecture;
import org.jnode.vm.x86.VmX86Architecture32;
import org.jnode.vm.x86.VmX86Architecture64;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AsmConstBuilder extends AbstractAsmConstBuilder {

    private int bits = 32;

    private BaseVmArchitecture arch;

    protected BaseVmArchitecture getArchitecture() {
        if (arch == null) {
            switch (bits) {
                case 32:
                    arch = new VmX86Architecture32();
                    break;
                case 64:
                    arch = new VmX86Architecture64();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid bits " + bits);
            }
        }
        return arch;
    }

    public final int getBits() {
        return bits;
    }

    public final void setBits(int bits) {
        if ((bits != 32) && (bits != 64)) {
            throw new IllegalArgumentException("Invalid bits " + bits);
        }
        this.bits = bits;
    }
    
    public static void main(String[] args) {
    	AsmConstBuilder builder = new AsmConstBuilder();
    	
    	String option = null;
    	
    	for (String arg : args) {
    		if (arg.startsWith("-")) {
    			option = arg.substring(1);
    		} else {
    			String value = arg;
    			
    			if ("destfile".contentEquals(option)) {
    				builder.setDestFile(new File(value));
    			} else if ("bits".equals(option)) {
    				builder.setBits(Integer.parseInt(value));
    			} else if ("classesURL".contentEquals(option)) {
    				builder.setClassesURL(value);
    			} else if ("class".equals(option)) {
    				ClassName className = new ClassName();
    				className.setClassName(value);
    				builder.addClass(className);
    			} else if ("classstatic".equals(option)) {
    				ClassName className = new ClassName();
    				className.setClassName(value);
    				className.setStatic(true);
    				builder.addClass(className);
    			}
    		}
    	}
    	
    	
    	    	
    	builder.execute();
    }
}
