// 所有表达式类型的基类
public abstract class Expr {
    public abstract Expr simplify();
    public abstract boolean isZero();
    public abstract Expr substitute(String var, Expr value);
    
    // 通过检查两个表达式的差是否为零来判断它们是否相等
    public boolean equals(Expr other) {
        Expr diff = new BinaryExpr(this, other, '-').simplify();
        return diff.isZero();
    }
}
