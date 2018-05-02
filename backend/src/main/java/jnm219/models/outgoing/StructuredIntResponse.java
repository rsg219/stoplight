package jnm219.models.outgoing;

public class StructuredIntResponse {
    public StructuredIntResponse(String mStatus, int mId, Object mData) {
        this.mStatus = mStatus;
        this.mId = mId;
        this.mData = mData;
    }

    public String mStatus;
    public int mId;
    public Object mData;

}