package com.example.ca_lalay.model;

public class ConversationModel {

    private String conversationId;

    private String customerId;
    private String customerName;
    private String customerImage;

    private String shopId;
    private String shopName;
    private String shopImage;

    private String lastMessage;
    private String lastTimestamp;

    private boolean readByCustomer;
    private boolean readByShop;

    // Required empty constructor
    public ConversationModel() {}

    public ConversationModel(
            String conversationId,
            String customerId,
            String customerName,
            String customerImage,
            String shopId,
            String shopName,
            String shopImage,
            String lastMessage,
            String lastTimestamp,
            boolean readByCustomer,
            boolean readByShop

    ) {
        this.conversationId = conversationId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerImage = customerImage;
        this.shopId = shopId;
        this.shopName = shopName;
        this.shopImage = shopImage;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
        this.readByCustomer = readByCustomer;
        this.readByShop = readByShop;
    }

    // Getters & Setters


    public boolean isReadByCustomer() { return readByCustomer; }
    public void setReadByCustomer(boolean readByCustomer) { this.readByCustomer = readByCustomer; }

    public boolean isReadByShop() { return readByShop; }
    public void setReadByShop(boolean readByShop) { this.readByShop = readByShop; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerImage() { return customerImage; }
    public void setCustomerImage(String customerImage) { this.customerImage = customerImage; }

    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getShopImage() { return shopImage; }
    public void setShopImage(String shopImage) { this.shopImage = shopImage; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastTimestamp() { return lastTimestamp; }
    public void setLastTimestamp(String lastTimestamp) { this.lastTimestamp = lastTimestamp; }
}