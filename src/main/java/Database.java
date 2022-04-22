import java.util.*;
import java.io.*;
import java.lang.*;
import java.util.Comparator;

public class Database {
    private final static String STAFF_FILE = "dataFiles/staff.txt";
    private final static String MANAGER_FILE = "dataFiles/manager.txt";
    private final static String MENU_FILE = "dataFiles/menu_item.txt";
    private final static String REPORT_FILE = "dataFiles/reports/report_";
    private final static String PAYMENT_FILE = "dataFiles/reports/payment_";
    private final static String WAGE_INFO_FILE = "dataFiles/wage_info.txt";

    private final ArrayList<Staff> staffList = new ArrayList<>();
    private final ArrayList<MenuItem> menuList = new ArrayList<>();
    private final ArrayList<Order> orderList = new ArrayList<>();

    int todaysOrderCounts;

    // Constructor
    public Database() {
        todaysOrderCounts = 0;  //Load order file??
    }

    // Getter
    public ArrayList<Staff> getStaffList() {
        return staffList;
    }

    public ArrayList<MenuItem> getMenuList() {
        return menuList;
    }

    public ArrayList<Order> getOrderList() {
        return orderList;
    }

    // Find staff from ID
    public Staff findStaffByID(int id) {
        Iterator<Staff> it = staffList.iterator();
        Staff re = null;
        boolean found = false;

        if (id < 0) {
            return null;
        }

        while (it.hasNext() && !found) {
            re = it.next();
            if (re.getID() == id) {
                found = true;
            }
        }

        if (found)
            return re;
        else
            return null;
    }

    // Find menu item from ID
    public MenuItem findMenuItemByID(int id) {
        Iterator<MenuItem> it = menuList.iterator();
        MenuItem re = null;
        boolean found = false;

        if (id < 0) {
            return null;
        }

        while (it.hasNext() && !found) {
            re = it.next();
            if (re.getID() == id) {
                found = true;
            }
        }

        if (found)
            return re;
        else
            return null;
    }

    // Find order from ID
    public Order findOrderByID(int id) {
        Iterator<Order> it = orderList.iterator();
        Order re = null;
        boolean found = false;

        if (id < 0) {
            return null;
        }

        while (it.hasNext() && !found) {
            re = it.next();
            if (re.getOrderID() == id) {
                found = true;
            }
        }

        if (found)
            return re;
        else
            return null;
    }
    // Manipulate data's

    public void editStaffData(int staffID, String newPassword, String newFirstName, String newLastName) throws DatabaseException {
        Staff rStaff = findStaffByID(staffID);
        rStaff.setPassword(newPassword);
        rStaff.setLastName(newLastName);
        rStaff.setFirstName(newFirstName);

        //update employee file
        updateStaffFile(rStaff instanceof Manager);//update manager file
    }

    public void deleteStaff(Staff rStaff) throws DatabaseException {
        staffList.remove(rStaff);
        if (rStaff instanceof Manager)
            updateStaffFile(true);
    }


    public void addStaff(int newID, String newPassword, String newFirstName, String newLastName, boolean isManager) throws DatabaseException {
        Staff newStaff;
        if (isManager)
            newStaff = new Manager(newID, newLastName, newFirstName, newPassword);
        else
            newStaff = new Employee(newID, newLastName, newFirstName, newPassword);
        staffList.add(newStaff);
        updateStaffFile(isManager);
    }

    public void editMenuItemData(int id, String newName, double newPrice, byte menuType) throws DatabaseException {
        MenuItem rMenuItem = findMenuItemByID(id);
        rMenuItem.setName(newName);
        rMenuItem.setPrice(newPrice);
        rMenuItem.setType(menuType);
    }

    public void deleteMenuItem(MenuItem rMenuItem) throws DatabaseException {
        menuList.remove(rMenuItem);
    }

    public void addMenuItem(int newID, String newName, double newPrice, byte newType) throws DatabaseException {
        MenuItem newMenuItem = new MenuItem(newID, newName, newPrice, newType);
        menuList.add(newMenuItem);
        menuList.sort(new MenuItemComparator());
    }

    // Order
    public int addOrder(int staffID, String staffName) {
        int newOrderID = ++todaysOrderCounts;
        Order newOrder = new Order(staffID, staffName);
        newOrder.setOrderID(newOrderID);
        orderList.add(newOrder);
        return newOrderID;
    }

    public void addOrderItem(int orderID, MenuItem rItem, byte quantity) {
        Order rOrder = findOrderByID(orderID);
        rOrder.addItem(rItem, quantity);
    }

    public boolean deleteOrderItem(int orderID, int index) {
        Order rOrder = findOrderByID(orderID);
        if (rOrder == null)
            return false;
        return rOrder.deleteItem(index);
    }


    //Cancel order: order data is not deleted from the database(Just put cancel flag on)
    public void cancelOrder(int orderID) {
        Order rOrder = findOrderByID(orderID);
        if (rOrder == null)
            return;
        rOrder.setState(Order.ORDER_CANCELED);
    }

