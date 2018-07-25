package com.brian.calculator;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView mTextViewResult;
    private TextView mTextViewEntry;

    // other member variables
    private static final int ENTRY_LIMIT = 70;
    private StringBuilder mCurrentEntry;
    private Stack<String> mStack; // for Shunting Yard Algorithm and Reverse Polish Notation
    private Queue<String> mQueue;
    private int mHasParens; // prevent off balance parenthesis
    private boolean mHasDecimal; // prevent more than one decimal in a number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // lock rotation

        mCurrentEntry = new StringBuilder("");
        mStack = new Stack<>();
        mQueue = new LinkedList<>();

        // initialize text views
        mTextViewResult = findViewById(R.id.text_view_result);
        mTextViewEntry = findViewById(R.id.text_view_entry);
        clear();

        // initialize number buttons
        Button mButton0 = findViewById(R.id.button_0);
        Button mButton1 = findViewById(R.id.button_1);
        Button mButton2 = findViewById(R.id.button_2);
        Button mButton3 = findViewById(R.id.button_3);
        Button mButton4 = findViewById(R.id.button_4);
        Button mButton5 = findViewById(R.id.button_5);
        Button mButton6 = findViewById(R.id.button_6);
        Button mButton7 = findViewById(R.id.button_7);
        Button mButton8 = findViewById(R.id.button_8);
        Button mButton9 = findViewById(R.id.button_9);

        // initialize operator buttons
        Button mButtonAddition = findViewById(R.id.button_addition);
        Button mButtonSubtraction = findViewById(R.id.button_subtraction);
        Button mButtonMultiplication = findViewById(R.id.button_multiplication);
        Button mButtonDivision = findViewById(R.id.button_division);
        Button mButtonModulo = findViewById(R.id.button_modulo);

        // initialize remaining function buttons
        Button mButtonEquals = findViewById(R.id.button_equals);
        Button mButtonDecimal = findViewById(R.id.button_decimal);
        Button mButtonParenthesis = findViewById(R.id.button_parenthesis);
        Button mButtonDelete = findViewById(R.id.button_delete);
        Button mButtonClear = findViewById(R.id.button_clear);


        // set listeners for number buttons
        mButton0.setOnClickListener(this);
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        mButton3.setOnClickListener(this);
        mButton4.setOnClickListener(this);
        mButton5.setOnClickListener(this);
        mButton6.setOnClickListener(this);
        mButton7.setOnClickListener(this);
        mButton8.setOnClickListener(this);
        mButton9.setOnClickListener(this);

        // set listeners for operator buttons
        mButtonAddition.setOnClickListener(this);
        mButtonSubtraction.setOnClickListener(this);
        mButtonMultiplication.setOnClickListener(this);
        mButtonDivision.setOnClickListener(this);
        mButtonModulo.setOnClickListener(this);

        // set listeners for remaining function buttons
        mButtonEquals.setOnClickListener(this);
        mButtonDecimal.setOnClickListener(this);
        mButtonParenthesis.setOnClickListener(this);
        mButtonDelete.setOnClickListener(this);
        mButtonClear.setOnClickListener(this);
    }

    // initialize on click listeners
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_0:
                // do not allow divide by zero
                if (mCurrentEntry.length() > 0 && mCurrentEntry.charAt(mCurrentEntry.length()-1) == '/')
                    Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show();
                else
                    inputNumber('0');
                break;
            case R.id.button_1:
                inputNumber('1');
                break;
            case R.id.button_2:
                inputNumber('2');
                break;
            case R.id.button_3:
                inputNumber('3');
                break;
            case R.id.button_4:
                inputNumber('4');
                break;
            case R.id.button_5:
                inputNumber('5');
                break;
            case R.id.button_6:
                inputNumber('6');
                break;
            case R.id.button_7:
                inputNumber('7');
                break;
            case R.id.button_8:
                inputNumber('8');
                break;
            case R.id.button_9:
                inputNumber('9');
                break;

            case R.id.button_addition:
                inputOperator(" +");
                break;
            case R.id.button_subtraction:
                inputOperator(" -");
                break;
            case R.id.button_multiplication:
                inputOperator(" *");
                break;
            case R.id.button_division:
                inputOperator(" /");
                break;
            case R.id.button_modulo:
                inputOperator(" %");
                break;

            case R.id.button_equals:
                if (!calculate())
                    Toast.makeText(this, "Invalid operation", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_decimal:
                inputDecimal();
                break;
            case R.id.button_parenthesis:
                inputParenthesis();
                break;
            case R.id.button_delete:
                deleteInput();
                break;
            case R.id.button_clear:
                clear();
                break;
        }

        // after any input, check input string if its over capacity
        // we still want the calculation result to still somewhat fit on the screen
        if (mCurrentEntry.length() > ENTRY_LIMIT) {
            mCurrentEntry.setLength(ENTRY_LIMIT);
            Toast.makeText(this, "Please shorten the input", Toast.LENGTH_SHORT).show();
        }
    }

    // shunting yard algorithm to turn calculator entry string into reverse polish notation
    private void produceReversePolish() {
        int pos = 0;
        while (pos < mCurrentEntry.length()) {
            // process numbers
            if (isNumber(pos)) {
                pos = getNumber(pos);
            }
            // process operators
            else if (isOperator(pos)) {
                if (mStack.isEmpty())
                    mStack.push("" + mCurrentEntry.charAt(pos));
                else {
                    checkOrder(pos);
                    mStack.push("" + mCurrentEntry.charAt(pos));
                }
            }
            // opening parenthesis go straight on the stack
            else if (mCurrentEntry.charAt(pos) == '(') {
                mStack.push("" + mCurrentEntry.charAt(pos));
            }
            // process closed parens: enqueue everything off the stack until we hit an open paren
            else if (mCurrentEntry.charAt(pos) == ')') {
                while (!mStack.peek().equals("(")) {
                    mQueue.add(mStack.peek());
                    mStack.pop();
                }
                mStack.pop(); // remove opening parenthesis
            }
            ++pos;
        }

        // put any remaining on the stack into the queue
        while (!mStack.isEmpty()) {
            mQueue.add(mStack.peek());
            mStack.pop();
        }
    }

    // parse the reverse polish notation to calculate the answer
    private String processReversePolish() {
        while (!mQueue.isEmpty()) {
            String token = mQueue.peek();
            if (token.equals("+") || token.equals("-")
                    || token.equals("*") || token.equals("/") || token.equals("%")) {
                mQueue.remove();
                // get the top 2 numbers from the stack
                double right = Double.parseDouble(mStack.peek());
                mStack.pop();
                double left = Double.parseDouble(mStack.peek());
                mStack.pop();
                // operate on the top two numbers and add the result back onto stack
                switch (token) {
                    case "+":
                        mStack.add("" + (left + right));
                        break;
                    case "-":
                        mStack.add("" + (left - right));
                        break;
                    case "*":
                        mStack.add("" + (left * right));
                        break;
                    case "/":
                        mStack.add("" + (left / right));
                        break;
                    case "%":
                        mStack.add("" + (left % right));
                        break;
                }
            }
            else { // numbers go straight into the stack
                mStack.add(mQueue.peek());
                mQueue.remove();
            }
        }
        return mStack.peek();
    }

    // parse the entry string and produce the number (in between operators, spaces, parens)
    private int getNumber(int pos) {
        int end = pos + 1;
        while (end < mCurrentEntry.length() && isNumber(end)) ++end;
        mQueue.add(mCurrentEntry.substring(pos, end));
        return end - 1; // new position of pos
    }

    private void checkOrder(int pos) {
        while (true) {
            if (mStack.isEmpty()) return;
            if (!checkPemdasOrder(pos) && !checkLeftAssociative(pos)) return;
            mQueue.add(mStack.peek());
            mStack.pop();
        }
    }

    // check if operator on top of stack has greater precedence
    private boolean checkPemdasOrder(int pos) {
        char peek = mStack.peek().charAt(0);
        char token = mCurrentEntry.charAt(pos);

        return ((peek == '%' || peek == '/' || peek == '*')
                && (token == '+' || token == '-'));
    }

    // check if operator on top has equal precedence and is left associative
    private boolean checkLeftAssociative(int pos) {
        char token = mCurrentEntry.charAt(pos);

        boolean modulo = (mStack.peek().equals("%")
                && (token == '%' || token == '/' || token == '*'));

        boolean division = (mStack.peek().equals("/")
                && (token == '%' || token == '/' || token == '*'));

        boolean subtraction = (mStack.peek().equals("-")
                && (token == '+' || token == '-'));

        return subtraction || division || modulo;
    }

    private boolean calculate() {
        if (isValidFormat()) {
            produceReversePolish();
            mTextViewResult.setText(processReversePolish());
            mQueue.clear();
            mStack.clear();
            //mCurrentEntry.setLength(0); // uncomment next 2 to clear the entry text after every calc
            //clearFlags();
            return true;
        }
        return false;
    }

    private void inputNumber(char num) {
        if (mCurrentEntry.length() > 0 && mCurrentEntry.charAt(mCurrentEntry.length()-1) == ')')
            return;
        if (isOperator(mCurrentEntry.length()-1))
            mCurrentEntry.append(" ");
        mCurrentEntry.append(num);
        setEntryText();
        calculate();
    }

    private void inputOperator(String op) {
        if (!isOperator(mCurrentEntry.length()-1)) {
            mCurrentEntry.append(op);
            setEntryText();
            clearFlags();
        }
    }

    private void inputDecimal() {
        if (!mHasDecimal) {
            if (isOperator(mCurrentEntry.length()-1))
                mCurrentEntry.append(" ");
            mCurrentEntry.append(".");
            setEntryText();
            mHasDecimal = true;
        }
    }

    private void inputParenthesis() {
        if (mCurrentEntry.length() == 0 || mCurrentEntry.charAt(mCurrentEntry.length()-1) == '(') {
            mCurrentEntry.append("(");
            ++mHasParens;
        }
        else if (isOperator(mCurrentEntry.length()-1)) {
            mCurrentEntry.append(" (");
            ++mHasParens;
        }
        // close the paren if previous character is a number or another closed paren
        else if (isNumber(mCurrentEntry.length()-1)
                || mCurrentEntry.charAt(mCurrentEntry.length()-1) == ')') {
            mCurrentEntry.append(")");
            --mHasParens;
        }
        setEntryText();
        calculate();
    }

    private void deleteInput() {
        if (mCurrentEntry.length() > 0) {
            char lastChar = mCurrentEntry.charAt(mCurrentEntry.length()-1);
            if (lastChar == '.') // restore flag if deleting a decimal in a number
                mHasDecimal = false;
            else if (lastChar == '(') // open and closed parens
                --mHasParens;
            else if (lastChar == ')')
                ++mHasParens;

            mCurrentEntry.setLength(mCurrentEntry.length()-1);
        }
        // delete ending whitespace in case there is any left after the previous deletion
        if (mCurrentEntry.length() > 0 && mCurrentEntry.charAt(mCurrentEntry.length()-1) == ' ')
            mCurrentEntry.setLength(mCurrentEntry.length()-1);

        if (mCurrentEntry.length() == 0)
            clear();
        else {
            setEntryText();
            calculate();
        }
    }

    private void clear() {
        mCurrentEntry.setLength(0);
        mTextViewEntry.setText(mCurrentEntry.toString());
        mTextViewResult.setText(mCurrentEntry.toString());
        mStack.clear();
        mQueue.clear();
        mHasParens = 0;
        clearFlags();
    }

    // clear any sign or decimal flags for next calculation
    private void clearFlags() {
        mHasDecimal = false;
    }

    private void setEntryText() {
        mTextViewEntry.setText(mCurrentEntry.toString());
    }

    // check if the character at the given pos is an operator
    private boolean isOperator(int pos) {
        if (mCurrentEntry.length() == 0) return false;
        char token = mCurrentEntry.charAt(pos);
        return token == '+' || token == '-' || token == '*' || token == '/' || token == '%';
    }

    // check if the character at the given pos is a number
    private boolean isNumber(int pos) {
        if (mCurrentEntry.length() == 0) return false;
        char token = mCurrentEntry.charAt(pos);
        return (token >= '0' && token <= '9') || token == '.';
    }

    // check if the entry string is a valid formatting
    private boolean isValidFormat () {
        // check for off balance parenthesis, non numeric digits at end of entry string
        char lastChar = mCurrentEntry.charAt(mCurrentEntry.length()-1);
        return mCurrentEntry.length() > 0 && mHasParens == 0
                && !isOperator(mCurrentEntry.length()-1)
                && lastChar != '.' && lastChar != ' ';
    }
}




// TO DO: signs, and exponents
/*

        private boolean checkNegation(int pos) {
        while (mStack.peek().equals("#") && mCurrentEntry.charAt(pos) != '#') {
            mQueue.add(mStack.peek());
            mStack.pop();
        }
        return mStack.isEmpty();
    }


    private void inputSign() {
        // make sure negative signs are only placed in front of numbers
        if (!mHasNegativeSign && !isNumber(mCurrentEntry.length()-1)) {
            if (isOperator(mCurrentEntry.length()-1))
                mCurrentEntry.append(" ");
            mCurrentEntry.append("#"); // pound sign to differentiate between minus sign
            mHasNegativeSign = true;
        }
        else if (mHasNegativeSign && mCurrentEntry.charAt(mCurrentEntry.length()-1) == '#') {
            deleteInput();
            mHasNegativeSign = false;
        }
        setEntryText();
    }


*/