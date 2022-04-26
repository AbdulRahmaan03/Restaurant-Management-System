public class Manager extends Staff
{
    private static final double MINIMUM_RATE = 100.0;

    public Manager( int newID, String newFirstName, String newLastName, String newPassword)
    {
        super(newID, newFirstName, newLastName, newPassword);
        wageRate = MINIMUM_RATE;
    }
    
    public void setWageRate(double newRate)
    {
        if(wageRate < MINIMUM_RATE)
            newRate = MINIMUM_RATE;
        wageRate = newRate;
    }
    

    public double calculateWages()
    {
        if(getWorkState() != WORKSTATE_FINISH)
            return 0;
        
        return this.wageRate;
    }
}
