#!/bin/bash

echo "Test 1: Basic exp"
echo "0" | java Main
echo "exp(x)" | java Main
echo ""

echo "Test 2: Exp with constant"
echo "0" | java Main
echo "exp((2*x))" | java Main
echo ""

echo "Test 3: Multiple exp terms"
echo "0" | java Main
echo "exp(x)+exp(x)" | java Main
echo ""

echo "Test 4: Selection with constants"
echo "0" | java Main
echo "[(1 == 1) ? x : 0]" | java Main
echo ""

echo "Test 5: Function with multiple terms"
echo "1" | java Main
echo "f(x) = x^2 + 2*x + 1" | java Main
echo "f(x)" | java Main
echo ""

echo "Test 6: Nested function call"
echo "1" | java Main
echo "f(x) = x^2" | java Main
echo "f((x+1))" | java Main
