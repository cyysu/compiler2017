package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.CJump;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Jmp;
import com.mercy.compiler.INS.Label;

import java.io.PrintStream;
import java.util.*;

import static java.lang.System.err;


/**
 * Created by mercy on 17-5-23.
 */
public class ControlFlowAnalyzer {
    List<FunctionEntity> functionEntities;

    public ControlFlowAnalyzer(InstructionEmitter emitter) {
        functionEntities = emitter.functionEntities();
    }

    public void buildControlFlow() {
        for (FunctionEntity functionEntity : functionEntities) {
            if (Option.enableInlineFunction && functionEntity.canbeInlined())
                return;
            buildBasicBlock(functionEntity);
            buildControFlowGraph(functionEntity);
            Optimize(functionEntity);
            layoutFunction(functionEntity);
        }
    }

    int ct = 0;
    private void buildBasicBlock(FunctionEntity entity) {
        List<BasicBlock> bbs = new LinkedList<>();

        BasicBlock bb = null;
        for (Instruction ins : entity.ins()) {
            if (bb == null && !(ins instanceof Label)) { // add new label
                Label label = new Label("cfg_added_" + ct++);
                bb = new BasicBlock(label);
                bb.addIns(label);
            }

            if (ins instanceof Label) {
                if (bb != null) {
                    bb.addJumpTo((Label) ins);
                    bb.ins().add(new Jmp((Label) ins));
                    bbs.add(bb);
                }
                bb = new BasicBlock((Label) ins);
                bb.addIns(ins);
            } else {
                bb.addIns(ins);
                if (ins instanceof Jmp) {
                    bb.addJumpTo(((Jmp) ins).dest());
                    bbs.add(bb);
                    bb = null;
                } else if (ins instanceof CJump) {
                    bb.addJumpTo(((CJump) ins).trueLabel());
                    bb.addJumpTo(((CJump) ins).falseLabel());
                    bbs.add(bb);
                    bb = null;
                }
            }
        }

        if (bb != null) { // handle the case that a function ends without "return"
            bb.addJumpTo(entity.endLabelINS());
            bbs.add(bb);
        }

        // link edge
        for (BasicBlock basicBlock : bbs) {
            for (Label label : basicBlock.jumpTo()) {
                if (label == entity.endLabelINS())
                    continue;
                basicBlock.addSuccessor(label.basicBlock());
                label.basicBlock().addPredecessor(basicBlock);
            }
        }

        entity.setBbs(bbs);
    }

    private void buildControFlowGraph(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) {
            // inside bb
            List<Instruction> ins = basicBlock.ins();
            Iterator<Instruction> iter = ins.iterator();
            if (iter.hasNext()) {
                Instruction pre = iter.next();
                while(iter.hasNext()) {
                    Instruction now = iter.next();
                    pre.sucessor().add(now);
                    now.predessor().add(pre);
                    pre = now;
                }
            }

            // between two bb
            Instruction first = ins.get(0);
            for (BasicBlock pre : basicBlock.predecessor()) {
                pre.ins().get(pre.ins().size()-1).sucessor().add(first);
            }

            Instruction last = ins.get(ins.size()-1);
            for (BasicBlock suc : basicBlock.successor()) {
                suc.ins().get(0).predessor().add(last);
            }
        }
    }

    void Optimize(FunctionEntity entity) {
        Set<BasicBlock> toMerge = new HashSet<>();

        boolean modified = true;
        while(modified) {
            modified = false;

            toMerge.clear();

            BasicBlock now;
            // merge
            for (BasicBlock basicBlock : entity.bbs()) {
                if (basicBlock.successor().size() == 1 && basicBlock.successor().get(0).predecessor().size() == 1) {
                    now = basicBlock;
                    BasicBlock next = now.successor().get(0);
                    if (next.successor().size() != 0) {
                        modified = true;
                        for (BasicBlock next_next : next.successor()) {
                            err.println("merge " + now.label() + " <- " + next.label());
                            next_next.predecessor().remove(next);
                            next_next.predecessor().add(now);
                        }

                        now.ins().addAll(next.ins());
                        entity.bbs().remove(next);
                        now.successor().remove(next);
                        break;
                    }
                }
            }

            // same branch
            for (BasicBlock basicBlock : entity.bbs()) {
                if (basicBlock.successor().size() == 2 && basicBlock.successor().get(0) == basicBlock.successor().get(1)) {
                    ;
                }
            }

            // empty block
            for (BasicBlock basicBlock : entity.bbs()) {
                if (basicBlock.ins().size() == 1) {
                    ;
                }
            }
        }
    }

    void layoutFunction(FunctionEntity entity) {
        List<Instruction> ins = new LinkedList<>();
        List<BasicBlock> bbs = entity.bbs();
        Queue<BasicBlock> queue = new ArrayDeque<>();

        queue.addAll(bbs);

        while(!queue.isEmpty()) {
            BasicBlock bb = queue.remove();
            while(bb != null && !bb.layouted()) {
                BasicBlock next = null;
                for (BasicBlock suc : bb.successor()) {
                    if (!suc.layouted()) {
                        Instruction last = bb.ins().get(bb.ins().size()-1);
                        if (last instanceof Jmp) {
                            bb.ins().remove(bb.ins().size()-1);  // remove redundant jump
                        } else if (last instanceof CJump) {
                            ((CJump) last).setFallThrough(suc.label());
                        }
                        next = suc;
                        break;
                    }
                }
                bb.setLayouted(true);
                ins.addAll(bb.ins());
                bb = next;
            }
        }

        entity.setINS(ins);
    }

    /********** DEBUG TOOL **********/
    public void printSelf(PrintStream out) {
        for (FunctionEntity functionEntity : functionEntities) {
            if (Option.enableInlineFunction && functionEntity.canbeInlined())
                return;
            out.println("========== " + functionEntity.name() + " ==========");
            if (Option.enableInlineFunction && functionEntity.canbeInlined()) {
                out.println("BE INLINED");
                continue;
            }
            for (BasicBlock basicBlock : functionEntity.bbs()) {
                out.print("----- b -----"  + "  jump to:");
                for (Label label : basicBlock.jumpTo()) {
                    out.print("   " + label.name());
                }
                out.println();
                for (Instruction instruction : basicBlock.ins()) {
                    out.println(instruction.toString());
                }
            }
        }
    }
}

