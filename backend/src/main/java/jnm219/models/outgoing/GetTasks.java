package jnm219.models.outgoing;

public class GetTasks{
    public GetTasks(int mTaskId, String mTitle, int mPriority){
        this.mTitle = mTitle;
        this.mPriority = mPriority;
        this.mTaskId = mTaskId;
    }
    public String mTitle;
    public int mPriority;
    public int mTaskId;
}