public class Employee extends Staff
{
    private static final double MINIMUM_RATE = 13.5; 

    public Employee( int newID, String newLastName, String newFirstName, String newPassword)
    {
        super(newID, newLastName, newFirstName, newPassword);
        wageRate = MINIMUM_RATE;
    }
    
    public void setWageRate(double newRate)
    {
        wageRate = Math.max(newRate, MINIMUM_RATE);
    }

    public double calculateWages()
    {
        return wageRate * calculateWorkTime();
    }
}
