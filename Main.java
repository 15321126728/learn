import java.math.BigInteger;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 读取自定义函数的个数
        int n = Integer.parseInt(scanner.nextLine().trim());
        
        // 如果存在函数定义，则读取
        String funcDef = null;
        if (n == 1) {
            funcDef = scanner.nextLine();
        }
        
        // 读取待求值的表达式
        String expr = scanner.nextLine();
        
        scanner.close();
        
        // 解析并求解
        Solver solver = new Solver(funcDef);
        Expr result = solver.parse(expr);
        String output = result.simplify().toString();
        
        // 移除不必要的外层括号
        if (output.startsWith("(") && output.endsWith(")")) {
            // 检查这些是否真的是外层括号
            int depth = 0;
            boolean isOuter = true;
            for (int i = 0; i < output.length() - 1; i++) {
                if (output.charAt(i) == '(') depth++;
                else if (output.charAt(i) == ')') depth--;
                if (depth == 0) {
                    isOuter = false;
                    break;
                }
            }
            if (isOuter) {
                output = output.substring(1, output.length() - 1);
            }
        }
        
        System.out.println(output);
    }

    // 所有表达式类型的基类
    private static abstract class Expr {
        public abstract Expr simplify();
        public abstract boolean isZero();
        public abstract Expr substitute(String var, Expr value);
        
        // 通过检查两个表达式的差是否为零来判断它们是否相等
        public boolean equals(Expr other) {
            Expr diff = new BinaryExpr(this, other, '-').simplify();
            return diff.isZero();
        }
    }

    // 多项式表达式（项的和）
    private static class PolyExpr extends Expr {
        private final Map<Integer, BigInteger> terms = new HashMap<>();

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

    // 指数表达式 exp(expr)
    private static class ExpExpr extends Expr {
        private Expr arg;
        private final int power;

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

    // 二元运算表达式
    private static class BinaryExpr extends Expr {
        private final Expr left;
        private final Expr right;
        private final char op;

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

    // 选择式表达式 [(A==B)?C:D]
    private static class SelectionExpr extends Expr {
        private final Expr condition1;
        private final Expr condition2;
        private final Expr trueExpr;
        private final Expr falseExpr;

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

    // 函数定义
    private static class Function {
        private final String name;
        private final String param;
        private final Expr body;

        public Function(String name, String param, Expr body) {
            this.name = name;
            this.param = param;
            // 存储时不化简函数体
            this.body = body;
        }

        public Expr call(Expr arg) {
            // 替换后再化简
            return body.substitute(param, arg).simplify();
        }
    }

    // 解析器/求解器
    private static class Solver {
        private String expr;
        private int pos;
        private Function function;

        public Solver(String funcDef) {
            if (funcDef != null) {
                parseFunctionDefinition(funcDef);
            }
        }

        private void parseFunctionDefinition(String def) {
            // 删除空白字符
            def = def.replaceAll("\\s+", "");
            
            // 解析 f(x) = 表达式
            int eqPos = def.indexOf('=');
            String body = def.substring(eqPos + 1);
            
            // 解析函数体表达式
            this.expr = body;
            this.pos = 0;
            Expr bodyExpr = parseExpr();
            
            this.function = new Function("f", "x", bodyExpr);
        }

        public Expr parse(String input) {
            // 删除空白字符并简化符号
            String s = input.replaceAll("\\s+", "");
            
            // 简化多重符号
            boolean changed = true;
            while (changed) {
                String original = s;
                s = s.replace("++", "+")
                        .replace("--", "+")
                        .replace("+-", "-")
                        .replace("-+", "-");
                changed = !s.equals(original);
            }
            
            this.expr = s;
            this.pos = 0;
            return parseExpr();
        }

        private char peek() {
            if (pos >= expr.length()) {
                return 0;
            }
            return expr.charAt(pos);
        }

        private char consume() {
            char c = peek();
            if (pos < expr.length()) {
                pos++;
            }
            return c;
        }

        private Expr parseExpr() {
            Expr left = parseTerm();

            while (true) {
                char op = peek();
                if (op == '+' || op == '-') {
                    consume();
                    Expr right = parseTerm();
                    left = new BinaryExpr(left, right, op);
                } else {
                    break;
                }
            }
            return left;
        }

        private Expr parseTerm() {
            int sign = 1;
            if (peek() == '+') {
                consume();
            } else if (peek() == '-') {
                consume();
                sign = -1;
            }

            Expr left = parseFactor();
            if (sign == -1) {
                if (left instanceof PolyExpr) {
                    left = ((PolyExpr) left).negate();
                } else {
                    left = new BinaryExpr(new PolyExpr(BigInteger.ZERO, 0), left, '-');
                }
            }

            while (peek() == '*') {
                consume();
                Expr right = parseFactor();
                left = new BinaryExpr(left, right, '*');
            }
            return left;
        }

        private Expr parseFactor() {
            Expr res;
            
            // 选择式因子：[(A==B)?C:D]
            if (peek() == '[') {
                consume(); // '['
                consume(); // '('
                Expr cond1 = parseFactor();
                consume(); // '='
                consume(); // '='
                Expr cond2 = parseFactor();
                consume(); // ')'
                consume(); // '?'
                Expr trueExpr = parseFactor();
                consume(); // ':'
                Expr falseExpr = parseFactor();
                consume(); // ']'
                res = new SelectionExpr(cond1, cond2, trueExpr, falseExpr);
            }
            // 表达式因子：(expr)^exp
            else if (peek() == '(') {
                consume();
                res = parseExpr();
                consume(); // ')'
                if (peek() == '^') {
                    consume();
                    int exp = parseSimpleInt();
                    // 先化简以获得PolyExpr，然后应用幂次
                    res = res.simplify();
                    if (res instanceof PolyExpr) {
                        res = ((PolyExpr) res).pow(exp);
                    }
                }
            }
            // Exp函数或函数调用
            else if (peek() == 'e' || peek() == 'f') {
                if (peek() == 'e') {
                    // exp(factor)
                    consume(); // 'e'
                    consume(); // 'x'
                    consume(); // 'p'
                    consume(); // '('
                    Expr arg = parseFactor();
                    consume(); // ')'
                    int power = 1;
                    if (peek() == '^') {
                        consume();
                        power = parseSimpleInt();
                    }
                    res = new ExpExpr(arg, power);
                } else {
                    // f(factor)
                    consume(); // 'f'
                    consume(); // '('
                    Expr arg = parseFactor();
                    consume(); // ')'
                    res = function.call(arg);
                }
            }
            // 变量因子：x^exp
            else if (peek() == 'x') {
                consume();
                int exp = 1;
                if (peek() == '^') {
                    consume();
                    exp = parseSimpleInt();
                }
                res = new PolyExpr(BigInteger.ONE, exp);
            }
            // 常数因子
            else {
                BigInteger val = parseBigInteger();
                res = new PolyExpr(val, 0);
            }
            return res;
        }

        private int parseSimpleInt() {
            if (peek() == '+') {
                consume();
            }
            StringBuilder sb = new StringBuilder();
            while (Character.isDigit(peek())) {
                sb.append(consume());
            }
            if (sb.length() == 0) {
                return 0;
            }
            return Integer.parseInt(sb.toString());
        }

        private BigInteger parseBigInteger() {
            StringBuilder sb = new StringBuilder();
            if (peek() == '+' || peek() == '-') {
                sb.append(consume());
            }
            while (Character.isDigit(peek())) {
                sb.append(consume());
            }
            if (sb.length() == 0 || (sb.length() == 1 && !Character.isDigit(sb.charAt(0)))) {
                return BigInteger.ZERO;
            }
            return new BigInteger(sb.toString());
        }
    }
}
