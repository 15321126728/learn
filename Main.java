import java.util.Scanner;

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
}
