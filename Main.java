import java.math.BigInteger;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Read number of function definitions
        int n = Integer.parseInt(scanner.nextLine().trim());
        
        // Read function definition if exists
        String funcDef = null;
        if (n == 1) {
            funcDef = scanner.nextLine();
        }
        
        // Read expression to evaluate
        String expr = scanner.nextLine();
        
        scanner.close();
        
        // Parse and solve
        Solver solver = new Solver(funcDef);
        Expr result = solver.parse(expr);
        System.out.println(result.simplify().toString());
    }

    // Base class for all expression types
    private static abstract class Expr {
        public abstract Expr simplify();
        public abstract boolean isZero();
        public abstract Expr substitute(String var, Expr value);
        
        // Check if two expressions are equal by checking if their difference is zero
        public boolean equals(Expr other) {
            Expr diff = new BinaryExpr(this, other, '-').simplify();
            return diff.isZero();
        }
    }

    // Polynomial expression (sum of terms)
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
            // Handle 0^0 = 1 edge case
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
                // Don't substitute if there's no x in this expression
                if (terms.isEmpty() || (terms.size() == 1 && terms.containsKey(0))) {
                    // This is a constant, no x to substitute
                    return this;
                }
                
                // Simplify value first to try to get a PolyExpr
                Expr simplifiedValue = value.simplify();
                
                if (simplifiedValue instanceof PolyExpr) {
                    // Normal polynomial substitution
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
                    // Value is not a polynomial (e.g., ExpExpr)
                    // We need to build a more complex expression
                    // For a polynomial like 3*x^2 + 2*x + 1 with value v,
                    // we want: 3*v^2 + 2*v + 1
                    
                    Expr result = null;
                    List<Integer> exps = new ArrayList<>(terms.keySet());
                    Collections.sort(exps, Collections.reverseOrder());
                    
                    for (int exp : exps) {
                        BigInteger coeff = terms.get(exp);
                        Expr term;
                        
                        if (exp == 0) {
                            // Constant term
                            term = new PolyExpr(coeff, 0);
                        } else {
                            // Build coeff * value^exp
                            // First, handle value^exp
                            Expr poweredValue;
                            if (exp == 1) {
                                poweredValue = value;
                            } else {
                                // For exp > 1, we'd need to create v*v*v... which is complex
                                // For now, represent as multiplication chain
                                poweredValue = value;
                                for (int i = 1; i < exp; i++) {
                                    poweredValue = new BinaryExpr(poweredValue, value, '*');
                                }
                            }
                            
                            // Then multiply by coefficient
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

    // Exponential expression exp(expr)
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
            
            // If power is 0, return 1
            if (power == 0) {
                return new PolyExpr(BigInteger.ONE, 0);
            }
            
            this.arg = simplifiedArg;
            return this;
        }

        @Override
        public boolean isZero() {
            return false; // exp(x) is never zero
        }

        @Override
        public Expr substitute(String var, Expr value) {
            return new ExpExpr(arg.substitute(var, value), power);
        }

        @Override
        public String toString() {
            String argStr = arg.toString();
            
            // Check if arg needs parentheses
            // Simple variable or constant doesn't need extra parens
            boolean needsParens = false;
            
            if (arg instanceof PolyExpr) {
                PolyExpr poly = (PolyExpr) arg;
                // If it's more than a single term, or a complex expression, add parens
                if (poly.terms.size() > 1 || (poly.terms.size() == 1 && !argStr.equals("x") && poly.terms.containsKey(0))) {
                    needsParens = true;
                } else if (poly.terms.size() == 1) {
                    // Single term - check if it's complex (like x^2)
                    int exp = poly.terms.keySet().iterator().next();
                    if (exp > 1 || poly.terms.get(exp).abs().compareTo(BigInteger.ONE) > 0) {
                        // It's like 2*x or x^2, doesn't need parens for exp
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

    // Binary operation expression
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
            
            // For exp expressions, we can combine: exp(a) * exp(b) = exp(a+b)
            // But don't simplify the argument further
            if (op == '*' && l instanceof ExpExpr && r instanceof ExpExpr) {
                ExpExpr el = (ExpExpr) l;
                ExpExpr er = (ExpExpr) r;
                if (el.power == 1 && er.power == 1) {
                    // Create the combined argument without further simplification
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
            return false; // Conservative
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

    // Selection expression [(A==B)?C:D]
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
            
            // Check if c1 == c2 by checking if c1 - c2 == 0
            if (c1.equals(c2)) {
                return trueExpr.simplify();
            } else {
                return falseExpr.simplify();
            }
        }

        @Override
        public boolean isZero() {
            return false; // Will be evaluated during simplification
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

    // Function definition
    private static class Function {
        private final String name;
        private final String param;
        private final Expr body;

        public Function(String name, String param, Expr body) {
            this.name = name;
            this.param = param;
            // Don't simplify the body when storing
            this.body = body;
        }

        public Expr call(Expr arg) {
            // Substitute and then simplify
            return body.substitute(param, arg).simplify();
        }
    }

    // Parser/Solver
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
            // Remove whitespace
            def = def.replaceAll("\\s+", "");
            
            // Parse f(x) = expression
            int eqPos = def.indexOf('=');
            String body = def.substring(eqPos + 1);
            
            // Parse the body expression
            this.expr = body;
            this.pos = 0;
            Expr bodyExpr = parseExpr();
            
            this.function = new Function("f", "x", bodyExpr);
        }

        public Expr parse(String input) {
            // Remove whitespace and simplify signs
            String s = input.replaceAll("\\s+", "");
            
            // Simplify multiple signs
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
            
            // Selection factor: [(A==B)?C:D]
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
            // Expression factor: (expr)^exp
            else if (peek() == '(') {
                consume();
                res = parseExpr();
                consume(); // ')'
                if (peek() == '^') {
                    consume();
                    int exp = parseSimpleInt();
                    // Simplify first to get PolyExpr, then apply power
                    res = res.simplify();
                    if (res instanceof PolyExpr) {
                        res = ((PolyExpr) res).pow(exp);
                    }
                }
            }
            // Exp function or function call
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
            // Variable factor: x^exp
            else if (peek() == 'x') {
                consume();
                int exp = 1;
                if (peek() == '^') {
                    consume();
                    exp = parseSimpleInt();
                }
                res = new PolyExpr(BigInteger.ONE, exp);
            }
            // Constant factor
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
