// 选择式表达式 [(A==B)?C:D]
public class SelectionExpr extends Expr {
    public final Expr condition1;
    public final Expr condition2;
    public final Expr trueExpr;
    public final Expr falseExpr;

    public SelectionExpr(Expr condition1, Expr condition2, Expr trueExpr, Expr falseExpr) {
        this.condition1 = condition1;
        this.condition2 = condition2;
        this.trueExpr = trueExpr;
        this.falseExpr = falseExpr;
    }

    @Override
    public Expr simplify() {
        Expr c1 = condition1.simplify();
        Expr c2 = condition2.simplify();
        
        // 通过检查c1 - c2 == 0来判断c1 == c2
        if (c1.equals(c2)) {
            return trueExpr.simplify();
        } else {
            return falseExpr.simplify();
        }
    }

    @Override
    public boolean isZero() {
        return false; // 将在化简过程中求值
    }

    @Override
    public Expr substitute(String var, Expr value) {
        return new SelectionExpr(
            condition1.substitute(var, value),
            condition2.substitute(var, value),
            trueExpr.substitute(var, value),
            falseExpr.substitute(var, value)
        );
    }

    @Override
    public String toString() {
        return "[(" + condition1 + "==" + condition2 + ")?" + trueExpr + ":" + falseExpr + "]";
    }
}
