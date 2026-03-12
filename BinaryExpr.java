// 二元运算表达式
public class BinaryExpr extends Expr {
    public final Expr left;
    public final Expr right;
    public final char op;

    public BinaryExpr(Expr left, Expr right, char op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    public Expr simplify() {
        Expr l = left.simplify();
        Expr r = right.simplify();
        
        // 对于exp表达式，我们可以合并：exp(a) * exp(b) = exp(a+b)
        // 但不进一步化简参数
        if (op == '*' && l instanceof ExpExpr && r instanceof ExpExpr) {
            ExpExpr el = (ExpExpr) l;
            ExpExpr er = (ExpExpr) r;
            if (el.power == 1 && er.power == 1) {
                // 创建组合参数，不进一步化简
                Expr combinedArg = new BinaryExpr(el.arg, er.arg, '+');
                return new ExpExpr(combinedArg, 1);
            }
        }
        
        if (l instanceof PolyExpr && r instanceof PolyExpr) {
            PolyExpr pl = (PolyExpr) l;
            PolyExpr pr = (PolyExpr) r;
            
            if (op == '+') {
                return pl.add(pr);
            } else if (op == '-') {
                return pl.sub(pr);
            } else if (op == '*') {
                return pl.multiply(pr);
            }
        }
        
        return new BinaryExpr(l, r, op);
    }

    @Override
    public boolean isZero() {
        return false; // 保守策略
    }

    @Override
    public Expr substitute(String var, Expr value) {
        return new BinaryExpr(left.substitute(var, value), right.substitute(var, value), op);
    }

    @Override
    public String toString() {
        return "(" + left.toString() + op + right.toString() + ")";
    }
}
