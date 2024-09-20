package nl.fontys.s3.codons;

public class Calculator {

    // Method to add two numbers
    public int add(int a, int b) {
        return a + b;
    }

    // Method to subtract two numbers
    public int subtract(int a, int b) {
        return a - b;
    }

    // Method to multiply two numbers
    public int multiply(int a, int b) {
        return a * b;
    }

    // Method to divide two numbers
    public int divide(int a, int b) throws IllegalArgumentException {
        if (b == 0) {
            throw new IllegalArgumentException("Cannot divide by zero.");
        }
        return a / b;
    }
}

