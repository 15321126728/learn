# 代码架构文档 (Code Architecture Documentation)

## 目录
1. [系统概览](#系统概览)
2. [架构设计](#架构设计)
3. [核心组件](#核心组件)
4. [设计模式](#设计模式)
5. [数据流](#数据流)
6. [类图与交互](#类图与交互)
7. [关键算法](#关键算法)

---

## 系统概览

### 系统目的
本系统是一个**数学表达式解析器和化简器**，能够：
- 解析复杂的数学表达式（包括变量、常数、括号、指数）
- 将表达式转换为标准多项式形式
- 合并同类项并按降幂排序输出

### 技术栈
- **语言**: Java
- **核心库**: 
  - `java.math.BigInteger` - 支持任意精度整数运算
  - `java.util.HashMap` - 高效存储多项式项
  - `java.util.Scanner` - 标准输入处理

### 输入输出
```
输入: -1 + x ^ 233 - x ^ 06 +x
输出: x^233-x^6+x-1
```

---

## 架构设计

### 整体架构模式

本系统采用**三层架构**：

```
┌─────────────────────────────────────────┐
│         入口层 (Entry Layer)            │
│         Main.main()                     │
│   - 读取用户输入                         │
│   - 创建Solver实例                       │
│   - 输出结果                             │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│       解析层 (Parser Layer)             │
│       Solver 类                         │
│   - 词法分析（tokenization）             │
│   - 语法分析（parsing）                  │
│   - 递归下降解析                         │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│      数据层 (Data Layer)                │
│      Poly 类                            │
│   - 多项式表示                           │
│   - 代数运算（加减乘幂）                  │
│   - 结果格式化                           │
└─────────────────────────────────────────┘
```

### 架构优势
1. **关注点分离**: 每层负责明确的功能
2. **高内聚低耦合**: 层之间通过清晰接口交互
3. **易于测试**: 各组件可独立测试
4. **易于扩展**: 可轻松添加新的运算符或因子类型

---

## 核心组件

### 1. Main 类（入口类）

**职责**: 程序入口，负责I/O操作

```java
public class Main {
    public static void main(String[] args)
}
```

**功能**:
- 从标准输入读取表达式
- 创建Solver实例进行解析
- 输出解析和化简后的结果

**交互**: Main → Solver

---

### 2. Solver 类（解析器）

**职责**: 将字符串表达式解析为多项式对象

#### 2.1 核心字段
```java
private final String expr;  // 预处理后的表达式
private int pos;            // 当前解析位置
```

#### 2.2 核心方法

##### 预处理方法
```java
public Solver(String input)
```
- **功能**: 构造函数，预处理输入
- **步骤**:
  1. 移除所有空白字符
  2. 简化连续符号 (++→+, --→+, +-→-, -+→-)
  
**为什么需要符号简化？**
```
输入: - -1 + x
简化前: --1+x
简化后: +1+x
解析: 正确识别为 1+x
```

##### 核心解析方法

**递归下降解析器** - 三个层次的解析方法：

```
parseExpr()     ← 表达式 (最高层)
    ↓
parseTerm()     ← 项 (中间层)
    ↓
parseFactor()   ← 因子 (最底层)
```

**1. parseExpr() - 解析表达式**
```java
private Poly parseExpr()
```
- **文法**: `Expr → Term { (+|-) Term }`
- **功能**: 处理加法和减法
- **示例**: `x^2 + 2*x - 1`

**2. parseTerm() - 解析项**
```java
private Poly parseTerm()
```
- **文法**: `Term → [Sign] Factor { * Factor }`
- **功能**: 处理乘法和前导符号
- **示例**: `- 3 * x * x`

**3. parseFactor() - 解析因子**
```java
private Poly parseFactor()
```
- **文法**: `Factor → (Expr)^exp | x^exp | Constant`
- **功能**: 处理基本元素
- **类型**:
  - 表达式因子: `(x+1)^2`
  - 变量因子: `x^3` 或 `x`
  - 常数因子: `233`

##### 辅助方法
```java
private char peek()              // 查看当前字符
private char consume()           // 消费当前字符
private int parseSimpleInt()     // 解析非负整数（指数）
private BigInteger parseBigInteger()  // 解析带符号整数（系数）
```

#### 2.3 解析算法特点

**递归下降解析 (Recursive Descent Parsing)**
- 每个文法规则对应一个方法
- 方法递归调用构建解析树
- 自顶向下的解析策略

**LL(1) 语法**
- 向前看1个字符即可决定使用哪个产生式
- 无回溯，效率高

**示例解析过程**:
```
输入: x^2 + 2*x + 1

parseExpr()
  ├─ parseTerm() → x^2
  │   └─ parseFactor() → Poly(1, 2)  // 系数1, 指数2
  │
  ├─ consume('+')
  │
  ├─ parseTerm() → 2*x
  │   ├─ parseFactor() → Poly(2, 0)  // 常数2
  │   ├─ consume('*')
  │   └─ parseFactor() → Poly(1, 1)  // x
  │   └─ multiply() → Poly(2, 1)     // 2*x
  │
  ├─ consume('+')
  │
  └─ parseTerm() → 1
      └─ parseFactor() → Poly(1, 0)

最终: Poly{2: 1, 1: 2, 0: 1} → x^2+2*x+1
```

---

### 3. Poly 类（多项式）

**职责**: 表示和操作多项式

#### 3.1 数据结构
```java
private final Map<Integer, BigInteger> terms;
```

**存储方式**: `指数 → 系数` 映射
```
多项式: 3x^5 - 2x^2 + 7
存储: {5: 3, 2: -2, 0: 7}
```

**优势**:
- O(1) 查找和更新系数
- 自动合并同类项
- 稀疏多项式高效存储

#### 3.2 构造方法
```java
public Poly()                          // 空多项式（零多项式）
public Poly(BigInteger coeff, int exp) // 单项式
```

#### 3.3 代数运算

##### 加法 (add)
```java
public Poly add(Poly other)
```
**算法**: 
1. 复制当前多项式的所有项
2. 遍历另一个多项式，合并同类项
3. 清理零系数项

**复杂度**: O(n + m)

**示例**:
```
(3x^2 + 2x) + (x^2 - x + 1)
= 4x^2 + x + 1
```

##### 减法 (sub)
```java
public Poly sub(Poly other)
```
**实现**: `this.add(other.negate())`
- 先取反，再相加

##### 乘法 (multiply)
```java
public Poly multiply(Poly other)
```
**算法**: 
1. 双重循环遍历两个多项式的所有项
2. 对每对项，指数相加，系数相乘
3. 结果累加到新多项式

**复杂度**: O(n × m)

**数学原理**:
```
(a₁x^n₁ + a₂x^n₂) × (b₁x^m₁ + b₂x^m₂)
= a₁b₁x^(n₁+m₁) + a₁b₂x^(n₁+m₂) + a₂b₁x^(n₂+m₁) + a₂b₂x^(n₂+m₂)
```

##### 乘方 (pow)
```java
public Poly pow(int exp)
```
**算法**: 重复乘法
```java
result = 1
for i = 1 to exp:
    result = result × base
```
**复杂度**: O(exp × n²)

**注意**: 
- exp=0 返回常数1
- exp=1 返回自身（避免不必要计算）

##### 取反 (negate)
```java
public Poly negate()
```
**算法**: 所有系数取负

##### 清理 (cleanup)
```java
private void cleanup()
```
**功能**: 移除系数为0的项
- 保持数据结构简洁
- 避免输出 `0*x^2` 这样的项

#### 3.4 格式化输出

```java
public String toString()
```

**输出规则**:
1. 按指数降序排列: `x^3 + x^2 + x + 1`
2. 首项负号直接显示: `-x^2 + x`
3. 非首项正号显示加号: `x^2+2*x+1`
4. 系数为±1时特殊处理:
   - `1*x` → `x`
   - `-1*x` → `-x`
5. 指数为0显示常数: `+5`
6. 指数为1省略指数: `x` 而非 `x^1`
7. 零多项式显示: `0`

**格式化流程**:
```
1. 提取所有指数并降序排序
2. 遍历每个指数:
   a. 处理符号（首项或非首项）
   b. 处理系数（特殊值或一般值）
   c. 处理变量和指数
3. 拼接成字符串
```

---

## 设计模式

### 1. 递归下降模式 (Recursive Descent Pattern)

**定义**: 每个语法规则对应一个递归方法

**应用**:
```
parseExpr()  → 表达式文法
parseTerm()  → 项文法  
parseFactor() → 因子文法
```

**优点**:
- 代码结构与文法结构一一对应
- 易于理解和维护
- 易于扩展新的语法规则

### 2. 不可变对象模式 (Immutable Object Pattern)

**应用**: Poly类的所有运算都返回新对象

```java
public Poly add(Poly other) {
    Poly res = new Poly();  // 创建新对象
    // ...
    return res;             // 返回新对象
}
```

**优点**:
- 线程安全
- 避免副作用
- 易于推理和调试

### 3. 构建器模式 (Builder Pattern)

**应用**: 通过逐步构建复杂对象

```java
// Poly内部使用Map逐步构建多项式
res.terms.merge(newExp, newCoeff, BigInteger::add);
```

### 4. 策略模式 (Strategy Pattern)

**应用**: 不同类型因子使用不同解析策略

```java
if (peek() == '(') {
    // 表达式因子策略
} else if (peek() == 'x') {
    // 变量因子策略
} else {
    // 常数因子策略
}
```

---

## 数据流

### 完整数据流图

```
┌─────────────┐
│ 用户输入     │
│ "x^2+2*x+1" │
└──────┬──────┘
       │
       ▼
┌──────────────────────┐
│ Main.main()          │
│ Scanner读取          │
└──────┬───────────────┘
       │
       ▼
┌──────────────────────┐
│ Solver构造函数        │
│ 1. 移除空格          │
│    "x^2+2*x+1"       │
│ 2. 简化符号          │
│    "x^2+2*x+1"       │
└──────┬───────────────┘
       │
       ▼
┌──────────────────────┐
│ Solver.solve()       │
│ 调用parseExpr()      │
└──────┬───────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│ 递归解析过程                          │
│                                      │
│ parseExpr()                          │
│   ├─ parseTerm() → x^2               │
│   │    └─ parseFactor()              │
│   │         → Poly{2: 1}             │
│   │                                  │
│   ├─ parseTerm() → 2*x               │
│   │    ├─ parseFactor() → Poly{0: 2} │
│   │    └─ parseFactor() → Poly{1: 1} │
│   │    └─ multiply() → Poly{1: 2}    │
│   │                                  │
│   └─ parseTerm() → 1                 │
│        └─ parseFactor() → Poly{0: 1} │
│                                      │
│ 合并: add(add(Poly{2:1}, Poly{1:2}), │
│           Poly{0:1})                 │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────┐
│ Poly{2:1, 1:2, 0:1}  │
│ toString()           │
│ "x^2+2*x+1"          │
└──────┬───────────────┘
       │
       ▼
┌──────────────────────┐
│ 输出到控制台          │
│ x^2+2*x+1            │
└──────────────────────┘
```

### 数据转换流程

```
字符串 → Token流 → 语法树 → Poly对象 → 字符串
"x+1"     x,+,1    Add(x,1)   {0:1,1:1}  "x+1"
```

---

## 类图与交互

### 类图

```
┌─────────────────────────────┐
│         Main                │
│ (Entry Point)               │
├─────────────────────────────┤
│ + main(String[]): void      │
└──────────┬──────────────────┘
           │ creates
           │
           ▼
┌─────────────────────────────┐
│         Solver              │
│ (Parser)                    │
├─────────────────────────────┤
│ - expr: String              │
│ - pos: int                  │
├─────────────────────────────┤
│ + Solver(String)            │
│ + solve(): String           │
│ - parseExpr(): Poly         │
│ - parseTerm(): Poly         │
│ - parseFactor(): Poly       │
│ - peek(): char              │
│ - consume(): char           │
│ - parseSimpleInt(): int     │
│ - parseBigInteger(): BigInt │
└──────────┬──────────────────┘
           │ uses
           │
           ▼
┌─────────────────────────────┐
│         Poly                │
│ (Polynomial)                │
├─────────────────────────────┤
│ - terms: Map<Int,BigInt>    │
├─────────────────────────────┤
│ + Poly()                    │
│ + Poly(BigInt, int)         │
│ + add(Poly): Poly           │
│ + sub(Poly): Poly           │
│ + multiply(Poly): Poly      │
│ + pow(int): Poly            │
│ + negate(): Poly            │
│ + toString(): String        │
│ - cleanup(): void           │
└─────────────────────────────┘
```

### 交互时序图

```
用户   Main    Solver              Poly
 │      │        │                  │
 │──①──>│        │                  │  输入表达式
 │      │──②────>│                  │  创建Solver
 │      │        │──③──────────────>│  创建Poly对象
 │      │        │<─④──────────────│  返回Poly
 │      │        │──⑤──────────────>│  调用add/multiply
 │      │        │<─⑥──────────────│  返回新Poly
 │      │        │──⑦──────────────>│  调用toString
 │      │        │<─⑧──────────────│  返回字符串
 │      │<─⑨────│                  │  返回结果
 │<─⑩──│        │                  │  输出结果
 │      │        │                  │
```

---

## 关键算法

### 1. 符号简化算法

**目的**: 将连续符号转换为单一符号

```java
while (changed) {
    original = s;
    s = s.replace("++", "+")
         .replace("--", "+")
         .replace("+-", "-")
         .replace("-+", "-");
    changed = !s.equals(original);
}
```

**数学依据**:
- `++` → `+` (正正得正)
- `--` → `+` (负负得正)
- `+-` → `-` (正负得负)
- `-+` → `-` (负正得负)

**必须迭代的原因**:
```
原始: "---1"
第1轮: "--1"  (---变--)
第2轮: "+1"   (--变+)
```

### 2. 递归下降解析算法

**伪代码**:
```
parseExpr():
    result = parseTerm()
    while peek() in {'+', '-'}:
        op = consume()
        right = parseTerm()
        if op == '+':
            result = result.add(right)
        else:
            result = result.sub(right)
    return result

parseTerm():
    sign = parseSign()
    result = parseFactor()
    if sign < 0:
        result = result.negate()
    while peek() == '*':
        consume()
        right = parseFactor()
        result = result.multiply(right)
    return result

parseFactor():
    if peek() == '(':
        return parseParenExpr()
    elif peek() == 'x':
        return parseVariable()
    else:
        return parseConstant()
```

### 3. 多项式乘法算法

**数学公式**:
```
P(x) = Σ aᵢx^i
Q(x) = Σ bⱼx^j
P(x)×Q(x) = Σ Σ (aᵢbⱼ)x^(i+j)
```

**实现**:
```java
for (entry1 in poly1.terms):
    for (entry2 in poly2.terms):
        newExp = entry1.exp + entry2.exp
        newCoeff = entry1.coeff × entry2.coeff
        result.terms[newExp] += newCoeff
```

**时间复杂度**: O(n×m)
- n: 第一个多项式的项数
- m: 第二个多项式的项数

### 4. 同类项合并算法

**使用HashMap的merge方法**:
```java
result.terms.merge(exp, coeff, BigInteger::add)
```

**等价于**:
```java
if (result.terms.containsKey(exp)):
    result.terms[exp] += coeff
else:
    result.terms[exp] = coeff
```

**效率**: O(1) 查找和插入

---

## 性能分析

### 时间复杂度

| 操作 | 复杂度 | 说明 |
|------|--------|------|
| 预处理 | O(n) | n为输入长度 |
| 解析 | O(n) | 线性扫描 |
| 加法 | O(n+m) | n,m为项数 |
| 乘法 | O(n×m) | 双重循环 |
| 乘方 | O(k×n²) | k为指数 |
| 格式化 | O(n log n) | 排序主导 |

### 空间复杂度

| 结构 | 复杂度 | 说明 |
|------|--------|------|
| Solver | O(n) | 存储输入字符串 |
| Poly | O(n) | n为非零项数 |
| 递归栈 | O(d) | d为嵌套深度 |

### 优化策略

1. **稀疏表示**: 使用HashMap只存储非零项
2. **BigInteger**: 支持任意精度，避免溢出
3. **不可变性**: 避免防御性复制
4. **短路求值**: pow(1)直接返回

---

## 扩展性分析

### 易于扩展的方面

1. **新运算符**
   - 在parseExpr或parseTerm中添加新case
   - 在Poly中添加对应方法

2. **新类型因子**
   - 在parseFactor中添加新分支
   - 例如：sin(x), cos(x), e^x

3. **多变量**
   - 修改Poly的key为Map<String, Integer>
   - 支持x, y, z等多个变量

### 限制

1. **单变量**: 只支持变量x
2. **多项式**: 不支持有理函数或三角函数
3. **整数系数**: 不支持分数或小数系数

---

## 总结

### 架构特点

✅ **模块化设计**: 清晰的三层架构  
✅ **递归优雅**: 文法与代码结构对应  
✅ **高效存储**: HashMap实现稀疏多项式  
✅ **精确计算**: BigInteger避免溢出  
✅ **易于测试**: 各组件独立可测试  
✅ **易于扩展**: 符合开闭原则  

### 设计亮点

1. **预处理**: 符号简化提高解析鲁棒性
2. **递归下降**: 直观清晰的解析策略
3. **不可变性**: Poly对象不可变，安全可靠
4. **智能格式化**: 输出符合数学习惯

### 适用场景

- 计算机代数系统
- 符号数学计算
- 表达式化简工具
- 教学演示系统

---

## 参考资料

### 相关文档
- [IMPLEMENTATION.md](IMPLEMENTATION.md) - 实现细节
- [USAGE.md](USAGE.md) - 使用指南
- [SUMMARY.md](SUMMARY.md) - 完成总结

### 相关理论
- 编译原理 - 递归下降解析
- 数据结构 - 哈希表应用
- 计算机代数 - 多项式运算
