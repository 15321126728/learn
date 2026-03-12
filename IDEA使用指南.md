# IDEA使用指南

## 问题描述

如果您在IDEA中复制Main.java后看到很多报错（红色下划线），这是因为原来的代码将所有类放在一个文件中作为内部类。现在我们已经将代码拆分为多个独立的文件。

## 解决方案

### 方案1：使用多文件版本（推荐）

#### 步骤1：创建Java项目
1. 打开IDEA
2. File → New → Project
3. 选择Java项目
4. 设置项目名称（如：Expression）
5. 点击Create

#### 步骤2：复制所有文件
将以下8个文件复制到项目的src目录：

```
src/
├── Main.java           (主程序)
├── Expr.java          (表达式基类)
├── PolyExpr.java      (多项式)
├── ExpExpr.java       (exp函数)
├── BinaryExpr.java    (二元运算)
├── SelectionExpr.java (选择式)
├── Function.java      (函数定义)
└── Solver.java        (解析器)
```

#### 步骤3：刷新项目
- 在项目视图中右键点击项目根目录
- 选择 "Reload from Disk" 或按 Ctrl+Alt+Y

#### 步骤4：编译运行
1. 打开Main.java
2. 右键点击Main类
3. 选择 "Run 'Main.main()'"

### 方案2：使用单文件版本

如果您更喜欢单文件版本，可以使用 `Main_single_file.java`：

1. 创建新的Java项目
2. 将 `Main_single_file.java` 重命名为 `Main.java`
3. 复制到src目录
4. 运行程序

注意：单文件版本在某些IDEA版本中可能会有警告或错误提示，但通常可以正常运行。

## 验证安装

### 方法1：在IDEA中运行
1. 运行Main.main()
2. 在控制台输入：
   ```
   0
   x+1
   ```
3. 应该看到输出：`x+1`

### 方法2：命令行测试
在项目目录下打开终端：

```bash
# Windows
javac Main.java
echo 0 > test.txt
echo x+1 >> test.txt
java Main < test.txt

# Linux/Mac
javac Main.java
echo -e "0\nx+1" | java Main
```

预期输出：`x+1`

## 常见问题

### Q1: IDEA提示"找不到类"
**A:** 确保所有8个.java文件都在同一个目录（通常是src目录）

### Q2: IDEA提示"包不匹配"
**A:** 确保所有文件都没有package声明（或者都在同一个package中）

### Q3: 编译时提示"class is public, should be declared in a file named..."
**A:** 确保文件名和类名一致：
- Main.java 包含 public class Main
- Expr.java 包含 public abstract class Expr
- 依此类推

### Q4: 运行时找不到主类
**A:** 
1. 右键点击Main.java
2. 选择 "Run 'Main.main()'"
3. 或者在Run/Debug Configuration中设置Main为主类

### Q5: 想使用单文件版本
**A:** 使用 `Main_single_file.java`，将其重命名为 `Main.java` 即可

## 项目结构说明

### 多文件版本结构
```
项目根目录/
├── src/
│   ├── Main.java           (47行) - 主程序
│   ├── Expr.java          (11行) - 抽象基类
│   ├── PolyExpr.java      (193行) - 多项式类
│   ├── ExpExpr.java       (76行) - exp函数类
│   ├── BinaryExpr.java    (60行) - 二元运算类
│   ├── SelectionExpr.java (47行) - 选择式类
│   ├── Function.java      (17行) - 函数类
│   └── Solver.java        (215行) - 解析器类
└── out/
    └── production/
        └── (编译后的.class文件)
```

### 单文件版本结构
```
项目根目录/
├── src/
│   └── Main.java (671行) - 包含所有类
└── out/
    └── production/
        └── (编译后的.class文件)
```

## 功能测试

运行以下测试确认程序正常工作：

### 测试1：基本多项式
```
输入：
0
x+1

输出：
x+1
```

### 测试2：exp函数展开
```
输入：
0
exp(((x+1)^2))

输出：
exp((x^2+2*x+1))
```

### 测试3：exp乘法优化
```
输入：
0
exp(x)*exp((2*x))

输出：
exp((x+2*x))
```

### 测试4：选择式
```
输入：
0
[( ((x-1)^2) == (x^2-2*x+1) ) ? exp(x) : x]

输出：
exp(x)
```

### 测试5：自定义函数
```
输入：
1
f(x) = x^2 + exp(0)
f((x+1))

输出：
x^2+2*x+2
```

## IDEA配置建议

### Java版本
- 推荐：Java 8 或更高
- 设置位置：File → Project Structure → Project SDK

### 编码
- 设置为UTF-8
- 设置位置：File → Settings → Editor → File Encodings
- 确保：
  - Global Encoding: UTF-8
  - Project Encoding: UTF-8
  - Default encoding for properties files: UTF-8

### 编译器
- 使用默认设置即可
- 确保 "Build automatically" 已启用
- 位置：File → Settings → Build, Execution, Deployment → Compiler

## 性能优化

### 启用自动编译
File → Settings → Build, Execution, Deployment → Compiler
→ 勾选 "Build project automatically"

### 增加内存
Help → Edit Custom VM Options
添加或修改：
```
-Xms512m
-Xmx2048m
```

## 调试技巧

### 设置断点
1. 在代码行号左侧点击，设置断点
2. 右键点击Main.java → Debug 'Main.main()'
3. 程序会在断点处暂停

### 查看变量
- 在调试模式下，鼠标悬停在变量上可以查看其值
- 或在Debug窗口的Variables标签中查看所有变量

### 单步执行
- F8: Step Over（执行到下一行）
- F7: Step Into（进入方法内部）
- Shift+F8: Step Out（跳出当前方法）

## 总结

- ✅ 推荐使用多文件版本（8个文件）
- ✅ 确保所有文件在同一个目录
- ✅ 使用UTF-8编码
- ✅ 使用Java 8或更高版本
- ✅ 运行测试用例验证功能

如果还有问题，请查看：
- `多文件版本说明.md` - 详细的文件说明
- `类结构图.md` - 类之间的关系
- `编译问题排查指南.md` - 其他编译问题

---

**更新日期**: 2026-03-12
