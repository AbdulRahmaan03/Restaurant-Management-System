public class Employee extends Staff {
    private static final double MINIMUM_RATE = 13.5;

    public Employee(int newID, String newFirstName, String newLastName, String newPassword) {
        super(newID, newFirstName, newLastName, newPassword);
        wageRate1 = MINIMUM_RATE;
    }

    public void setWageRate(double newRate) {
        wageRate1 = Math.max(newRate, MINIMUM_RATE);
    }

    public double calculateWages() {
        return wageRate1 * calculateWorkTime();
    }
}
