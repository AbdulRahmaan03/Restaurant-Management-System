import java.util.*;
import java.text.*;

public class Controller_GUI
{
    private final UserInterface_GUI cView;
    private final Database    cDatabase;
    private int         currentUserID;
    private String      currentUserName;
    private final String      todaysDate;
    
    private int         todaysOrderCnt;     //Today's order count
    private double      totalSales;         //Today's total sales
    private int         todaysCancelCnt;    //Today's cancel count
    private double      cancelTotal;        //Total cost of today's canceled orders
    
    
    private String      errorMessage;

    public Controller_GUI()
    {
        this.cDatabase = new Database();
        try
        {
            cDatabase.loadFiles();
        }
        catch(DatabaseException de)
        {
            System.out.println(de.getErrMessage());
            System.exit(0);
        }
        
        cView = new UserInterface_GUI( this);
        
        Date date = new Date();
        SimpleDateFormat stf = new SimpleDateFormat("yyyy/MM/dd");
        todaysDate = stf.format(date);
        cView.setVisible(true);
        cView.setTodaysDate();

        todaysOrderCnt = 0;
        totalSales = 0;
        todaysCancelCnt = 0;
        cancelTotal = 0;
    }
    
    private void  setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
    
    public String  getErrorMessage()
    {
        String result = this.errorMessage;
        this.errorMessage = "";
        return result;
    }
    
    public int getTodaysOrderCnt()
    {
        return this.todaysOrderCnt;
    }
    
    public int getTodaysCancelCnt()
    {
        return this.todaysCancelCnt;
    }
    
    public double getTotalSales()
    {
        return this.totalSales;
    }
    
    public double getCancelTotal()
    {
        return this.cancelTotal;
    }
    
    public double getOrderTotalCharge(int orderID)
    {
        return cDatabase.getOrderTotalCharge(orderID);
    }
    
    public int getOrderState(int orderID)
    {
        return cDatabase.getOrderState(orderID);
    }
    
    public String getCurrentUserName()
    {
        return this.currentUserName;
    }
    
    public boolean checkIfUserClockedOut()
    {
        Staff   rStaff = cDatabase.findStaffByID(currentUserID);
        
        if( rStaff == null) return false;
        return rStaff.getWorkState() == Staff.WORKSTATE_ACTIVE;
    }
    
     // Login

    // Find user
    public boolean loginCheck( int inputID, String inputPassword, boolean isManager)
    {
        String searchClassName;
            
        //---------search user----------
        Staff   rStaff = cDatabase.findStaffByID(inputID);

        if(isManager)   searchClassName = "Manager";
        else            searchClassName = "Employee";
        
        if( rStaff != null)//User data is found
        {
            //Search only particular target(Manager or Employee)
            if( rStaff.getClass().getName().equalsIgnoreCase(searchClassName))
            {
                if(rStaff.getPassword().equals(inputPassword))
                {
                    if(rStaff.getWorkState() == 0)  //Not clocked in yet
                    {
                        rStaff.clockIn();
                    }
                    if(isManager)
                    {
                        cView.changeMode(UserInterface_GUI.MODE_MANAGER);
                    }
                    else 
                    {
                        cView.changeMode(UserInterface_GUI.MODE_EMPLOYEE);
                    }
                    currentUserID = inputID;
                    currentUserName = rStaff.getFullName();
                    cView.setLoginUserName(currentUserName);  //show username on the view
                    
                    return true; //Login success
                }
                else
                {
                    setErrorMessage("Wrong Password!.");
                    return false;
                }
            }
            else
            {
                setErrorMessage("Not found.");
                return false;
            }
        }
        else
        {
            setErrorMessage("Not found.");
            return false;
        }

    }   
    
    // Logout (Set state as Anonymous)
    public void userLogout()
    {
        currentUserID = 0;
        cView.setLoginUserName("");
    }
    
