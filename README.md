# Calculator-Android-App
APK Available   

Calculator App that uses the shunting yard algorithm to change input from the user into reverse polish notation, and calculates the result based on the RPN.   

Supports addition, subtraction, multiplication, division, and modulo operations. Also has parenthesis function if user wants more complex calculations  

No negative sign yet, but can work around that by subtracting a number inside parenthesis, ex: -(2) or -(2+1).   
A lot of error checking was implemented, such as preventing divide by zero, disallowing consecutive operators (++, or *-+, etc)



TO DO: still crashes in the rare case of nesting too many parenthesis and trying to negate them   
should implement an actual sign changing function instead. some code is already there but commented out
