package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Type.BoolType;

/**
 * Created by mercy on 17-3-23.
 */
public class BoolLiteralNode extends LiteralNode {
    boolean value;

    public BoolLiteralNode(Location loc, boolean value) {
        super(loc, new BoolType());
        this.value = value;
    }

    public boolean value() {
        return value;
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}