     // Staff management
    public boolean addNewStaff(int newID, String newPassword, String newFirstName, String newLastName, boolean isManager)
    {
        Staff rStaff = cDatabase.findStaffByID(newID);
        if(rStaff != null)
        {
            setErrorMessage("ID:" + newID + " is already used by " + rStaff.getFullName());
            return false;
        }
        
        try
        {
            cDatabase.addStaff(newID, newPassword, newFirstName, newLastName, isManager);
            return true;
        }
        catch(DatabaseException de)
        {
            setErrorMessage(de.getErrMessage());
            return false;
        }
    }
    
    public boolean updateStaff(int id, String newPassword, String newFirstName, String newLastName)
    {
        try
        {
            cDatabase.editStaffData(id, newPassword, newFirstName, newLastName);
            return true;
        }
        catch(DatabaseException de)
        {
            setErrorMessage(de.getErrMessage());
            return false;
        }
    }
    
    public boolean deleteStaff(int id)
    {
        Staff rStaff = cDatabase.findStaffByID(id);
        if(rStaff == null)
        {
            setErrorMessage("StaffID:" + id + " is not found.");
            return false;
        }
        
        try
        {
            cDatabase.deleteStaff(rStaff);
        }
        catch(DatabaseException de)
        {
            setErrorMessage(de.getErrMessage());
            return false;
        }
        return true;
    }
    
    public Staff    getStaffData(int staffID)
    {
        return cDatabase.findStaffByID(staffID);
    }
    
    public void clockOut()
    {
        clockOut(currentUserID);
    }
    
    public boolean clockOut(int staffID)
    {
        Staff rStaff = cDatabase.findStaffByID(staffID);
        
        byte state = rStaff.getWorkState();
        boolean result = false;
        switch (state) {
            case Staff.WORKSTATE_ACTIVE -> {
                rStaff.clockOut();
                result = true;
            }
            case Staff.WORKSTATE_FINISH -> setErrorMessage("Staff:" + rStaff.getFullName() + " already clocked out.");
            default -> setErrorMessage("Staff:" + rStaff.getFullName() + "has not been on work today.");
        }
        
        return result;
    }
    
    public void clockOutAll()
    {
        cDatabase.forthClockOutAllStaff();
    }

     // Menu management
    public boolean addNewMenuItem(int newID, String newName, double newPrice, byte menuType)
    {
        MenuItem rMenuItem = cDatabase.findMenuItemByID(newID);
        if(rMenuItem != null)
        {
            setErrorMessage("ID:" + newID + " is already used by " + rMenuItem.getName());
            return false;
        }
        
        try
        {
            cDatabase.addMenuItem(newID, newName, newPrice, menuType);
            return true;
        }
        catch(DatabaseException de)
        {
            setErrorMessage(de.getErrMessage());
            return false;
        }
    }
    
    public boolean updateMenuItem(int id, String newName, double newPrice, byte menuType)
    {
        try
        {
            cDatabase.editMenuItemData(id, newName, newPrice, menuType);
            return true;
        }
        catch(DatabaseException de)
        {
            setErrorMessage(de.getErrMessage());
            return false;
        }
    }
    
    public boolean deleteMenuItem(int id)
    {
        MenuItem rMenuItem= cDatabase.findMenuItemByID(id);
        if(rMenuItem == null)
        {
            setErrorMessage("Menu item ID:" + id + " is not found.");
            return false;
        }
        
        try
        {
            cDatabase.deleteMenuItem(rMenuItem);
        }
        catch(DatabaseException de)
        {
            setErrorMessage(de.getErrMessage());
            return false;
        }
        return true;
    }
     
    public MenuItem    getMenuItemData(int menuItemID)
    {
        return cDatabase.findMenuItemByID(menuItemID);
    }
     // Order management
    public int createOrder()
    {
        return cDatabase.addOrder(currentUserID, currentUserName);
    }
    
