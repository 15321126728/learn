# 表达式解析和化简程序

这是一个用Java编写的表达式解析和化简程序，支持多项式、指数函数exp()、自定义函数和选择式因子。

## ⚠️ 重要提示

本项目提供**两个版本**：
1. **多文件版本**（推荐在IDEA中使用）- 8个独立的.java文件
2. **单文件版本**（Main_single_file.java）- 适合命令行使用

如果您在IDEA中遇到编译错误，请查看：
- **[IDEA使用指南.md](IDEA使用指南.md)** ⭐ 最重要！
- **[文件清单.md](文件清单.md)** - 了解需要哪些文件

## 🚀 快速开始

### 方法1：在IDEA中使用（推荐）

1. 创建新的Java项目
2. 复制以下8个文件到src目录：
   - Main.java
   - Expr.java
   - PolyExpr.java
   - ExpExpr.java
   - BinaryExpr.java
   - SelectionExpr.java
   - Function.java
   - Solver.java
3. 运行Main.main()

详细步骤见：**[IDEA使用指南.md](IDEA使用指南.md)**

### 方法2：命令行使用

```bash
# 多文件版本
javac Main.java
java Main

# 单文件版本
javac Main_single_file.java -d . && mv Main_single_file.class Main.class
java Main
```

### 方法3：验证代码

#### Windows用户
双击运行 `test.bat` 文件

#### Linux/Mac用户
```bash
./test.sh
```

如果所有测试显示 ✅，说明代码完全正常！

## 📚 文档

### 核心文档（必读）
- **[IDEA使用指南.md](IDEA使用指南.md)** ⭐ 在IDEA中使用的详细步骤
- **[文件清单.md](文件清单.md)** - 所有文件的完整清单
- **[多文件版本说明.md](多文件版本说明.md)** - 为什么拆分文件
- **[类结构图.md](类结构图.md)** - 代码结构和类关系

### 其他文档
- **[快速使用指南.md](快速使用指南.md)** - 完整的使用说明和示例
- **[编译问题排查指南.md](编译问题排查指南.md)** - 遇到编译错误时查看
- **[COMMENTS_TRANSLATION.md](COMMENTS_TRANSLATION.md)** - 代码注释翻译说明
- **[SUMMARY_CN.md](SUMMARY_CN.md)** - 项目中文总结

## ⚡ 快速示例

### 示例1：多项式化简
```
输入：
0
x^2 + 2*x + 1

输出：
x^2+2*x+1
```

### 示例2：exp函数展开
```
输入：
0
exp(((x+1)^2))

输出：
exp((x^2+2*x+1))
```

### 示例3：自定义函数
```
输入：
1
f(x) = x^2 + exp(0)
f((x+1))

输出：
x^2+2*x+2
```

## 🛠️ 功能特性

- ✅ 多项式运算（加、减、乘、幂）
- ✅ 指数函数 exp(x)
- ✅ exp乘法自动合并：exp(a) * exp(b) → exp(a+b)
- ✅ 自定义函数定义和调用
- ✅ 选择式因子：[(A==B)?C:D]
- ✅ 表达式化简
- ✅ 中文注释

## 🔧 测试工具

项目提供了自动测试脚本：

- `test.sh` - Linux/Mac自动测试脚本
- `test.bat` - Windows自动测试脚本

这些脚本会：
1. 检查Java环境
2. 编译代码
3. 运行5个测试用例
4. 显示测试结果

## ❓ 常见问题

### Q: 在IDEA中有很多编译错误怎么办？
A: 使用多文件版本！查看 **[IDEA使用指南.md](IDEA使用指南.md)**

### Q: 需要复制哪些文件到IDEA？
A: 8个.java文件。详见 **[文件清单.md](文件清单.md)**

### Q: 编译时出现"非法的表达式开始"错误
A: 请查看 [编译问题排查指南.md](编译问题排查指南.md)

### Q: 如何确认代码是否正常？
A: 运行 `test.sh` 或 `test.bat`

### Q: 支持哪些Java版本？
A: 建议使用Java 8或更高版本

### Q: 单文件版本和多文件版本有什么区别？
A: 功能完全相同。多文件版本更适合IDEA，单文件版本更适合命令行。

## 📖 更多信息

详细的使用说明和问题排查，请查看：
- [快速使用指南.md](快速使用指南.md)
- [编译问题排查指南.md](编译问题排查指南.md)

## 📝 许可证

MIT License

---

**最后更新**: 2026-03-12
