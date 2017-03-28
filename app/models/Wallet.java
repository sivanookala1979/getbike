package models;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by adarsht on 09/12/16.
 */
@Entity
public class Wallet extends Model {
    @Id
    Long id;

    Long userId;
    Date transactionDateTime;
    Double amount;
    String description;
    String type;
    String mobileNumber;
    String operator;
    String circle;
    String walletName;
    String isAmountPaidStatus;
    Date   statusActedAt;
    boolean notificationSeen = true;
    public transient String userName;
    @Column(length = 4096)
    String pgDetails;
    public static Finder<Long, Wallet> find = new Finder<Long, Wallet>(Wallet.class);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(Date transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public String getPgDetails() {
        return pgDetails;
    }

    public void setPgDetails(String pgDetails) {
        this.pgDetails = pgDetails;
    }

    public String getIsAmountPaidStatus() {
        return isAmountPaidStatus;
    }

    public void setIsAmountPaidStatus(String isAmountPaidStatus) {
        this.isAmountPaidStatus = isAmountPaidStatus;
    }

    public String getUserName() {
        //TODO This is not good coding. Please stop finding in the model.
        return "";
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getStatusActedAt() {
        return statusActedAt;
    }

    public void setStatusActedAt(Date statusActedAt) {
        this.statusActedAt = statusActedAt;
    }

    public boolean isNotificationSeen() {
        return notificationSeen;
    }

    public void setNotificationSeen(boolean notificationSeen) {
        this.notificationSeen = notificationSeen;
    }
}