    public void closeOrder(int orderID) {
        Order rOrder = findOrderByID(orderID);
        if (rOrder == null)
            return;
        rOrder.setState(Order.ORDER_CLOSED);
    }

    public void closeAllOrder() {
        Iterator<Order> it = orderList.iterator();
        Order re;

        while (it.hasNext()) {
            re = it.next();
            if (re.getState() == 0)//neither closed and canceled
            {
                re.setState(Order.ORDER_CLOSED);
            }
        }
    }

    public int getOrderState(int orderID) {
        Order re = findOrderByID(orderID);
        if (re == null)
            return -1;
        return re.getState();
    }

    public double getOrderTotalCharge(int orderID) {
        Order re = findOrderByID(orderID);
        if (re == null)
            return -1;
        return re.getTotal();
    }

    public boolean checkIfAllOrderClosed() {
        Iterator<Order> it = orderList.iterator();
        Order re;

        while (it.hasNext()) {
            re = it.next();
            if (re.getState() == 0)//neither closed and canceled
            {
                return false;
            }
        }
        return true;
    }

    public boolean checkIfAllStaffCheckout() {
        Iterator<Staff> it = staffList.iterator();
        Staff re;

        while (it.hasNext()) {
            re = it.next();
            if (re.getWorkState() == Staff.WORKSTATE_ACTIVE) {
                return false;
            }
        }
        return true;
    }

    public void forthClockOutAllStaff() {
        Iterator<Staff> it = staffList.iterator();
        Staff re;

        while (it.hasNext()) {
            re = it.next();
            if (re.getWorkState() == Staff.WORKSTATE_ACTIVE) {
                re.clockOut();
            }
        }
    }

    // File load
    public void loadFiles() throws DatabaseException {
        loadStaffFile();
        loadManagerFile();
        staffList.sort(new StaffComparator());
        loadMenuFile();
        loadWageInfoFile();
    }

