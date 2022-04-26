public class OrderDetail
{
    private final int itemID;
    private final String itemName;
    private final double price;
    private byte quantity;
    private double totalPrice;

    // Constructor for objects of class OrderDetail
    public OrderDetail(MenuItem newMenuItem, byte newQuantity)
    {
        this.itemID     = newMenuItem.getID();
        this.itemName   = newMenuItem.getName();
        this.price      = newMenuItem.getPrice();
        this.quantity   = newQuantity;
        this.totalPrice = this.price * this.quantity;
    }
    
    // Getter
    public int getItemID()
    {
        return this.itemID;
    }
    public String getItemName()
    {
        return this.itemName;
    }
    public byte getQuantity()
    {
        return this.quantity;
    }
    public double getTotalPrice()
    {
        return this.totalPrice;
    }
    
    public void addQuantity(byte add)
    {
        quantity += add;
        totalPrice = price * quantity;
    }
        
}
