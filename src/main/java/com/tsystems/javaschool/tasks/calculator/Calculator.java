package com.tsystems.javaschool.tasks.calculator;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Calculator {

    /**
     * Evaluate statement represented as string.
     *
     * @param statement mathematical statement containing digits, '.' (dot) as decimal mark,
     *                  parentheses, operations signs '+', '-', '*', '/'<br>
     *                  Example: <code>(1 + 38) * 4.5 - 1 / 2.</code>
     * @return string value containing result of evaluation or null if statement is invalid
     */
    public String evaluate(String statement) {
        // TODO: Implement the logic here
        class OperatorsStack {
            private ArrayList<Character> stackArray;
            private int top;

            public OperatorsStack() {
                int overSize = 100;
                stackArray = new ArrayList<>();
                for (int i = 0; i < overSize; i++) {
                    stackArray.add('@');
                }
                top = -1;
            }

            public void push(char ch) {
                stackArray.set(++top, ch);
            }

            public char pop() {
                return stackArray.get(top--);
            }

            public boolean isEmpty() {
                return (top == -1);
            }

            public int size() {
                return top + 1;
            }
        }

        class ToPostfix {
            private OperatorsStack opStack;
            private List<String> tokBuf;
            private List<String> output;

            public ToPostfix(List<String> tokensBuf) {
                opStack = new OperatorsStack();
                tokBuf = tokensBuf;
                output = new ArrayList<>(tokBuf.size());
            }

            public void gotOper(char opThis, int priorFirst) {
                while (!opStack.isEmpty()) {
                    char opTop = opStack.pop();
                    if (opTop == '(') {
                        opStack.push(opTop);
                        break;
                    } else {
                        int priorSecond;
                        if (opTop == '+' || opTop == '-')
                            priorSecond = 1;
                        else
                            priorSecond = 2;
                        if (priorSecond < priorFirst) {
                            opStack.push(opTop);
                            break;
                        } else
                            output.add(String.valueOf(opTop));
                    }
                }
                opStack.push(opThis);
            }

            public void gotParen(char ch) {
                while (!opStack.isEmpty()) {
                    char chx = opStack.pop();
                    if (chx == '(')
                        break;
                    else
                        output.add(String.valueOf(chx));
                }
            }

            public List<String> doPostfix() {
                int i = 0;
                while (i < tokBuf.size()) {
                    String str = tokBuf.get(i++);
                    switch (str) {
                        case "+":
                        case "-":
                            gotOper(str.charAt(0), 1);
                            break;
                        case "*":
                        case "/":
                            gotOper(str.charAt(0), 2);
                            break;
                        case "(":
                            opStack.push(str.charAt(0));
                            break;
                        case ")":
                            gotParen(str.charAt(0));
                            break;
                        default:
                            try {
                                output.add(String.valueOf(Double.parseDouble(str)));
                            } catch (NumberFormatException e) {
                                return null;
                            }

                    }
                }
                while (!opStack.isEmpty()) {
                    output.add(String.valueOf(opStack.pop()));
                }
                return output;
            }
        }
        class PareserPostix {
            private List<String> valueStack;
            private List<String> input;

            public PareserPostix(List<String> postfixStatement) {
                valueStack = new ArrayList<>(postfixStatement);
                for (int i = 0; i < postfixStatement.size(); i++) {
                    valueStack.add("");
                }
                input = postfixStatement;
            }

            public double doParse() throws IllegalArgumentException {
                double num1 = 0, num2 = 0, res = 0;
                int i = 0;
                int top = -1;
                while (i < input.size()) {
                    String str = input.get(i);
                    if (!str.equals("+") && !str.equals("-") && !str.equals("*") && !str.equals("/")) {
                        valueStack.set(++top, str);
                    } else {
                        if (top < 1) throw new IllegalArgumentException();
                        num2 = Double.parseDouble(valueStack.get(top--));
                        num1 = Double.parseDouble(valueStack.get(top--));
                        switch (str) {
                            case "+":
                                res = num1 + num2;
                                break;
                            case "-":
                                res = num1 - num2;
                                break;
                            case "*":
                                res = num1 * num2;
                                break;
                            case "/":
                                if (num2 == 0) throw new IllegalArgumentException();
                                res = num1 / num2;
                                break;
                        }
                        valueStack.set(++top, String.valueOf(res));
                        res = 0;
                    }
                    i++;
                }
                res = Double.parseDouble(valueStack.get(top));
                return Math.round(res * 10000.0) / 10000.0;
            }
        }
        if (statement == null) return null;
        if (statement.length() < 3) return null;
        for (int i = 0; i < statement.length(); i++) {
            char ch = statement.charAt(i);
            if (Character.isDigit(ch)) {
                boolean correctDecimal = false;
                for (int j = i + 1; j < statement.length(); j++) {
                    if (statement.charAt(j) == '.') {
                        if (!correctDecimal) correctDecimal = true;
                        else return null;
                    } else if (!Character.isDigit(statement.charAt(j)))
                        break;
                }
            }
        }
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(statement));
        tokenizer.ordinaryChar('-'); // Don't parse minus as part of numbers.
        tokenizer.ordinaryChar('/');  // Don't treat slash as a comment start.
        List<String> tokBuf = new ArrayList<>();
        try {
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                switch (tokenizer.ttype) {
                    case StreamTokenizer.TT_NUMBER:
                        tokBuf.add(String.valueOf(tokenizer.nval));
                        break;
                    default:  // operator
                        tokBuf.add(String.valueOf((char) tokenizer.ttype));
                }
            }
        } catch (IOException e) {
            // add to logger
            throw new RuntimeException();
        }
        if (tokBuf.size() <= 2)
            return null;
        ToPostfix postfix = new ToPostfix(tokBuf);
        List<String> postfixStatement = postfix.doPostfix();
        if (postfixStatement == null) return null;
        PareserPostix parser = new PareserPostix(postfix.doPostfix());
        double res = 0;
        try {
            res = parser.doParse();
        } catch (IllegalArgumentException e) {
            return null;
        }
        int check = (int) res;
        if (res / check == 1) return "" + check;
        return String.valueOf(parser.doParse());
    }

    public static void main(String[] args) {
        Calculator calc = new Calculator();
        System.out.println(calc.evaluate(args.toString()));
    }
}
