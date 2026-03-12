import java.math.BigInteger;
import java.util.*;

// 多项式表达式（项的和）
public class PolyExpr extends Expr {
    public final Map<Integer, BigInteger> terms = new HashMap<>();

    public PolyExpr() {
    }

    public PolyExpr(BigInteger coeff, int exp) {
        if (!coeff.equals(BigInteger.ZERO)) {
            terms.put(exp, coeff);
        }
    }

    public PolyExpr add(PolyExpr other) {
        PolyExpr res = new PolyExpr();
        res.terms.putAll(this.terms);
        for (Map.Entry<Integer, BigInteger> entry : other.terms.entrySet()) {
            res.terms.merge(entry.getKey(), entry.getValue(), BigInteger::add);
        }
        res.cleanup();
        return res;
    }

    public PolyExpr sub(PolyExpr other) {
        return this.add(other.negate());
    }

    public PolyExpr multiply(PolyExpr other) {
        PolyExpr res = new PolyExpr();
        for (Map.Entry<Integer, BigInteger> e1 : this.terms.entrySet()) {
            for (Map.Entry<Integer, BigInteger> e2 : other.terms.entrySet()) {
                int newExp = e1.getKey() + e2.getKey();
                BigInteger newCoeff = e1.getValue().multiply(e2.getValue());
                res.terms.merge(newExp, newCoeff, BigInteger::add);
            }
        }
        res.cleanup();
        return res;
    }

    public PolyExpr pow(int exp) {
        // 处理0^0 = 1的边界情况
        if (exp == 0) {
            return new PolyExpr(BigInteger.ONE, 0);
        }
        if (exp == 1) {
            return this;
        }
        PolyExpr res = new PolyExpr(BigInteger.ONE, 0);
        PolyExpr base = this;
        for (int i = 0; i < exp; i++) {
            res = res.multiply(base);
        }
        return res;
    }

    public PolyExpr negate() {
        PolyExpr res = new PolyExpr();
        for (Map.Entry<Integer, BigInteger> e : this.terms.entrySet()) {
            res.terms.put(e.getKey(), e.getValue().negate());
        }
        return res;
    }

    private void cleanup() {
        terms.entrySet().removeIf(e -> e.getValue().equals(BigInteger.ZERO));
    }

    @Override
    public Expr simplify() {
        return this;
    }

    @Override
    public boolean isZero() {
        return terms.isEmpty() || terms.values().stream().allMatch(v -> v.equals(BigInteger.ZERO));
    }

    @Override
    public Expr substitute(String var, Expr value) {
        if (var.equals("x")) {
            // 如果表达式中没有x，则不需要替换
            if (terms.isEmpty() || (terms.size() == 1 && terms.containsKey(0))) {
                // 这是一个常数，没有x需要替换
                return this;
            }
            
            // 首先化简value，尝试得到一个PolyExpr
            Expr simplifiedValue = value.simplify();
            
            if (simplifiedValue instanceof PolyExpr) {
                // 正常的多项式替换
                PolyExpr polyValue = (PolyExpr) simplifiedValue;
                PolyExpr result = new PolyExpr(BigInteger.ZERO, 0);
                for (Map.Entry<Integer, BigInteger> entry : terms.entrySet()) {
                    int exp = entry.getKey();
                    BigInteger coeff = entry.getValue();
                    PolyExpr term = polyValue.pow(exp);
                    term = term.multiply(new PolyExpr(coeff, 0));
                    result = result.add(term);
                }
                return result;
            } else {
                // value不是多项式（例如，ExpExpr）
                // 我们需要构建一个更复杂的表达式
                // 对于像3*x^2 + 2*x + 1这样的多项式，用值v替换后，
                // 我们想要：3*v^2 + 2*v + 1
                
                Expr result = null;
                List<Integer> exps = new ArrayList<>(terms.keySet());
                Collections.sort(exps, Collections.reverseOrder());
                
                for (int exp : exps) {
                    BigInteger coeff = terms.get(exp);
                    Expr term;
                    
                    if (exp == 0) {
                        // 常数项
                        term = new PolyExpr(coeff, 0);
                    } else {
                        // 构建 coeff * value^exp
                        // 首先，处理 value^exp
                        Expr poweredValue;
                        if (exp == 1) {
                            poweredValue = value;
                        } else {
                            // 对于exp > 1，我们需要创建v*v*v...，这比较复杂
                            // 目前，表示为乘法链
                            poweredValue = value;
                            for (int i = 1; i < exp; i++) {
                                poweredValue = new BinaryExpr(poweredValue, value, '*');
                            }
                        }
                        
                        // 然后乘以系数
                        if (coeff.equals(BigInteger.ONE)) {
                            term = poweredValue;
                        } else if (coeff.equals(BigInteger.valueOf(-1))) {
                            term = new BinaryExpr(new PolyExpr(BigInteger.ZERO, 0), poweredValue, '-');
                        } else {
                            term = new BinaryExpr(new PolyExpr(coeff, 0), poweredValue, '*');
                        }
                    }
                    
                    if (result == null) {
                        result = term;
                    } else {
                        result = new BinaryExpr(result, term, '+');
                    }
                }
                
                return result != null ? result : new PolyExpr(BigInteger.ZERO, 0);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        if (terms.isEmpty()) {
            return "0";
        }
        List<Integer> exps = new ArrayList<>(terms.keySet());
        exps.sort(Collections.reverseOrder());

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (int exp : exps) {
            BigInteger coeff = terms.get(exp);

            if (!first && coeff.signum() > 0) {
                sb.append("+");
            }

            if (exp == 0) {
                sb.append(coeff);
            } else {
                if (coeff.equals(BigInteger.ONE)) {
                    sb.append("x");
                } else if (coeff.equals(BigInteger.valueOf(-1))) {
                    sb.append("-x");
                } else {
                    sb.append(coeff).append("*x");
                }

                if (exp > 1) {
                    sb.append("^").append(exp);
                }
            }
            first = false;
        }
        return sb.toString();
    }
}
