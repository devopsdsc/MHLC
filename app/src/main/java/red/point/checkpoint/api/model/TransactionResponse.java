package red.point.checkpoint.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TransactionResponse {

    @SerializedName("data")
    @Expose
    private Transaction transaction;

    @SerializedName("error")
    @Expose
    private Error error;

    public TransactionResponse(Transaction transaction, Error error) {
        this.transaction = transaction;
        this.error = error;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
