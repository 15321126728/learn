# 迭代2架构说明 (Iteration 2 Architecture Documentation)

## 概述 (Overview)

本次迭代在原有多项式解析器的基础上，新增了以下功能：
- 指数函数 exp(factor)
- 选择式因子 [(A==B)?C:D]
- 自定义函数定义和调用

## 新架构设计 (New Architecture Design)

### 表达式类型系统 (Expression Type System)

```
                    Expr (抽象基类)
                      |
        +-------------+-------------+-------------+
        |             |             |             |
    PolyExpr      ExpExpr      BinaryExpr   SelectionExpr
   (多项式)      (指数函数)    (二元运算)    (选择式)
```

#### 1. Expr - 抽象基类

所有表达式类型的基类，定义了三个核心方法：

```java
abstract class Expr {
    abstract Expr simplify();           // 化简表达式
    abstract boolean isZero();          // 判断是否为零
    abstract Expr substitute(var, val); // 变量替换
    boolean equals(Expr other);         // 表达式相等性判断
}
```

#### 2. PolyExpr - 多项式表达式

表示标准多项式，使用 `Map<Integer, BigInteger>` 存储指数→系数映射。

**功能：**
- 多项式加减乘运算
- 幂运算
- 变量替换（支持复杂表达式替换）

**示例：**
```
3*x^2 + 2*x + 1
存储为: {2: 3, 1: 2, 0: 1}
```

#### 3. ExpExpr - 指数函数表达式

表示指数函数 exp(arg)^power。

**关键特性：**
- exp(0) 自动化简为 1
- exp((x+1)^2) 自动展开为 exp((x^2+2*x+1))
- exp(a) * exp(b) 自动合并为 exp(a+b)

**输出格式：**
- 简单参数：`exp(x)`
- 复杂表达式：`exp((x^2+2*x+1))`
- 带指数：`exp(x)^2`

#### 4. BinaryExpr - 二元运算表达式

表示二元运算 left op right，其中 op 可以是 +, -, *。

**用途：**
- 中间计算结果
- 不能化简为多项式的表达式（如 exp(x)+1）
- 函数替换的中间形式

**化简规则：**
- 两个 PolyExpr 运算 → 化简为 PolyExpr
- exp * exp → 合并为单个 exp
- 其他保持 BinaryExpr 形式

#### 5. SelectionExpr - 选择式表达式

表示条件选择 [(cond1 == cond2) ? trueExpr : falseExpr]。

**求值规则：**
1. 计算 cond1 - cond2
2. 判断结果是否恒等于 0
3. 如果是，返回 trueExpr；否则返回 falseExpr

**示例：**
```
[((x^2) == (x*x)) ? 1 : 0]  // x^2 - x*x = 0, 返回 1
[(x == (x+1)) ? 1 : 0]       // x - (x+1) = -1 ≠ 0, 返回 0
```

### 函数系统 (Function System)

#### Function 类

存储自定义函数的定义。

```java
class Function {
    String name;      // 函数名 (固定为 "f")
    String param;     // 形参 (固定为 "x")
    Expr body;        // 函数体表达式
    
    Expr call(Expr arg);  // 调用函数，进行替换和化简
}
```

**替换机制：**
1. 将函数体中所有 x 替换为实参
2. 对替换后的表达式进行化简
3. 返回化简结果

**示例：**
```
定义: f(x) = x^2 + 2*x + 1
调用: f((x+1))
步骤:
  1. 替换: (x+1)^2 + 2*(x+1) + 1
  2. 展开: x^2+2*x+1 + 2*x+2 + 1
  3. 化简: x^2+4*x+4
```

### 解析器增强 (Parser Enhancements)

#### 多行输入处理

```java
1. 读取 n (函数定义个数)
2. 如果 n=1，读取函数定义
3. 读取待求值表达式
```

#### 新增因子类型解析

**exp 函数：**
```
exp(factor)^power
```
- 解析 "exp" 关键字
- 解析参数（必须是因子）
- 可选的指数部分

**选择式因子：**
```
[(factor1 == factor2) ? factor3 : factor4]
```
- 解析条件比较
- 解析真/假两个分支
- 求值时进行相等性判断

**函数调用：**
```
f(factor)
```
- 解析函数名
- 解析参数（必须是因子）
- 进行替换和化简

## 关键算法 (Key Algorithms)

### 1. 表达式相等性判断

```java
boolean equals(Expr e1, Expr e2) {
    Expr diff = e1 - e2;
    return diff.simplify().isZero();
}
```

**原理：** 两个表达式相等当且仅当它们的差为 0。

### 2. 变量替换算法

**PolyExpr 替换：**

对于多项式 P(x) = Σ aᵢ·x^i，替换 x 为 e：
```
P(e) = Σ aᵢ·e^i
```

**非多项式参数处理：**
- 如果 e 不是 PolyExpr，构建表达式树
- 例如：x^2 替换为 exp(x)，得到 exp(x)*exp(x)

### 3. exp 优化算法

**合并规则：**
```
exp(a) * exp(b) = exp(a + b)
```

**实现：**
```java
if (left instanceof ExpExpr && right instanceof ExpExpr) {
    return new ExpExpr(left.arg + right.arg, 1);
}
```

## 数据流 (Data Flow)

```
输入字符串
    ↓
【预处理】去除空格、简化符号
    ↓
【解析】构建 Expr 树
    ↓
【求值】处理选择式、函数调用
    ↓
【化简】展开括号、合并同类项
    ↓
【格式化】生成输出字符串
    ↓
输出结果
```

## 输出格式规范 (Output Format)

### 必要括号 (Necessary Parentheses)

1. **exp 的参数括号：** 始终保留
   ```
   exp(x)         // 必要
   exp((x+1))     // 外层必要，内层仅当参数是表达式时必要
   ```

2. **表达式因子括号：** 当 exp 参数是多项式时必要
   ```
   exp((x^2+2*x+1))  // 必要
   exp(x^2)          // 不必要，应输出 exp((x^2))
   ```

3. **外层括号：** 自动移除
   ```
   (exp(x)+1)  → exp(x)+1
   ```

## 性能优化 (Performance Optimizations)

1. **exp 合并：** exp(a)*exp(b) → exp(a+b)
2. **常数化简：** exp(0) → 1
3. **幂运算优化：** 0^0 → 1
4. **提前终止：** 选择式求值后立即返回结果

## 测试用例 (Test Cases)

参见 `TEST_CASES_V2.md` 文件，包含所有功能的测试用例。

## 扩展性 (Extensibility)

### 易于扩展

1. **新函数类型：** 添加新的 Expr 子类
2. **多变量支持：** 修改 PolyExpr 的键类型
3. **新运算符：** 在 BinaryExpr 中添加新操作

### 当前限制

1. 只支持单个自定义函数 f(x)
2. 不支持函数递归定义
3. exp 参数必须是因子，不能是表达式

---

**最后更新：** 2026-03-12
