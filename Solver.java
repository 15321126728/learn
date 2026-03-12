import java.math.BigInteger;

// 解析器/求解器
public class Solver {
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
