import java.time.LocalDateTime;

public class Transaction {
    
    String txnId;
    String customerId;
    String merchant;
    String category;
    double amount;
    LocalDateTime txnTime;
    String city;
    boolean fraud;
    public Transaction() {
    }
    public Transaction(String txnId, String customerId, String merchant, String category, double amount, LocalDateTime txnTime, String city, boolean fraud) {
        this.txnId = txnId;
        this.customerId = customerId;
        this.merchant = merchant;
        this.category = category;
        this.amount = amount;
        this.txnTime = txnTime;
        this.city = city;
        this.fraud = fraud;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(LocalDateTime txnTime) {
        this.txnTime = txnTime;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isFraud() {
        return fraud;
    }

    public void setFraud(boolean fraud) {
        this.fraud = fraud;
    }
}

