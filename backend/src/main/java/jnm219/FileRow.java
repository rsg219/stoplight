package jnm219;


/**
 * Holds necessary info for a file
 */
public class FileRow {
    /**
     * The unique identifier associated with this element.  It's final, because
     * we never want to change it.
     */
    public final int mId;

    /**
     * What the name of the file is
     */
    public String mFileName;
    //What the Unique file Id is to look for inside of the google drive
    public String mFileId;

    /**
     * Create a new DataRow with the provided id and title/content, and a
     * creation date based on the system clock at the time the constructor was
     * called
     *
     * @param id The id to associate with this row.  Assumed to be unique
     *           throughout the whole program.
     *
     * @param title The title string for this row of data
     *
     * @param content The content string for this row of data
     */
    FileRow(int id, String fileName, String fileId) {
        mId = id;
        mFileName = fileName;
        mFileId = fileId;
    }

}