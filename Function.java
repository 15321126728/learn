// 函数定义
public class Function {
    public final String name;
    public final String param;
    public final Expr body;

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
