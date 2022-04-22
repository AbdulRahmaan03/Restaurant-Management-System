import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.util.Comparator;

public class Database {
    private final static String REPORT_FILE = "dataFiles/reports/report_";
    private final static String PAYMENT_FILE = "dataFiles/reports/payment_";

    private final static String host = "localhost";
    private final static int port = 3306;
    private final static String database = "restaurant";
    private final static String user = "root";
    private final static String password = "1234";

    private final ArrayList<Staff> staffList = new ArrayList<>();
    private final ArrayList<MenuItem> menuList = new ArrayList<>();
    private final ArrayList<Order> orderList = new ArrayList<>();

    private int todaysOrderCounts;
    private final MysqlDataSource ds;

    // Constructor
    public Database() throws DatabaseException {
        todaysOrderCounts = 0;  //Load order file?? idk

        try {
            Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ds = new MysqlDataSource();
        ds.setServerName(host);
        ds.setPort(port);
        ds.setDatabaseName(database);
        ds.setUser(user);
        ds.setPassword(password);


/**
 * Staff - id, first name, last name, password, wage, isManager
 * Menu - id, name, price, type
 * Order ? Dont think we need that table
 */
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS staff (" +
                    "id INTEGER PRIMARY KEY NOT NULL," +
                    "first_name VARCHAR(20) NOT NULL," +
                    "last_name VARCHAR(20) NOT NULL," +
                    "password VARCHAR(255) NOT NULL" +
                    "wage FLOAT(10, 5) NOT NULL," +
                    "is_manager BOOLEAN NOT NULL DEFAULT FALSE" + // max: 99999.99999  : 10 digits, 5 decimals
                    ")");

            statement.execute("CREATE TABLE IF NOT EXISTS menu (" +
                    "id INTEGER PRIMARY KEY NOT NULL," +
                    "name VARCHAR(20) NOT NULL," +
                    "price DOUBLE(10, 5) NOT NULL," +
                    "type SMALLINT NOT NULL" +
                    ")");
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
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

    // Manipulate data's

    public void editStaffData(int staffID, String newPassword, String newFirstName, String newLastName) throws DatabaseException {
        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE staff SET password = ?, first_name = ?, last_name = ? WHERE id = ?")) {
            statement.setString(1, newPassword);
            statement.setString(2, newFirstName);
            statement.setString(3, newLastName);
            statement.setInt(4, staffID);

            if (statement.executeUpdate() == 0)
                throw new DatabaseException("No staff found");

            Staff rStaff = findStaffByID(staffID);
            rStaff.setPassword(newPassword);
            rStaff.setLastName(newLastName);
            rStaff.setFirstName(newFirstName);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void deleteStaff(Staff rStaff) throws DatabaseException {
        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM staff WHERE id = ?")) {
            statement.setInt(1, rStaff.getID());
            statement.executeUpdate();

            staffList.remove(rStaff);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }


    public void addStaff(int newID, String newPassword, String newFirstName, String newLastName, boolean isManager) throws DatabaseException {
        Staff newStaff;
        if (isManager)
            newStaff = new Manager(newID, newLastName, newFirstName, newPassword);
        else
            newStaff = new Employee(newID, newLastName, newFirstName, newPassword);
        staffList.add(newStaff);

        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO staff(id, first_name, last_name, password, wage, is_manager) VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setInt(1, newStaff.getID());
            statement.setString(2, newStaff.getFirstName());
            statement.setString(3, newStaff.getLastName());
            statement.setString(4, newStaff.getPassword());
            statement.setDouble(5, newStaff.getWageRate());
            statement.setBoolean(6, newStaff instanceof Manager);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // database exception
        }
    }

    private void loadStaff() throws DatabaseException {
        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM staff")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    String pass = resultSet.getString("password");
                    double wageRate = resultSet.getDouble("wage");
                    boolean isManager = resultSet.getBoolean("is_manager");

                    Staff staff = isManager ?
                            new Manager(id, lastName, firstName, pass) :
                            new Employee(id, lastName, firstName, pass); // maybe isManager in Staff ?
                    staff.setWageRate(wageRate);
                    staffList.add(staff);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
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

    public void editMenuItemData(int id, String newName, double newPrice, byte menuType) throws DatabaseException {
        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE menu SET name = ?, price = ?, type = ? WHERE id = ?")) {
            statement.setString(1, newName);
            statement.setString(2, newPrice);
            statement.setString(3, menuType);
            statement.setInt(4, id);

            if (statement.executeUpdate() == 0)
                throw new DatabaseException("No item found");

            MenuItem rMenuItem = findMenuItemByID(id);
            rMenuItem.setName(newName);
            rMenuItem.setPrice(newPrice);
            rMenuItem.setType(menuType);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void deleteMenuItem(MenuItem rMenuItem) throws DatabaseException {
        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM menu WHERE id = ?")) {
            statement.setInt(1, rMenuItem.getID());
            statement.executeUpdate();

            menuList.remove(rMenuItem);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void addMenuItem(int newID, String newName, double newPrice, byte newType) throws DatabaseException {
        MenuItem newMenuItem = new MenuItem(newID, newName, newPrice, newType);
        menuList.add(newMenuItem);
        menuList.sort(new MenuItemComparator());

        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO menu(id, name, price, type) VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, newMenuItem.getID());
            statement.setString(2, newMenuItem.getName());
            statement.setByte(3, newMenuItem.getPrice());
            statement.setDouble(4, newMenuItem.getType());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // database exception
        }
    }

    private void loadMenuFile() throws DatabaseException {
        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM menu")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    Double price = resultSet.getString("price");
                    Byte type = resultSet.getString("type");

                    MenuItem rMenuItem = new MenuItem(Integer.parseInt(id), name, Double.parseDouble(price), Byte.parseByte(type));
                    menuList.add(rMenuItem);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    // Order

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
    public void loadData() throws DatabaseException {
        loadStaff();
        staffList.sort(new StaffComparator());
        loadMenuFile();
    }

    // File edit

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