    private void loadStaffFile() throws DatabaseException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(STAFF_FILE));
            String line = reader.readLine();

            while (line != null) {
                String[] record = line.split(",");

                String id = record[0].trim();
                String password = record[1].trim();
                String firstName = record[2].trim();
                String lastName = record[3].trim();

                // Add the data from file to the registerCourses array list
                Employee rEmployee = new Employee(Integer.parseInt(id), lastName, firstName, password);
                staffList.add(rEmployee);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException ioe) {
            String message = ioe.getMessage() + Arrays.toString(ioe.getStackTrace());
            throw new DatabaseException(message);
        }
    }

    private void loadManagerFile() throws DatabaseException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(MANAGER_FILE));
            String line = reader.readLine();

            while (line != null) {
                String[] record = line.split(",");

                String id = record[0].trim();
                String password = record[1].trim();
                String firstName = record[2].trim();
                String lastName = record[3].trim();

                // Add the data from file to the registerCourses array list
                Manager rManager = new Manager(Integer.parseInt(id), lastName, firstName, password);
                staffList.add(rManager);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException ioe) {
            String message = ioe.getMessage() + Arrays.toString(ioe.getStackTrace());
            throw new DatabaseException(message);
        }
    }

    private void loadMenuFile() throws DatabaseException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(MENU_FILE));
            String line = reader.readLine();

            while (line != null) {
                String[] record = line.split(",");

                String id = record[0].trim();
                String name = record[1].trim();
                String price = record[2].trim();
                String type = record[3].trim();

                // Add the data from file to the registerCourses array list
                MenuItem rMenuItem = new MenuItem(Integer.parseInt(id), name, Double.parseDouble(price), Byte.parseByte(type));
                menuList.add(rMenuItem);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException ioe) {
            String message = ioe.getMessage() + Arrays.toString(ioe.getStackTrace());
            throw new DatabaseException(message);
        }
    }

    private void loadWageInfoFile() throws DatabaseException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(WAGE_INFO_FILE));
            String line = reader.readLine();

            while (line != null) {
                String[] record = line.split(",");

                String id = record[0].trim();
                String rate = record[1].trim();

                double dRate = Double.parseDouble(rate);
                int iId = Integer.parseInt(id);

                Staff rStaff = findStaffByID(iId);
                if (rStaff == null) {
                    throw new DatabaseException("Load wage file error\n Staff ID:" + iId + " is not found.");
                }
                rStaff.setWageRate(dRate);

                line = reader.readLine();
            }
            reader.close();
        } catch (Exception ioe) {
            String message = ioe.getMessage() + Arrays.toString(ioe.getStackTrace());
            throw new DatabaseException(message);
        }
    }

    // File Edit
    public void updateStaffFile(boolean isManager) throws DatabaseException {
        Writer writer;
        String fileName;
        String tempFileName = "dataFiles/temp.txt";

        if (isManager)
            fileName = MANAGER_FILE;
        else
            fileName = STAFF_FILE;

        staffList.sort(new StaffComparator());
        File tempFile = new File(tempFileName);

        try {
            writer = new BufferedWriter(new FileWriter(tempFile));

            for (Staff re : staffList) {
                //skip writing data
                if (isManager) {
                    //skip employee data
                    if (re instanceof Employee)
                        continue;
                } else {
                    //skip manager data
                    if (re instanceof Manager)
                        continue;
                }

                writer.write(re.getID() + "," + re.getPassword() + "," + re.getFirstName() + "," + re.getLastName() + "\r\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            String message = e.getMessage() + Arrays.toString(e.getStackTrace());
            throw new DatabaseException(message);
        }

        //delete current file
        File deleteFile = new File(fileName);
        deleteFile.delete();

        // renames temporally file to new file
        File newFile = new File(fileName);
        tempFile.renameTo(newFile);

        updateWageFile();
    }

    public void updateWageFile() throws DatabaseException {
        Writer writer;
        String tempFileName = "dataFiles/temp.txt";

        File tempFile = new File(tempFileName);

        try {
            writer = new BufferedWriter(new FileWriter(tempFile));

            for (Staff re : staffList) {
                writer.write(re.getID() + "," + re.getWageRate() + "\r\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            String message = e.getMessage() + Arrays.toString(e.getStackTrace());
            throw new DatabaseException(message);
        }

        //delete current file
        File deleteFile = new File(WAGE_INFO_FILE);
        deleteFile.delete();

        // renames temporally file to new file
        File newFile = new File(WAGE_INFO_FILE);
        tempFile.renameTo(newFile);
    }

    public String generateOrderReport(String todaysDate) throws DatabaseException {
        Writer writer;
        String line;
        int state;
        double totalAllOrder = 0;
        String generateFileName;
        File newFile;
        int orderCnt = 0;
        int cancelCnt = 0;
        double cancelTotal = 0;

        String[] record = todaysDate.split("/");
        String today = record[0].trim() + "_" + record[1].trim() + "_" + record[2].trim();
        generateFileName = REPORT_FILE + today + ".txt";
        newFile = new File(generateFileName);

        try {
            writer = new BufferedWriter(new FileWriter(newFile));

            line = "*********** Order List (" + today + ") ***********\r\n";
            writer.write(line);

            for (Order re : orderList) {
                state = re.getState();
                String stateString;
                double totalOfEachOrder = re.getTotal();
                if (state == Order.ORDER_CANCELED) {
                    stateString = "Canceled";
                    cancelTotal += totalOfEachOrder;
                    cancelCnt++;
                } else {
                    stateString = "";
                    totalAllOrder += totalOfEachOrder;
                    orderCnt++;
                }
                String output = String.format("Order ID:%4d  StaffName:%-30s  Total:$%-5.2f %s\r\n",
                        re.getOrderID(), re.getStaffName(), totalOfEachOrder, stateString);
                writer.write(output);


            }
            writer.write("-------------------------------------------------------\r\n");

            writer.write("Total sales:$" + totalAllOrder + "(" + orderCnt + ")" +
                    "  Canceled:$" + cancelTotal + "(" + cancelCnt + ")\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            String message = e.getMessage() + Arrays.toString(e.getStackTrace());
            newFile.delete();
            throw new DatabaseException(message);
        }
        return generateFileName;
    }

    public String generatePaymentReport(String todaysDate) throws DatabaseException {
        Writer writer;
        String line;
        double totalPayment = 0;
        String generateFileName;
        File newFile;
        int staffNum = 0;

        String[] record = todaysDate.split("/");
        String today = record[0].trim() + "_" + record[1].trim() + "_" + record[2].trim();
        generateFileName = PAYMENT_FILE + today + ".txt";
        newFile = new File(generateFileName);

        try {
            writer = new BufferedWriter(new FileWriter(newFile));

            line = "*********** Payment List (" + today + ") ***********\r\n";
            writer.write(line);

            for (Staff re : staffList) {
                if (re.getWorkState() == Staff.WORKSTATE_FINISH) {
                    double pay = re.calculateWages();
                    String output = String.format("Order ID:%4d  StaffName:%-30s  Work time:%-5.2f Pay:%-5.2f\r\n",
                            re.getID(), re.getFullName(), re.calculateWorkTime(), pay);
                    writer.write(output);
                    staffNum++;
                    totalPayment += pay;
                }
            }
            writer.write("-------------------------------------------------------\r\n");

            writer.write("Total payment:$" + totalPayment + "(" + staffNum + ")\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            String message = e.getMessage() + Arrays.toString(e.getStackTrace());
            newFile.delete();
            throw new DatabaseException(message);
        }
        return generateFileName;
    }

    // Comparator
    private static class StaffComparator implements Comparator<Staff> {

        @Override
        public int compare(Staff s1, Staff s2) {
            return s1.getID() < s2.getID() ? -1 : 1;
        }
    }

    private static class MenuItemComparator implements Comparator<MenuItem> {

        @Override
        public int compare(MenuItem m1, MenuItem m2) {
            return m1.getID() < m2.getID() ? -1 : 1;
        }
    }
}