    public boolean addNewOrderItem(int orderID, int addItemID, byte addItemQuantity)
    {
        Order rOrder = cDatabase.findOrderByID(orderID);
        if( currentUserID != rOrder.getStaffID())
        {
            setErrorMessage("You are not eligible to edit the order.\nThe order belongs to " + rOrder.getStaffName() + ")");
            return false;    
        }
        
        MenuItem    rNewItem;
        
        rNewItem = cDatabase.findMenuItemByID(addItemID);
        if(rNewItem == null)
        {
            setErrorMessage("MenuID[" + addItemID + "]is not found.");
            return false;
        }
         cDatabase.addOrderItem(orderID, rNewItem, addItemQuantity);
         return true;
    }
    
    public boolean deleteOrderItem(int orderID, int deleteNo)
    {
        Order rOrder = cDatabase.findOrderByID(orderID);
        if( currentUserID != rOrder.getStaffID())
        {
            setErrorMessage("You are not eligible to delete the order.\nThe order belongs to " + rOrder.getStaffName() + ")");
            return false;    
        }
        
        deleteNo -=1;  //index actually starts from zero
        if(!cDatabase.deleteOrderItem(orderID, deleteNo))
        {
            setErrorMessage("Not found.");
            return false;
        }
        return true;
    }
    
    public boolean closeOrder(int closeOrderID)
    {
        Order rOrder = cDatabase.findOrderByID(closeOrderID);
        if( currentUserID != rOrder.getStaffID())
        {
            setErrorMessage("You are not eligible to delete the order.\n(The order belongs to " + rOrder.getStaffName() + ")");
            return false;    
        }
        
        if(rOrder.getState() != 0)
        {
            setErrorMessage("The order is already closed or canceled.");
            return false;
        }
        cDatabase.closeOrder(closeOrderID);
        todaysOrderCnt++;
        totalSales += rOrder.getTotal();
        return true;
    }
    
    public boolean cancelOrder(int cancelOrderID)
    {
        Order rOrder = cDatabase.findOrderByID(cancelOrderID);
        if( currentUserID != rOrder.getStaffID())
        {
            setErrorMessage("You are not eligible to delete the order.\n(The order belongs to " + rOrder.getStaffName() + ")");
            return false;    
        }
        
        if( rOrder.getState() != 0)
        {
            setErrorMessage("The order is already closed or canceled.");
            return false;
        }

        cDatabase.cancelOrder(cancelOrderID);
        todaysCancelCnt++;
        cancelTotal += rOrder.getTotal();
        return true;
    }
    
    public void closeAllOrder()
    {
        cDatabase.closeAllOrder();
    }

     public String generateSalesReport()
     {
        if(!cDatabase.checkIfAllOrderClosed())
        {
             setErrorMessage("All orders must be closed or canceled before generate reports.");
             return null;
        }
            
         try
        {
            return cDatabase.generateOrderReport(todaysDate);
        }
        catch(DatabaseException de)
        {
            setErrorMessage(de.getErrMessage());
            return null;
        }
    }
    
    public String generatePaymentReport()
    {
        if(!cDatabase.checkIfAllStaffCheckout())
        {
            setErrorMessage("All staff must be checked out before generate a payment report.");
            return null;
        }

        try
        {
            return cDatabase.generatePaymentReport(todaysDate);
        }
        catch(DatabaseException de)
        {
            setErrorMessage(de.getErrMessage());
            return null;
        }
    }
    
    // Create string lists
    public  ArrayList<String>  createStaffList()
    {
        Iterator<Staff> it = cDatabase.getStaffList().iterator();
        ArrayList<String> initData = new ArrayList<>();
        
        while (it.hasNext()) {
            Staff re = it.next();
            String fullName = re.getFullName();
            String output = String.format("Staff ID:%4d  Name:%-25s",
                                            re.getID(), fullName);
            switch (re.getWorkState()) {
                case Staff.WORKSTATE_ACTIVE -> output += "[From:" + re.getStartTime() + "]";
                case Staff.WORKSTATE_FINISH -> output += "[From:" + re.getStartTime() + " to " + re.getFinishTime() + "]";
                default -> output += "[Not on work]";
            }

             if(re instanceof Manager)
            {
                output += " * Manager *";
            }
            initData.add(output);
        }
        
        return initData;
    }
    
