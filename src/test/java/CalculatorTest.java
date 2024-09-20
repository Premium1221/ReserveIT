import nl.fontys.s3.codons.Calculator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTest {

    Calculator calculator = new Calculator();

    @Test
    public void testAddition() {
        assertEquals(5, calculator.add(2, 3), "2 + 3 should equal 5");
    }

    @Test
    public void testSubtraction() {
        assertEquals(1, calculator.subtract(3, 2), "3 - 2 should equal 1");
    }

    @Test
    public void testMultiplication() {
        assertEquals(6, calculator.multiply(2, 3), "2 * 3 should equal 6");
    }

    @Test
    public void testDivision() {
        assertEquals(2, calculator.divide(6, 3), "6 / 3 should equal 2");
    }

    @Test
    public void testDivisionByZero() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.divide(5, 0);
        });

        String expectedMessage = "Cannot divide by zero.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
