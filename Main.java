import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            Solver solver = new Solver(input);
            System.out.println(solver.solve());
        }
        scanner.close();
    }

    /*表达式解析器*/

    private static class Solver {
        private final String expr;
        private int pos;

        public Solver(String input) {
            // 去除空白符
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
        }

        public String solve() {
            if (expr.isEmpty()) {
                return "";
            }
            Poly result = parseExpr();
            return result.toString();

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

        // 解析表达式：Expr -> Term { + Term | - Term }
        private Poly parseExpr() {
            Poly left = parseTerm();

            while (true) {
                char op = peek();
                if (op == '+' || op == '-') {
                    consume();
                    Poly right = parseTerm();
                    if (op == '+') {
                        left = left.add(right);
                    } else {
                        left = left.sub(right);
                    }
                } else {
                    break;
                }
            }
            return left;
        }

        // 解析项：Term -> [Sign] Factor { * Factor }
        private Poly parseTerm() {
            int sign = 1;
            if (peek() == '+') {
                consume();
            } else if (peek() == '-') {
                consume();
                sign = -1;
            }

            Poly left = parseFactor();
            if (sign == -1) {
                left = left.negate();
            }

            while (peek() == '*') {
                consume();
                Poly right = parseFactor();
                left = left.multiply(right);
            }
            return left;
        }

        // 解析因子：Factor -> (Expr)^exp | x^exp | Constant
        private Poly parseFactor() {
            Poly res;
            if (peek() == '(') {
                consume();
                res = parseExpr();
                consume();
                if (peek() == '^') {
                    consume();
                    int exp = parseSimpleInt();
                    res = res.pow(exp);
                }
            } else if (peek() == 'x') {
                consume();
                int exp = 1;
                if (peek() == '^') {
                    consume();
                    exp = parseSimpleInt();
                }
                res = new Poly(BigInteger.ONE, exp);
            } else {
                BigInteger val = parseBigInteger();
                res = new Poly(val, 0);
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
            if (sb.length() == 0) {
                return BigInteger.ZERO;
            }
            return new BigInteger(sb.toString());
        }
    }

    /*多项式类：支持加减乘和乘方*/

    private static class Poly {
        private final Map<Integer, BigInteger> terms = new HashMap<>();

        public Poly() {
        }

        public Poly(BigInteger coeff, int exp) {
            if (!coeff.equals(BigInteger.ZERO)) {
                terms.put(exp, coeff);
            }
        }

        public Poly add(Poly other) {
            Poly res = new Poly();
            res.terms.putAll(this.terms);
            for (Map.Entry<Integer, BigInteger> entry : other.terms.entrySet()) {
                res.terms.merge(entry.getKey(), entry.getValue(), BigInteger::add);
            }
            res.cleanup();
            return res;
        }

        public Poly sub(Poly other) {
            return this.add(other.negate());
        }

        public Poly multiply(Poly other) {
            Poly res = new Poly();
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

        public Poly pow(int exp) {
            if (exp == 0) {
                return new Poly(BigInteger.ONE, 0);
            }
            if (exp == 1) {
                return this;
            }
            Poly res = new Poly(BigInteger.ONE, 0);
            Poly base = this;
            for (int i = 0; i < exp; i++) {
                res = res.multiply(base);
            }
            return res;
        }

        public Poly negate() {
            Poly res = new Poly();
            for (Map.Entry<Integer, BigInteger> e : this.terms.entrySet()) {
                res.terms.put(e.getKey(), e.getValue().negate());
            }
            return res;
        }

        private void cleanup() {
            terms.entrySet().removeIf(e -> e.getValue().equals(BigInteger.ZERO));
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
}