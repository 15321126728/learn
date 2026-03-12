@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 编译和测试脚本（Windows版）
REM 用于验证Main.java是否可以正常编译和运行

echo ================================
echo Main.java 编译测试脚本
echo ================================
echo.

REM 检查Java是否安装
echo 1. 检查Java环境...
where javac >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未找到javac命令。请确保已安装JDK。
    pause
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未找到java命令。请确保已安装JDK。
    pause
    exit /b 1
)

echo ✅ Java环境检查通过
java -version
echo.

REM 清理旧的编译文件
echo 2. 清理旧的编译文件...
del /q Main.class 2>nul
del /q Main$*.class 2>nul
echo ✅ 清理完成
echo.

REM 编译Main.java
echo 3. 编译Main.java...
javac -encoding UTF-8 Main.java 2>&1
if errorlevel 1 (
    echo ❌ 编译失败！请检查上面的错误信息。
    pause
    exit /b 1
)
echo ✅ 编译成功
echo.

REM 运行测试用例
echo 4. 运行测试用例...
echo.

REM 测试1
echo 测试1: 基本表达式
echo 输入: 0 和 x+1
(
echo 0
echo x+1
) | java Main > temp_output.txt 2>&1
set /p result=<temp_output.txt
if "!result!"=="x+1" (
    echo ✅ 通过：!result!
) else (
    echo ❌ 失败：预期 'x+1'，实际 '!result!'
)
echo.

REM 测试2
echo 测试2: exp函数展开
echo 输入: 0 和 exp^^^(^^^(^^^(x+1^^^)^^^^2^^^)^^^)
(
echo 0
echo exp(((x+1)^2))
) | java Main > temp_output.txt 2>&1
set /p result=<temp_output.txt
set expected=exp((x^2+2*x+1))
if "!result!"=="exp((x^2+2*x+1))" (
    echo ✅ 通过：!result!
) else (
    echo ❌ 失败：预期 'exp((x^2+2*x+1))'，实际 '!result!'
)
echo.

REM 测试3
echo 测试3: exp乘法优化
echo 输入: 0 和 exp^^^(x^^^)*exp^^^(^^^(2*x^^^)^^^)
(
echo 0
echo exp(x)*exp((2*x))
) | java Main > temp_output.txt 2>&1
set /p result=<temp_output.txt
if "!result!"=="exp((x+2*x))" (
    echo ✅ 通过：!result!
) else (
    echo ❌ 失败：预期 'exp((x+2*x))'，实际 '!result!'
)
echo.

REM 测试4
echo 测试4: 自定义函数调用
echo 输入: 1, f^^^(x^^^) = x^^^^2 + exp^^^(0^^^) 和 f^^^(^^^(x+1^^^)^^^)
(
echo 1
echo f(x^) = x^2 + exp(0^)
echo f((x+1^)^)
) | java Main > temp_output.txt 2>&1
set /p result=<temp_output.txt
if "!result!"=="x^2+2*x+2" (
    echo ✅ 通过：!result!
) else (
    echo ❌ 失败：预期 'x^2+2*x+2'，实际 '!result!'
)
echo.

REM 清理临时文件
del temp_output.txt 2>nul

echo ================================
echo 所有测试完成！
echo ================================
echo.
echo 如果所有测试都通过，说明代码完全正常。
echo 如果在您的环境中编译失败，请查看'编译问题排查指南.md'。
echo.
pause
