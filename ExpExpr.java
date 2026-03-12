import java.math.BigInteger;

// 指数表达式 exp(expr)
public class ExpExpr extends Expr {
    public Expr arg;
    public final int power;

    public ExpExpr(Expr arg, int power) {
        this.arg = arg;
        this.power = power;
    }

    @Override
    public Expr simplify() {
        Expr simplifiedArg = arg.simplify();
        
        // exp(0) = 1
        if (simplifiedArg.isZero()) {
            return new PolyExpr(BigInteger.ONE, 0);
        }
        
        // 如果幂次为0，返回1
        if (power == 0) {
            return new PolyExpr(BigInteger.ONE, 0);
        }
        
        this.arg = simplifiedArg;
        return this;
    }

    @Override
    public boolean isZero() {
        return false; // exp(x)永远不为零
    }

    @Override
    public Expr substitute(String var, Expr value) {
        return new ExpExpr(arg.substitute(var, value), power);
    }

    @Override
    public String toString() {
        String argStr = arg.toString();
        
        // 检查参数是否需要括号
        // 简单的变量或常数不需要额外的括号
        boolean needsParens = false;
        
        if (arg instanceof PolyExpr) {
            PolyExpr poly = (PolyExpr) arg;
            // 如果是多项，或者是复杂表达式，添加括号
            if (poly.terms.size() > 1 || (poly.terms.size() == 1 && !argStr.equals("x") && poly.terms.containsKey(0))) {
                needsParens = true;
            } else if (poly.terms.size() == 1) {
                // 单项 - 检查是否复杂（如x^2）
                int exp = poly.terms.keySet().iterator().next();
                if (exp > 1 || poly.terms.get(exp).abs().compareTo(BigInteger.ONE) > 0) {
                    // 类似2*x或x^2，对于exp不需要括号
                    needsParens = false;
                }
            }
        }
        
        String result;
        if (needsParens) {
            result = "exp((" + argStr + "))";
        } else {
            result = "exp(" + argStr + ")";
        }
        
        if (power > 1) {
            result += "^" + power;
        }
        
        return result;
    }
}
