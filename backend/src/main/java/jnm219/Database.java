
package jnm219;

import jnm219.models.outgoing.GetFolders;
import jnm219.models.outgoing.GetTasks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.URISyntaxException;
// Imports for time functionality
import java.security.Timestamp;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import java.util.ArrayList;


public class Database {
    /**
     * The connection to the database.  When there is no connection, it should
     * be null.  Otherwise, there is a valid open connection
     */
    private Connection mConnection;

    /**
     * A prepared statement for getting all messages
     */
    private PreparedStatement mSelectAllMessage;
    private PreparedStatement mInsertName;
    private PreparedStatement mInsertFile;
    private PreparedStatement mInsertFolder;
    private PreparedStatement mSelectAllFiles;
    private PreparedStatement mGetTasks;
    private PreparedStatement mGetFolders;

    /**
     * Give the Database object a connection, fail if we cannot get one
     * Must be logged into heroku on a local computer to be able to use mvn heroku:deploy
     */
    private static Connection getConnection() throws URISyntaxException, SQLException {

        String dbUrl = System.getenv("JDBC_DATABASE_URL"); // Url for heroku database connection
        Connection conn = DriverManager.getConnection(dbUrl);
        return DriverManager.getConnection(dbUrl);
    }

    static Database getDatabase(int connectionType) {
        // Create an un-configured Database object
        Database db = new Database();

        // Give the Database object a connection, fail if we cannot get one
        try {
            Connection conn;
            if(connectionType == 1)
            {
                conn = getConnection();
            }
            else if(connectionType == 2)
            {
                conn = getConnection();
            }
            else
            {
                conn = getConnection();
            }
            if (conn == null) {
                System.err.println("Error: DriverManager.getConnection() returned a null object");
                return null;
            }
            db.mConnection = conn;
        } catch (SQLException e) {
            System.err.println("Error: DriverManager.getConnection() threw a SQLException");
            e.printStackTrace();
            return null;
        } catch (URISyntaxException e) {
            System.err.println("Error: DriverManager.getConnection() threw a URISyntaxException");
            e.printStackTrace();
            return null;
        }

        try{
            db.mSelectAllMessage = db.mConnection.prepareStatement("SELECT * FROM Users");
            db.mInsertName = db.mConnection.prepareStatement("INSERT INTO Users Values (default,?)");

            db.mSelectAllFiles = db.mConnection.prepareStatement("SELECT * FROM Files");
            db.mInsertFile = db.mConnection.prepareStatement("INSERT INTO Files Values (default,?,?)");
            db.mInsertFolder = db.mConnection.prepareStatement("INSERT into folders Values (default,?,?) RETURNING Id;");
            db.mGetTasks = db.mConnection.prepareStatement("select * from tasks");
            db.mGetFolders = db.mConnection.prepareStatement("select * from folders");


        } catch (SQLException e) {
            System.err.println("Error creating prepared statement");
            e.printStackTrace();
            db.disconnect();
            return null;
        }
        return db;
    }
    /**
     * Close the current connection to the database, if one exists.
     *
     * NB: The connection will always be null after this call, even if an
     *     error occurred during the closing operation.
     *
     * @return True if the connection was cleanly closed, false otherwise
     */
    boolean disconnect()
    {
        if (mConnection == null) {
            System.err.println("Unable to close connection: Connection was null");
            return false;
        }
        try {
            mConnection.close();
        } catch (SQLException e) {
            System.err.println("Error: Connection.close() threw a SQLException");
            e.printStackTrace();
            mConnection = null;
            return false;
        }
        mConnection = null;
        return true;
    }

    /**
     * Returning arraylist of rowmessages which displays all the message created by
     * all the users
     */
    ArrayList<DataRow> selectAllMessages() {
        ArrayList<DataRow> res = new ArrayList<DataRow>();
        try {
            ResultSet rs = mSelectAllMessage.executeQuery();
            while (rs.next()) {
                //System.err.println("NAMES: "+rs.getString("name"));
                res.add(new DataRow(rs.getInt("your_id"),rs.getString("name")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    boolean insertFile(String fileName,String fileId)
    {
        int rs=0;
        try {
            System.out.println("FileName: "+ fileName);
            mInsertFile.setString(1,fileName);
            mInsertFile.setString(2,fileId);
            rs +=mInsertFile.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    ArrayList<FileRow> selectAllFiles(int folderId) {
        ArrayList<FileRow> res = new ArrayList<FileRow>();
        try {
            mSelectAllFiles.setInt(1,folderId);
            ResultSet rs = mSelectAllFiles.executeQuery();
            System.out.println("HERE");
            while (rs.next()) {
                //System.err.println("NAMES: "+rs.getString("name"));
                res.add(new FileRow(rs.getInt("id"),rs.getString("fileName"),rs.getString("fileId")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    ArrayList<GetFolders> selectAllFolders() {
        ArrayList<GetFolders> res = new ArrayList<>();
        try {
            ResultSet rs = mGetFolders.executeQuery();
            System.out.println("HERE");
            while (rs.next()) {
                //System.err.println("NAMES: "+rs.getString("name"));
                res.add(new GetFolders(rs.getInt("id"),rs.getString("name"),rs.getInt("projectId")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    int createFolder(String name,int id)
    {
        int rs = -1;
        try {
            System.out.println("Enter create Folder Try");
            mInsertFolder.setString(1,name);
            mInsertFolder.setInt(2,id);
            mInsertFolder.execute();
            ResultSet set = mInsertFolder.getResultSet();
            set.next();
            rs =  set.getInt(1);
            System.out.println(rs);
            return rs;
        } catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }

    }

    ArrayList<GetTasks> getTasks() {
        ArrayList<GetTasks> data = new ArrayList<>();
        String status = "ok";
            try {
                ResultSet set = mGetTasks.executeQuery();
                while (set.next()) {
                    data.add(new GetTasks(set.getInt( "taskId"), set.getString("title"), set.getInt("priority")));
                }
                set.close();
                return  data;
            } catch (SQLException e) {
                status = "error";
                System.out.println("Error with getTasks");
                e.printStackTrace();
                return null;
            }
        }

    boolean insertName(String name)
    {
        int rs=0;
        try {
            System.out.println("New Name: "+name);
            mInsertName.setString(1,name);
            rs +=mInsertName.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}