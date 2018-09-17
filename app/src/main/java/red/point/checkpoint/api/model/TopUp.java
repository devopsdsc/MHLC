package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TopUp {

    @SerializedName("id")
    @Expose
    private long id;

    @SerializedName("user_id")
    @Expose
    private long userId;

    @SerializedName("company_id")
    @Expose
    private long companyId;

    @SerializedName("order_id")
    @Expose
    private String orderId;

    @SerializedName("item_type")
    @Expose
    private String itemType;

    @SerializedName("signature")
    @Expose
    private String signature;

    @SerializedName("purchase_state")
    @Expose
    private int purchaseState;

    @SerializedName("purchase_time")
    @Expose
    private long purchaseTime;

    @SerializedName("developer_payload")
    @Expose
    private String developerPayload;

    @SerializedName("sku")
    @Expose
    private String sku;

    @SerializedName("token")
    @Expose
    private String token;

    public TopUp(long id, long userId, long companyId, String orderId, String itemType, String signature, int purchaseState, long purchaseTime, String developerPayload, String sku, String token) {
        this.id = id;
        this.userId = userId;
        this.companyId = companyId;
        this.orderId = orderId;
        this.itemType = itemType;
        this.signature = signature;
        this.purchaseState = purchaseState;
        this.purchaseTime = purchaseTime;
        this.developerPayload = developerPayload;
        this.sku = sku;
        this.token = token;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getPurchaseState() {
        return purchaseState;
    }

    public void setPurchaseState(int purchaseState) {
        this.purchaseState = purchaseState;
    }

    public long getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(long purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getDeveloperPayload() {
        return developerPayload;
    }

    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
