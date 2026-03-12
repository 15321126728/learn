# Test Cases for Iteration 2

## Test Case 1: exp with nested expression factor
Input:
```
0
exp(((x+1)^2))
```
Expected Output: `exp((x^2+2*x+1))`

## Test Case 2: exp multiplication optimization
Input:
```
0
exp(x)*exp((2*x))
```
Expected Output: `exp((x+2*x))`

## Test Case 3: Selection factor - condition true
Input:
```
0
[( ((x-1)^2) == (x^2-2*x+1) ) ? exp(x) : x]
```
Expected Output: `exp(x)`

## Test Case 4: Selection factor - condition false, exp(0)
Input:
```
0
[(x == (x+1)) ? 1 : exp(0)]
```
Expected Output: `1`

## Test Case 5: Function definition and call
Input:
```
1
f(x) = x^2 + exp(0)
f((x+1))
```
Expected Output: `x^2+2*x+2`

## Test Case 6: Nested selection factors
Input:
```
0
[(( [(x == x) ? 1 : 0] ) == 1) ? exp(x) : 0]
```
Expected Output: `exp(x)`

## Test Case 7: Function with expression factor
Input:
```
1
f(x) = (x+1)^2
f((x+1))
```
Expected Output: `x^2+2*x+1`

## Test Case 8: Complex exp expression
Input:
```
0
exp((x^2+1))
```
Expected Output: `exp((x^2+1))`

## Test Case 9: Selection with polynomial expansion
Input:
```
0
[((x+1) == (x+1)) ? (x+1)^2 : 0]
```
Expected Output: `x^2+2*x+1`

## Test Case 10: Function call with power
Input:
```
1
f(x) = x
f((x^2))
```
Expected Output: `x^2`
