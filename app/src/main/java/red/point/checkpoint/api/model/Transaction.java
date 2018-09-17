package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("id")
    @Expose
    private long id;

    @SerializedName("company_Id")
    @Expose
    private long companyId;

    @SerializedName("created_at")
    @Expose
    private String date;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("transactionable_id")
    @Expose
    private long transactionableId;

    @SerializedName("transactionable_type")
    @Expose
    private String transactionableType;

    @SerializedName("value")
    @Expose
    private double value;

    public Transaction(long id, long companyId, String date, String description, String type, long transactionableId, String transactionableType, double value) {
        this.id = id;
        this.companyId = companyId;
        this.date = date;
        this.description = description;
        this.type = type;
        this.transactionableId = transactionableId;
        this.transactionableType = transactionableType;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
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

    public long getTransactionableId() {
        return transactionableId;
    }

    public void setTransactionableId(long transactionableId) {
        this.transactionableId = transactionableId;
    }

    public String getTransactionableType() {
        return transactionableType;
    }

    public void setTransactionableType(String transactionableType) {
        this.transactionableType = transactionableType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
