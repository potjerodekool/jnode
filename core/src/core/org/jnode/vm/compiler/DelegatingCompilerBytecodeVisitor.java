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
 
package org.jnode.vm.compiler;

import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstDynamicMethodRef;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DelegatingCompilerBytecodeVisitor<T extends CompilerBytecodeVisitor>
    extends CompilerBytecodeVisitor {

    protected final T delegate;

    public DelegatingCompilerBytecodeVisitor(T delegate) {
        this.delegate = delegate;
    }

    public final T getDelegate() {
        return delegate;
    }

    /**
     * A try block is about to start
     */
    public void startTryBlock() {
        delegate.startTryBlock();
    }

    /**
     * A try block has finished
     */
    public void endTryBlock() {
        delegate.endTryBlock();
    }

    /**
     *
     */
    public void endBasicBlock() {
        delegate.endBasicBlock();
    }

    /**
     *
     */
    public void endInstruction() {
        delegate.endInstruction();
    }

    /**
     *
     */
    public void endMethod() {
        delegate.endMethod();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @param parser
     */
    public void setParser(BytecodeParser parser) {
        delegate.setParser(parser);
    }

    /**
     * @param bb
     */
    public void startBasicBlock(BasicBlock bb) {
        delegate.startBasicBlock(bb);
    }

    /**
     * @param address
     */
    public void startInstruction(int address) {
        delegate.startInstruction(address);
    }

    /**
     * @param method
     */
    public void startMethod(VmMethod method) {
    	log("");
    	log(method.getName());    	
        delegate.startMethod(method);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegate.toString();
    }

    /**
     *
     */
    public void visit_aaload() {
        delegate.visit_aaload();
        throwException();
    }

    /**
     *
     */
    public void visit_aastore() {
    	log("aastore");
        delegate.visit_aastore();
    }

    /**
     *
     */
    public void visit_aconst_null() {
        delegate.visit_aconst_null();
        throwException();
    }

    /**
     * @param index
     */
    public void visit_aload(int index) {
    	log("aload " + index);
        delegate.visit_aload(index);        
    }

    /**
     * @param clazz
     */
    public void visit_anewarray(VmConstClass clazz) {
    	log("anewarray " + clazz.getClassName());
        delegate.visit_anewarray(clazz);        
    }

    /**
     *
     */
    public void visit_areturn() {
        delegate.visit_areturn();
        throwException();
    }

    /**
     *
     */
    public void visit_arraylength() {
    	log("arraylength");
    	delegate.visit_arraylength();        
    }

    /**
     * @param index
     */
    public void visit_astore(int index) {
    	log("astore " + index);
        delegate.visit_astore(index);        
    }

    /**
     *
     */
    public void visit_athrow() {
        delegate.visit_athrow();
        throwException();
    }

    /**
     *
     */
    public void visit_baload() {
        delegate.visit_baload();
        throwException();
    }

    /**
     *
     */
    public void visit_bastore() {
        delegate.visit_bastore();
        throwException();
    }

    /**
     *
     */
    public void visit_caload() {
        delegate.visit_caload();
        throwException();
    }

    /**
     *
     */
    public void visit_castore() {
        delegate.visit_castore();
        throwException();
    }

    /**
     * @param clazz
     */
    public void visit_checkcast(VmConstClass clazz) {
        delegate.visit_checkcast(clazz);
        throwException();
    }

    /**
     *
     */
    public void visit_d2f() {
        delegate.visit_d2f();
        throwException();
    }

    /**
     *
     */
    public void visit_d2i() {
        delegate.visit_d2i();
        throwException();
    }

    /**
     *
     */
    public void visit_d2l() {
        delegate.visit_d2l();
        throwException();
    }

    /**
     *
     */
    public void visit_dadd() {
        delegate.visit_dadd();
        throwException();
    }

    /**
     *
     */
    public void visit_daload() {
        delegate.visit_daload();
        throwException();
    }

    /**
     *
     */
    public void visit_dastore() {
        delegate.visit_dastore();
        throwException();
    }

    /**
     *
     */
    public void visit_dcmpg() {
        delegate.visit_dcmpg();
        throwException();
    }

    /**
     *
     */
    public void visit_dcmpl() {
        delegate.visit_dcmpl();
        throwException();
    }

    /**
     * @param value
     */
    public void visit_dconst(double value) {
        delegate.visit_dconst(value);
        throwException();
    }

    /**
     *
     */
    public void visit_ddiv() {
        delegate.visit_ddiv();
        throwException();
    }

    /**
     * @param index
     */
    public void visit_dload(int index) {
        delegate.visit_dload(index);
        throwException();
    }

    /**
     *
     */
    public void visit_dmul() {
        delegate.visit_dmul();
        throwException();
    }

    /**
     *
     */
    public void visit_dneg() {
        delegate.visit_dneg();
        throwException();
    }

    /**
     *
     */
    public void visit_drem() {
        delegate.visit_drem();
        throwException();
    }

    /**
     *
     */
    public void visit_dreturn() {
        delegate.visit_dreturn();
        throwException();
    }

    /**
     * @param index
     */
    public void visit_dstore(int index) {
        delegate.visit_dstore(index);
        throwException();
    }

    /**
     *
     */
    public void visit_dsub() {
        delegate.visit_dsub();
        throwException();
    }

    /**
     *
     */
    public void visit_dup() {
    	log("dup");
        delegate.visit_dup();        
    }

    /**
     *
     */
    public void visit_dup_x1() {
        delegate.visit_dup_x1();
        throwException();
    }

    /**
     *
     */
    public void visit_dup_x2() {
        delegate.visit_dup_x2();
        throwException();
    }

    /**
     *
     */
    public void visit_dup2() {
    	log("dup2");
        delegate.visit_dup2();        
    }

    /**
     *
     */
    public void visit_dup2_x1() {
        delegate.visit_dup2_x1();
        throwException();
    }

    /**
     *
     */
    public void visit_dup2_x2() {
        delegate.visit_dup2_x2();
        throwException();
    }

    /**
     *
     */
    public void visit_f2d() {
        delegate.visit_f2d();
        throwException();
    }

    /**
     *
     */
    public void visit_f2i() {
        delegate.visit_f2i();
        throwException();
    }

    /**
     *
     */
    public void visit_f2l() {
        delegate.visit_f2l();
        throwException();
    }

    /**
     *
     */
    public void visit_fadd() {
        delegate.visit_fadd();
        throwException();
    }

    /**
     *
     */
    public void visit_faload() {
        delegate.visit_faload();
        throwException();
    }

    /**
     *
     */
    public void visit_fastore() {
        delegate.visit_fastore();
        throwException();
    }

    /**
     *
     */
    public void visit_fcmpg() {
        delegate.visit_fcmpg();
        throwException();
    }

    /**
     *
     */
    public void visit_fcmpl() {
        delegate.visit_fcmpl();
        throwException();
    }

    /**
     * @param value
     */
    public void visit_fconst(float value) {
        delegate.visit_fconst(value);
        throwException();
    }

    /**
     *
     */
    public void visit_fdiv() {
        delegate.visit_fdiv();
        throwException();
    }

    /**
     * @param index
     */
    public void visit_fload(int index) {
        delegate.visit_fload(index);
        throwException();
    }

    /**
     *
     */
    public void visit_fmul() {
        delegate.visit_fmul();
        throwException();
    }

    /**
     *
     */
    public void visit_fneg() {
        delegate.visit_fneg();
        throwException();
    }

    /**
     *
     */
    public void visit_frem() {
        delegate.visit_frem();
        throwException();
    }

    /**
     *
     */
    public void visit_freturn() {
        delegate.visit_freturn();
        throwException();
    }

    /**
     * @param index
     */
    public void visit_fstore(int index) {
        delegate.visit_fstore(index);
        throwException();
    }

    /**
     *
     */
    public void visit_fsub() {
        delegate.visit_fsub();
        throwException();
    }

    /**
     * @param fieldRef
     */
    public void visit_getfield(VmConstFieldRef fieldRef) {
        delegate.visit_getfield(fieldRef);
        throwException();
    }

    /**
     * @param fieldRef
     */
    public void visit_getstatic(VmConstFieldRef fieldRef) {
    	log("getstatic " + fieldRef.getClassName() + "." + fieldRef.getName());
        delegate.visit_getstatic(fieldRef);        
    }

    /**
     * @param address
     */
    public void visit_goto(int address) {
        delegate.visit_goto(address);
        throwException();
    }

    /**
     *
     */
    public void visit_i2b() {
        delegate.visit_i2b();
        throwException();
    }

    /**
     *
     */
    public void visit_i2c() {
        delegate.visit_i2c();
        throwException();
    }

    /**
     *
     */
    public void visit_i2d() {
        delegate.visit_i2d();
        throwException();
    }

    /**
     *
     */
    public void visit_i2f() {
        delegate.visit_i2f();
        throwException();
    }

    /**
     *
     */
    public void visit_i2l() {
        delegate.visit_i2l();
        throwException();
    }

    /**
     *
     */
    public void visit_i2s() {
        delegate.visit_i2s();
        throwException();
    }

    /**
     *
     */
    public void visit_iadd() {
        delegate.visit_iadd();
        throwException();
    }

    /**
     *
     */
    public void visit_iaload() {
        delegate.visit_iaload();
        throwException();
    }

    /**
     *
     */
    public void visit_iand() {
        delegate.visit_iand();
        throwException();
    }

    /**
     *
     */
    public void visit_iastore() {
        delegate.visit_iastore();
        throwException();
    }

    /**
     * @param value
     */
    public void visit_iconst(int value) {
    	log("iconst " + value);
        delegate.visit_iconst(value);                
    }

    /**
     *
     */
    public void visit_idiv() {
        delegate.visit_idiv();
        throwException();
    }

    /**
     * @param address
     */
    public void visit_if_acmpeq(int address) {
        delegate.visit_if_acmpeq(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_if_acmpne(int address) {
        delegate.visit_if_acmpne(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_if_icmpeq(int address) {
    	log("if_icmpeq " + address);
        delegate.visit_if_icmpeq(address);        
    }

    /**
     * @param address
     */
    public void visit_if_icmpge(int address) {
    	log("if_icmpge " + address);
        delegate.visit_if_icmpge(address);        
    }

    /**
     * @param address
     */
    public void visit_if_icmpgt(int address) {
        delegate.visit_if_icmpgt(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_if_icmple(int address) {
        delegate.visit_if_icmple(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_if_icmplt(int address) {
        delegate.visit_if_icmplt(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_if_icmpne(int address) {
        delegate.visit_if_icmpne(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_ifeq(int address) {
        delegate.visit_ifeq(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_ifge(int address) {
        delegate.visit_ifge(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_ifgt(int address) {
        delegate.visit_ifgt(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_ifle(int address) {
        delegate.visit_ifle(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_iflt(int address) {
        delegate.visit_iflt(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_ifne(int address) {
        delegate.visit_ifne(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_ifnonnull(int address) {
        delegate.visit_ifnonnull(address);
        throwException();
    }

    /**
     * @param address
     */
    public void visit_ifnull(int address) {
    	log("ifnull " + address);
        delegate.visit_ifnull(address);        
    }

    /**
     * @param index
     * @param incValue
     */
    public void visit_iinc(int index, int incValue) {
    	log("iinc " + index + " " + incValue);
        delegate.visit_iinc(index, incValue);
    }

    /**
     * @param index
     */
    public void visit_iload(int index) {
    	log("iload " + index);
        delegate.visit_iload(index);        
    }

    /**
     *
     */
    public void visit_imul() {
        delegate.visit_imul();
        throwException();
    }

    /**
     *
     */
    public void visit_ineg() {
        delegate.visit_ineg();
        throwException();
    }

    /**
     * @param clazz
     */
    public void visit_instanceof(VmConstClass clazz) {
        delegate.visit_instanceof(clazz);
        throwException();
    }

    /**
     * @param methodRef
     * @param count
     */
    public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
        delegate.visit_invokeinterface(methodRef, count);
        throwException();
    }

    /**
     * @param methodRef
     */
    public void visit_invokespecial(VmConstMethodRef methodRef) {
    	log("invokespecial " + methodRef.getClassName() + "." + methodRef.getName());
        delegate.visit_invokespecial(methodRef);
    }

    /**
     * @param methodRef
     */
    public void visit_invokestatic(VmConstMethodRef methodRef) {
    	log("invokestatic " + methodRef.getClassName() + "." + methodRef.getName());
        delegate.visit_invokestatic(methodRef);        
    }

    /**
     * @param methodRef
     */
    public void visit_invokevirtual(VmConstMethodRef methodRef) {
        delegate.visit_invokevirtual(methodRef);
        throwException();
    }
        
    @Override
    public void visit_invokedynamic(VmConstDynamicMethodRef constMethodHandle) {
    	delegate.visit_invokedynamic(constMethodHandle);
    	throwException();
    }

    /**
     *
     */
    public void visit_ior() {
        delegate.visit_ior();
        throwException();
    }

    /**
     *
     */
    public void visit_irem() {
        delegate.visit_irem();
        throwException();
    }

    /**
     *
     */
    public void visit_ireturn() {
        delegate.visit_ireturn();
        throwException();
    }

    /**
     *
     */
    public void visit_ishl() {
        delegate.visit_ishl();
        throwException();
    }

    /**
     *
     */
    public void visit_ishr() {
        delegate.visit_ishr();
        throwException();
    }

    /**
     * @param index
     */
    public void visit_istore(int index) {
    	log("istore " + index);
        delegate.visit_istore(index);        
    }

    /**
     *
     */
    public void visit_isub() {
        delegate.visit_isub();
        throwException();
    }

    /**
     *
     */
    public void visit_iushr() {
        delegate.visit_iushr();
        throwException();
    }

    /**
     *
     */
    public void visit_ixor() {
        delegate.visit_ixor();
        throwException();
    }

    /**
     * @param address
     */
    public void visit_jsr(int address) {
        delegate.visit_jsr(address);
        throwException();
    }

    /**
     *
     */
    public void visit_l2d() {
        delegate.visit_l2d();
        throwException();
    }

    /**
     *
     */
    public void visit_l2f() {
        delegate.visit_l2f();
        throwException();
    }

    /**
     *
     */
    public void visit_l2i() {
        delegate.visit_l2i();
        throwException();
    }

    /**
     *
     */
    public void visit_ladd() {
    	log("ladd");
        delegate.visit_ladd();        
    }

    /**
     *
     */
    public void visit_laload() {
        delegate.visit_laload();
        throwException();
    }

    /**
     *
     */
    public void visit_land() {
        delegate.visit_land();
        throwException();
    }

    /**
     *
     */
    public void visit_lastore() {
        delegate.visit_lastore();
        throwException();
    }

    /**
     *
     */
    public void visit_lcmp() {
        delegate.visit_lcmp();
        throwException();
    }

    /**
     * @param value
     */
    public void visit_lconst(long value) {
    	log("lconst " + value);
        delegate.visit_lconst(value);        
    }

    /**
     * @param value
     */
    public void visit_ldc(VmConstString value) {
        delegate.visit_ldc(value);
        throwException();
    }

    /**
     * @param value
     */
    public void visit_ldc(VmConstClass value) {
    	log("ldc " + value.getClassName());
        delegate.visit_ldc(value);        
    }

    /**
     * Push the given VmType on the stack.
     */
    public void visit_ldc(VmType<?> value) {
        delegate.visit_ldc(value);
        throwException();
    }

    /**
     *
     */
    public void visit_ldiv() {
        delegate.visit_ldiv();
        throwException();
    }

    /**
     * @param index
     */
    public void visit_lload(int index) {
    	log("lload " + index);
        delegate.visit_lload(index);        
    }

    /**
     *
     */
    public void visit_lmul() {
        delegate.visit_lmul();
        throwException();
    }

    /**
     *
     */
    public void visit_lneg() {
        delegate.visit_lneg();
        throwException();
    }

    /**
     * @param defValue
     * @param matchValues
     * @param addresses
     */
    public void visit_lookupswitch(int defValue, int[] matchValues,
                                   int[] addresses) {
        delegate.visit_lookupswitch(defValue, matchValues, addresses);
        throwException();
    }

    /**
     *
     */
    public void visit_lor() {
        delegate.visit_lor();
        throwException();
    }

    /**
     *
     */
    public void visit_lrem() {
        delegate.visit_lrem();
        throwException();
    }

    /**
     *
     */
    public void visit_lreturn() {
        delegate.visit_lreturn();
        throwException();
    }

    /**
     *
     */
    public void visit_lshl() {
        delegate.visit_lshl();
        throwException();
    }

    /**
     *
     */
    public void visit_lshr() {
        delegate.visit_lshr();
        throwException();
    }

    /**
     * @param index
     */
    public void visit_lstore(int index) {
    	log("lstore " + index);
        delegate.visit_lstore(index);        
    }

    /**
     *
     */
    public void visit_lsub() {
        delegate.visit_lsub();
        throwException();
    }

    /**
     *
     */
    public void visit_lushr() {
        delegate.visit_lushr();
        throwException();
    }

    /**
     *
     */
    public void visit_lxor() {
        delegate.visit_lxor();
        throwException();
    }

    /**
     *
     */
    public void visit_monitorenter() {
        delegate.visit_monitorenter();
        throwException();
    }

    /**
     *
     */
    public void visit_monitorexit() {
        delegate.visit_monitorexit();
        throwException();
    }

    /**
     * @param clazz
     * @param dimensions
     */
    public void visit_multianewarray(VmConstClass clazz, int dimensions) {
        delegate.visit_multianewarray(clazz, dimensions);
        throwException();
    }

    /**
     * @param clazz
     */
    public void visit_new(VmConstClass clazz) {
    	log("new " + clazz.getClassName());
        delegate.visit_new(clazz);        
    }

    /**
     * @param type
     */
    public void visit_newarray(int type) {
        delegate.visit_newarray(type);
        throwException();
    }

    /**
     *
     */
    public void visit_nop() {
        delegate.visit_nop();
        throwException();
    }

    /**
     *
     */
    public void visit_pop() {
        delegate.visit_pop();
        throwException();
    }

    /**
     *
     */
    public void visit_pop2() {
        delegate.visit_pop2();
        throwException();
    }

    /**
     * @param fieldRef
     */
    public void visit_putfield(VmConstFieldRef fieldRef) {
        delegate.visit_putfield(fieldRef);
        throwException();
    }

    /**
     * @param fieldRef
     */
    public void visit_putstatic(VmConstFieldRef fieldRef) {
        delegate.visit_putstatic(fieldRef);
        throwException();
    }

    /**
     * @param index
     */
    public void visit_ret(int index) {
        delegate.visit_ret(index);
        throwException();
    }

    /**
     *
     */
    public void visit_return() {
        delegate.visit_return();
        throwException();
    }

    /**
     *
     */
    public void visit_saload() {
        delegate.visit_saload();
        throwException();
    }

    /**
     *
     */
    public void visit_sastore() {
        delegate.visit_sastore();
        throwException();
    }

    /**
     *
     */
    public void visit_swap() {
        delegate.visit_swap();
        throwException();
    }

    /**
     * @param defValue
     * @param lowValue
     * @param highValue
     * @param addresses
     */
    public void visit_tableswitch(int defValue, int lowValue, int highValue,
                                  int[] addresses) {
        delegate.visit_tableswitch(defValue, lowValue, highValue, addresses);
        throwException();
    }

    /**
     *
     */
    public void yieldPoint() {
        delegate.yieldPoint();
        throwException();
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#visit_aloadStored(int)
     */
    public void visit_aloadStored(int index) {
        delegate.visit_aloadStored(index);
        throwException();
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#visit_dloadStored(int)
     */
    public void visit_dloadStored(int index) {
        delegate.visit_dloadStored(index);
        throwException();
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#visit_floadStored(int)
     */
    public void visit_floadStored(int index) {
        delegate.visit_floadStored(index);
        throwException();
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#visit_iloadStored(int)
     */
    public void visit_iloadStored(int index) {
        delegate.visit_iloadStored(index);
        throwException();
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#visit_lloadStored(int)
     */
    public void visit_lloadStored(int index) {
        delegate.visit_lloadStored(index);
        throwException();
    }
    
    private void throwException() {
    	//throw new UnsupportedOperationException();
    }
    
    private void log(String s) {
    	//System.out.println(s);
    }
}
