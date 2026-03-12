# 代码注释中文化说明

## 更新内容

本次更新将Main.java中的所有英文注释翻译成了中文，以便中文用户更好地理解代码。

## 翻译的注释类别

### 1. 主方法注释
- 读取自定义函数的个数
- 如果存在函数定义，则读取
- 读取待求值的表达式
- 解析并求解
- 移除不必要的外层括号

### 2. 表达式类注释
- **Expr（基类）**：所有表达式类型的基类
- **PolyExpr**：多项式表达式（项的和）
- **ExpExpr**：指数表达式 exp(expr)
- **BinaryExpr**：二元运算表达式
- **SelectionExpr**：选择式表达式 [(A==B)?C:D]

### 3. 函数和解析器注释
- **Function**：函数定义
- **Solver**：解析器/求解器

## 关键注释翻译示例

### 原英文注释：
```java
// Read number of function definitions
// Base class for all expression types
// Check if two expressions are equal by checking if their difference is zero
// Handle 0^0 = 1 edge case
// exp(0) = 1
// For exp expressions, we can combine: exp(a) * exp(b) = exp(a+b)
```

### 翻译后的中文注释：
```java
// 读取自定义函数的个数
// 所有表达式类型的基类
// 通过检查两个表达式的差是否为零来判断它们是否相等
// 处理0^0 = 1的边界情况
// exp(0) = 1
// 对于exp表达式，我们可以合并：exp(a) * exp(b) = exp(a+b)
```

## 代码功能保持不变

所有翻译仅涉及注释内容，代码逻辑完全未改动。经过测试，所有功能正常运行：

### 测试用例验证
1. ✅ `exp(((x+1)^2))` → `exp((x^2+2*x+1))`
2. ✅ `exp(x)*exp((2*x))` → `exp((x+2*x))`
3. ✅ `[( ((x-1)^2) == (x^2-2*x+1) ) ? exp(x) : x]` → `exp(x)`
4. ✅ `f(x) = x^2 + exp(0); f((x+1))` → `x^2+2*x+2`
5. ✅ `[(( [(x == x) ? 1 : 0] ) == 1) ? exp(x) : 0]` → `exp(x)`

## 维护建议

未来在修改或添加代码时，请保持中文注释的风格，以确保代码库的一致性和可读性。

---

**更新日期：** 2026-03-12
