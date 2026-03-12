#!/bin/bash

# 编译和测试脚本
# 用于验证Main.java是否可以正常编译和运行

echo "================================"
echo "Main.java 编译测试脚本"
echo "================================"
echo ""

# 检查Java是否安装
echo "1. 检查Java环境..."
if ! command -v javac &> /dev/null; then
    echo "❌ 错误：未找到javac命令。请确保已安装JDK。"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到java命令。请确保已安装JDK。"
    exit 1
fi

echo "✅ Java环境检查通过"
java -version
echo ""

# 清理旧的编译文件
echo "2. 清理旧的编译文件..."
rm -f Main.class *\$*.class
echo "✅ 清理完成"
echo ""

# 编译Main.java
echo "3. 编译Main.java..."
javac -encoding UTF-8 Main.java 2>&1
if [ $? -ne 0 ]; then
    echo "❌ 编译失败！请检查上面的错误信息。"
    exit 1
fi
echo "✅ 编译成功"
echo ""

# 运行测试用例
echo "4. 运行测试用例..."
echo ""

# 测试1
echo "测试1: 基本表达式"
echo "输入: 0 和 x+1"
result=$(echo -e "0\nx+1" | java Main)
expected="x+1"
if [ "$result" = "$expected" ]; then
    echo "✅ 通过：$result"
else
    echo "❌ 失败：预期 '$expected'，实际 '$result'"
fi
echo ""

# 测试2
echo "测试2: exp函数展开"
echo "输入: 0 和 exp(((x+1)^2))"
result=$(echo -e "0\nexp(((x+1)^2))" | java Main)
expected="exp((x^2+2*x+1))"
if [ "$result" = "$expected" ]; then
    echo "✅ 通过：$result"
else
    echo "❌ 失败：预期 '$expected'，实际 '$result'"
fi
echo ""

# 测试3
echo "测试3: exp乘法优化"
echo "输入: 0 和 exp(x)*exp((2*x))"
result=$(echo -e "0\nexp(x)*exp((2*x))" | java Main)
expected="exp((x+2*x))"
if [ "$result" = "$expected" ]; then
    echo "✅ 通过：$result"
else
    echo "❌ 失败：预期 '$expected'，实际 '$result'"
fi
echo ""

# 测试4
echo "测试4: 选择式因子（条件为真）"
echo "输入: 0 和 [( ((x-1)^2) == (x^2-2*x+1) ) ? exp(x) : x]"
result=$(echo -e "0\n[( ((x-1)^2) == (x^2-2*x+1) ) ? exp(x) : x]" | java Main)
expected="exp(x)"
if [ "$result" = "$expected" ]; then
    echo "✅ 通过：$result"
else
    echo "❌ 失败：预期 '$expected'，实际 '$result'"
fi
echo ""

# 测试5
echo "测试5: 自定义函数调用"
echo "输入: 1, f(x) = x^2 + exp(0) 和 f((x+1))"
result=$(echo -e "1\nf(x) = x^2 + exp(0)\nf((x+1))" | java Main)
expected="x^2+2*x+2"
if [ "$result" = "$expected" ]; then
    echo "✅ 通过：$result"
else
    echo "❌ 失败：预期 '$expected'，实际 '$result'"
fi
echo ""

echo "================================"
echo "所有测试完成！"
echo "================================"
echo ""
echo "如果所有测试都通过，说明代码完全正常。"
echo "如果在您的环境中编译失败，请查看'编译问题排查指南.md'。"
