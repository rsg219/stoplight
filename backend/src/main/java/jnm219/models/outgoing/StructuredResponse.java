package jnm219.models.outgoing;

public class StructuredResponse {
    public StructuredResponse(String mStatus, String mMessage, Object mData) {
        this.mStatus = mStatus;
        this.mMessage = mMessage;
        this.mData = mData;
    }

    public String mStatus;
    public String mMessage;
    public Object mData;

}