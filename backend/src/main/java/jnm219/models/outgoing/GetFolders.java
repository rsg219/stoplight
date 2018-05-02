package jnm219.models.outgoing;

public class GetFolders {
    public GetFolders(int mFolderId, String mFolderName, int mProjectId){
        this.mFolderId = mFolderId;
        this.mFolderName = mFolderName;
        this.mProjectId = mProjectId;
    }
    public String mFolderName;
    public int mFolderId;
    public int mProjectId;
}
