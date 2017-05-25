package com.mercy.compiler.BackEnd;

import com.mercy.compiler.INS.Operand.Register;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mercy on 17-5-24.
 */
public class RegisterConfig {
    private Register rax, rbx, rcx, rdx, rsi, rdi, rsp, rbp;
    private List<Register> registers;
    private List<Register> paraRegister;

    public RegisterConfig() {
        // init registers
        registers = new ArrayList<>();
        paraRegister = new ArrayList<>();
        rax = new Register("rax"); registers.add(rax);
        rbx = new Register("rbx"); registers.add(rbx);
        rcx = new Register("rcx"); registers.add(rcx);
        rdx = new Register("rdx"); registers.add(rdx);
        rsi = new Register("rsi"); registers.add(rsi);
        rdi = new Register("rdi"); registers.add(rdi);
        rbp = new Register("rbp"); registers.add(rbp);
        rsp = new Register("rsp"); registers.add(rsp);
        for (int i = 8; i < 16; i++) {
            registers.add(new Register("r" + i));
        }
        int [] calleeSave = {1, 6, 12, 13, 14, 15};
        for (int x : calleeSave)
            registers.get(x).setCalleeSave(true);

        // set registers for parameter
        paraRegister.add(rdi); paraRegister.add(rsi);
        paraRegister.add(rdx); paraRegister.add(rcx);
        paraRegister.add(registers.get(8));
        paraRegister.add(registers.get(9));
    }


    public List<Register> registers() {
        return registers;
    }

    public List<Register> paraRegister() {
        return paraRegister;
    }
}