    public ArrayList<String>  createOrderList()
    {
        Iterator<Order> it = cDatabase.getOrderList().iterator();
        String          state;
        ArrayList<String> initData = new ArrayList<>();
        String          output;
        
        while (it.hasNext()) {
            Order re = it.next();
            state = switch (re.getState()) {
                case Order.ORDER_CLOSED -> "Closed";
                case Order.ORDER_CANCELED -> "Canceled";
                default -> "-";
            };
            
            output = String.format("Order ID:%4d  StaffName:%-20s  Total:$%5.2f State:%-8s\n",
                                            re.getOrderID(),re.getStaffName(),re.getTotal(),state);
            initData.add(output);
        }
        if(initData.isEmpty())
            initData.add("No order.");
        return initData;
    }
    
    public ArrayList<String> createOrderItemlList(int orderID)
    {
        Order rOrder = cDatabase.findOrderByID(orderID);
        ArrayList<String> initData = new ArrayList<>();

        if(rOrder == null)
        {
            initData.add("No order information");
            return initData;
        }
        
        String output;

        Iterator<OrderDetail> it = rOrder.getOrderDetail().iterator();
        OrderDetail    re;
        
        int count = 0;
        
        while (it.hasNext()) {
            re = it.next();
            output = String.format("%-4d|%-24s|%5d|%5.2f",
                                    ++count, re.getItemName(), re.getQuantity(), re.getTotalPrice());
           initData.add(output);
        }
        if(initData.isEmpty())
            initData.add("No item");
        return initData;
    }   
    
    public ArrayList<String> createMenuList(int displayMenuType)
    {
        Iterator<MenuItem> it = cDatabase.getMenuList().iterator();
        ArrayList<String> initData = new ArrayList<>();
        
        while (it.hasNext()) {
            MenuItem re = it.next();
             byte menuType = re.getType();
             if(displayMenuType!= 0 && displayMenuType != menuType)
                continue;
             String strMenuType = switch (menuType) {
                 case MenuItem.BREAKFAST -> "Breakfast";
                 case MenuItem.LUNCH -> "Lunch";
                 case MenuItem.DINNER -> "Dinner";
                 case MenuItem.DESSERT -> "Dessert";
                 default -> "Undefined";
             };
            String output = String.format("Menu ID:%4d  Name:%-20s  Price:%5.2f Type:%s",
                                            re.getID(),re.getName(),re.getPrice(),strMenuType);
           if(re.getState() == MenuItem.PROMOTION_ITEM)
           {
               output += " ** Today's Special!! **";
            }
            
            initData.add(output);
        }
        if(initData.isEmpty())
            initData.add("No order.");
        return initData;
    }
    
    public String createPaymentList()
    {
        double          totalPayment = 0;
        int             staffNum = 0;
        StringBuilder output = new StringBuilder();

        for (Staff re : cDatabase.getStaffList()) {
            if (re.getWorkState() == Staff.WORKSTATE_FINISH) {
                double pay = re.calculateWages();
                output.append(String.format("Staff ID:%4d  StaffName:%-20s  Work time:%5.2f Pay:%5.2f\n",
                        re.getID(), re.getFullName(), re.calculateWorkTime(), pay));
                staffNum++;
                totalPayment += pay;
            } else if (re.getWorkState() == Staff.WORKSTATE_ACTIVE) {
                output.append(String.format("Staff ID:%4d  StaffName:%-20s  * On work *\n",
                        re.getID(), re.getFullName()));
                staffNum++;
            }
        }
        output.append("-------------------------------------------------------\n");
        output.append(String.format("Total payment:$%.2f (%d)", totalPayment, staffNum));
        return output.toString();
    }
}
