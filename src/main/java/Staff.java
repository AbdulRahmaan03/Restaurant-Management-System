import java.util.*;
import java.text.*;

public abstract class Staff {
    private int ID;
    private String lastName;
    private String firstName;
    private String password;
    private byte state;


    //protected byte  workState;  //0:not active  1:active (on work)  2:finish work
    protected Date startWorkTime;
    protected Date finishWorkTime;
    protected double wageRate;


    public Staff(int newID, String newFirstName, String newLastName, String newPassword) {
        setID(newID);
        setFirstName(newFirstName);
        setLastName(newLastName);
        setPassword(newPassword);
        startWorkTime = null;
        finishWorkTime = null;
        state = 0;
        //workState = 0;
    }

    // setter
    protected void setID(int newID) {
        this.ID = newID;
    }

    protected void setLastName(String newLastName) {
        this.lastName = newLastName;
    }

    protected void setFirstName(String newFirstName) {
        this.firstName = newFirstName;
    }

    protected void setPassword(String newPassword) {
        this.password = newPassword;
    }

    // getter
    public int getID() {
        return this.ID;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public String getPassword() {
        return this.password;
    }

    public double getWageRate() {
        return this.wageRate;
    }

    public static final byte WORKSTATE_ACTIVE = 1;
    public static final byte WORKSTATE_FINISH = 2;

    public byte getWorkState() {
        return this.state;
    }

    public String getStartTime() {
        if (startWorkTime == null)
            return "getStartTime Error";
        DateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(startWorkTime);
    }

    public String getFinishTime() {
        if (finishWorkTime == null)
            return "getFinishTime Error";
        DateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(finishWorkTime);
    }

    // other methods
    public void clockIn() {
        startWorkTime = new Date(System.currentTimeMillis());
        state = WORKSTATE_ACTIVE;
    }

    public void clockOut() {
        if (state != WORKSTATE_ACTIVE)
            return;
        finishWorkTime = new Date(System.currentTimeMillis());
        state = WORKSTATE_FINISH;
    }

    public double calculateWorkTime() {
        if (getWorkState() != WORKSTATE_FINISH)
            return 0;

        long diffTimeMin = (finishWorkTime.getTime() - startWorkTime.getTime()) / 60000;//convert Milli sec to Minutes
        long baseTime = diffTimeMin / 60;
        long fraction = diffTimeMin % 60;
        double addTime;

        if (fraction < 15)
            addTime = 0;
        else if (fraction < 30)
            addTime = 0.25;
        else if (fraction < 45)
            addTime = 0.5;
        else
            addTime = 0.75;

        return (double) baseTime + addTime;
    }

    protected abstract void setWageRate(double newRate);

    protected abstract double calculateWages();
}
