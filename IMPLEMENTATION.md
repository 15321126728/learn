# 表达式解析器实现说明 (Expression Parser Implementation)

本项目实现了一个完整的数学表达式解析器，严格遵循第四部分基本概念的规范。

## 基本概念实现

### 一、带符号整数 (Signed Integers)

实现位置：`parseBigInteger()` 和 `parseSimpleInt()` 方法

- 支持前导 0 的十进制带符号整数
- 正数的正号可以省略
- 无进制标识
- 示例：`+02`、`-16`、`20220928`

**实现细节：**
```java
private BigInteger parseBigInteger() {
    StringBuilder sb = new StringBuilder();
    if (peek() == '+' || peek() == '-') {
        sb.append(consume());
    }
    while (Character.isDigit(peek())) {
        sb.append(consume());
    }
    return new BigInteger(sb.toString());
}
```

### 二、因子 (Factors)

#### 1. 变量因子 (Variable Factors)

实现位置：`parseFactor()` 方法中的 `x` 分支

**一般形式：**
- 由自变量 `x`，指数符号 `^` 和指数组成
- 指数为一个非负带符号整数
- 示例：`x ^ +2`、`x ^ 02`、`x ^ 2`

**省略形式：**
- 当指数为 1 时，可以省略指数符号 `^` 和指数
- 示例：`x`

**实现细节：**
```java
else if (peek() == 'x') {
    consume();
    int exp = 1;
    if (peek() == '^') {
        consume();
        exp = parseSimpleInt();
    }
    res = new Poly(BigInteger.ONE, exp);
}
```

#### 2. 常数因子 (Constant Factors)

实现位置：`parseFactor()` 方法中的默认分支

- 包含一个带符号整数
- 示例：`233`、`-16`

**实现细节：**
```java
else {
    BigInteger val = parseBigInteger();
    res = new Poly(val, 0);
}
```

#### 3. 表达式因子 (Expression Factors)

实现位置：`parseFactor()` 方法中的 `(` 分支

- 用一对小括号包裹起来的表达式
- 可以带指数，且指数为一个非负带符号整数
- 示例：`(x^2 + 2*x + x)^2`

**实现细节：**
```java
if (peek() == '(') {
    consume();
    res = parseExpr();
    consume(); // consume ')'
    if (peek() == '^') {
        consume();
        int exp = parseSimpleInt();
        res = res.pow(exp);
    }
}
```

### 三、项 (Terms)

实现位置：`parseTerm()` 方法

- 由乘法运算符连接若干因子组成
- 在第一个因子之前，可以带一个正号或者负号
- 示例：`x * 02`、`+ x * 02`、`- +3 * x`
- 注意：空串不属于合法的项

**实现细节：**
```java
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
```

### 四、表达式 (Expressions)

实现位置：`parseExpr()` 方法

- 由加法和减法运算符连接若干项组成
- 在第一项之前，可以带一个正号或者负号
- 示例：`-1 + x ^ 233 - x ^ 06 +x`、`- -1 + x ^ 233`、`+ -2 + x ^ 19911226`
- 注意：空串不属于合法的表达式

**实现细节：**
```java
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
```

### 五、空白字符处理

实现位置：`Solver` 构造函数

- 空白字符包含且仅包含空格
- 在解析前，所有空格都被移除

**实现细节：**
```java
public Solver(String input) {
    // 去除空白符
    String s = input.replaceAll("\\s+", "");
    // ...
}
```

## 符号简化

为了简化解析，构造函数还会简化连续的符号：
- `++` → `+`
- `--` → `+`
- `+-` → `-`
- `-+` → `-`

这使得解析器能够正确处理像 `- -1` 和 `+ -2` 这样的表达式。

## 多项式表示和运算

`Poly` 类使用哈希映射存储多项式的系数和指数，支持：
- 加法 (`add`)
- 减法 (`sub`)
- 乘法 (`multiply`)
- 乘方 (`pow`)
- 取反 (`negate`)

输出格式按指数降序排列，并遵循标准数学表示法。

## 测试示例

以下是一些符合规范的测试用例：

```bash
# 带符号整数
echo "+02" | java Main        # 输出: 2
echo "-16" | java Main        # 输出: -16

# 变量因子
echo "x ^ +2" | java Main     # 输出: x^2
echo "x ^ 02" | java Main     # 输出: x^2
echo "x" | java Main          # 输出: x

# 表达式因子
echo "(x^2 + 2*x + x)^2" | java Main  # 输出: x^4+6*x^3+9*x^2

# 项
echo "+ x * 02" | java Main   # 输出: 2*x
echo "- +3 * x" | java Main   # 输出: -3*x

# 表达式
echo "-1 + x ^ 233 - x ^ 06 +x" | java Main  # 输出: x^233-x^6+x-1
echo "- -1 + x ^ 233" | java Main            # 输出: x^233+1
```

## 总结

本实现完全符合第四部分基本概念的所有要求，提供了一个健壮的数学表达式解析器，能够正确处理各种复杂的表达式形式。
