package jnm219.admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Database {
 
    /**
     * The connection to the database.  When there is no connection, it should
     * be null.  Otherwise, there is a valid open connection
     */
    private Connection mConnection;

    /**
     * A prepared statement for inserting into the tblUser
     */
    private PreparedStatement mInsertUser;
    private PreparedStatement mInsertUserUnauth;
    private PreparedStatement mSelectUnauthUserOne; // from the tblUnauthUser table
    public PreparedStatement mSelectUnauthUserAll; // public so App.java can execute() this.
    private PreparedStatement mRemoveUnauthUserOne; // from the tblUnauthUser table

    /**
     * A prepared statement for creating the unauthorized user table in the database
     */
    private PreparedStatement mCreateUnauthUserTable;

    /**
     * A prepared statement for creating the user table in the database
     */
    private PreparedStatement mCreateUserTable;

    /**
     * A prepared statement for creating the user's profile table in the database
     */
    private PreparedStatement mCreateProfileTable;

    /**
     * A prepared statement for creating the message in the database
     */
    private PreparedStatement mCreateMessageTable;

    /**
     * A prepared statement for creating the comment table in the database
     */
    private PreparedStatement mCreateCommentTable;

    /**
     * A prepared statement for creating the down-vote table in the database
     */
    private PreparedStatement mCreateDownVoteTable;

    /**
     * A prepared statement for creating the up-vote table in the database
     */
    private PreparedStatement mCreateUpVoteTable;

    /**
     * A prepared statement for creating the poll table in the database
     */
    private PreparedStatement mCreatePollTable;

    /**
     * A prepared statement for creating voting for polls in the database
     */
    private PreparedStatement mCreatePollVotesTable;
 
    /**
     * The Database constructor is private: we only create Database objects 
     * through the getDatabase() method.
     */
    private Database() {
    }
    // url for test
    private static Connection getConnection(String url) throws URISyntaxException, SQLException {
        //String dbUrl = App.getDBURLFromEnv();
        return DriverManager.getConnection(url);
    }
    
    /**
     * Get a fully-configured connection to the database
     * @return A Database object, or null if we cannot connect properly
     */
    static Database getDatabase(String dbUrl)  {
        // Create an un-configured Database object
        Database db = new Database();
  
        // Give the Database object a connection, fail if we cannot get one
        try {
            Connection conn = getConnection(dbUrl);
            if (conn == null) {
                System.err.println("Error: DriverManager.getConnection() returned a null object");
                return null;
            }
            db.mConnection = conn;
        } catch (SQLException e) {
            System.err.println("Error: DriverManager.getConnection() threw a SQLException");
            e.printStackTrace();
            return null;
        }
        catch (URISyntaxException e) {
            System.err.println("Error: DriverManager.getConnection() threw a URISyntaxException");
            e.printStackTrace();
            return null;
        }
        // Attempt to create all of our prepared statements.  If any of these 
        // fail, the whole getDatabase() call should fail
        try {
            // NB: we can easily get ourselves in trouble here by typing the
            //     SQL incorrectly.  We really should have things like "tblData"
            //     as constants, and then build the strings for the statements
            //     from those constants.

            // Note: no "IF NOT EXISTS" or "IF EXISTS" checks on table 
            // creation/deletion, so multiple executions will cause an exception
            db.mCreateUnauthUserTable = db.mConnection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tblUnauthUser ("
                +"username VARCHAR(255) PRIMARY KEY,"
                +"realname VARCHAR(255) NOT NULL,"
                +"email VARCHAR(255) NOT NULL)"
            );
            db.mCreateUserTable = db.mConnection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tblUser ("
                +"user_id Serial PRIMARY KEY,"
                +"username VARCHAR(255) UNIQUE NOT NULL,"
                +"realname VARCHAR(255) NOT NULL,"
                +"email VARCHAR(255) NOT NULL)"
            );
            db.mCreateProfileTable = db.mConnection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tblProfile ("
                +"username VARCHAR(255) PRIMARY KEY,"
                +"profile_text VARCHAR(500),"
                +"FOREIGN KEY (username) REFERENCES tblUser (username))"
            );
            db.mCreateMessageTable = db.mConnection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tblMessage ("
                +"message_id SERIAL PRIMARY KEY,"
                +"username VARCHAR(255),"
                +"subject VARCHAR(50) NOT NULL,"
                +"message VARCHAR(500),"
                +"url VARCHAR(500),"
                +"file_id VARCHAR(500),"
                +"createTime VARCHAR(50),"
                +"vote INTEGER,"
                +"poll_exist INTEGER,"
                +"FOREIGN KEY (username) REFERENCES tblUser (username))"
            );
            db.mCreateCommentTable = db.mConnection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tblComment ("
                +"comment_id SERIAL PRIMARY KEY,"
                +"username VARCHAR(255),"
                +"message_id INTEGER,"
                +"comment_text VARCHAR(255),"
                +"url VARCHAR(500),"
                +"file_id VARCHAR(500),"
                +"createTime VARCHAR(50),"
                +"FOREIGN KEY (username) REFERENCES tblUser (username),"
                +"FOREIGN KEY (message_id) REFERENCES tblMessage (message_id))"
            );
            db.mCreateDownVoteTable = db.mConnection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tblDownVote ("
                +"username VARCHAR(255),"
                +"message_id INTEGER,"
                +"PRIMARY KEY (username, message_id),"
                +"FOREIGN KEY (username) REFERENCES tblUser (username),"
                +"FOREIGN KEY (message_id) REFERENCES tblMessage (message_id))"
            );
            db.mCreateUpVoteTable = db.mConnection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tblUpVote ("
                +"username VARCHAR(255),"
                +"message_id INTEGER,"
                +"PRIMARY KEY (username, message_id),"
                +"FOREIGN KEY (username) REFERENCES tblUser (username),"
                +"FOREIGN KEY (message_id) REFERENCES tblMessage (message_id))"
            );
            db.mCreatePollTable = db.mConnection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tblPoll ("
                +"poll_id INTEGER,"
                +"subject varchar(255),"
                +"option1 varchar(255),"
                +"option2 varchar(255),"
                +"option3 varchar(255),"
                +"option4 varchar(255),"
                +"option5 varchar(255),"
                +"option6 varchar(255),"
                +"PRIMARY KEY (poll_id),"
                +"FOREIGN KEY (poll_id) REFERENCES tblMessage (message_id))"
            );
            db.mCreatePollVotesTable = db.mConnection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tblPollVotes ("
                +"pollOptionVoted INTEGER,"
                +"poll_id INTEGER,"
                +"username VARCHAR(255),"
                +"PRIMARY KEY (poll_id, username),"
                +"FOREIGN KEY (poll_id) REFERENCES tblPoll (poll_id),"
                +"FOREIGN KEY (username) REFERENCES tblUser (username))"
            );
 
            db.mInsertUser = db.mConnection.prepareStatement("INSERT INTO tblUser VALUES (default, ?, ?, ?)");
            db.mInsertUserUnauth = db.mConnection.prepareStatement("INSERT INTO tblUnauthUser VALUES (?, ?, ?)");
            db.mSelectUnauthUserOne = db.mConnection.prepareStatement("SELECT * FROM tblUnauthUser WHERE username=?");
            db.mSelectUnauthUserAll = db.mConnection.prepareStatement("SELECT * FROM tblUnauthUser");
            db.mRemoveUnauthUserOne = db.mConnection.prepareStatement("DELETE FROM tblUnauthUser WHERE username=?");
            
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
    boolean disconnect() {
        if (mConnection == null) {
            System.err.println("Unable to close connection: Connection was null");
            return false;
        }
        try {
            mConnection.close();
        } catch (SQLException e) {
            System.err.println("Error: Connection.close() threw a SQLException");
            //e.printStackTrace();
            mConnection = null;
            return false;
        }
        mConnection = null;
        return true;
    }

    /**
     * Create all tables: tblUser, tblMessage, tblComment, tblUpVote, tblDownVote.  
     * If it already exists, this will print an error
     */
    boolean createAllTables() {
        try {
            mCreateUnauthUserTable.execute();
            mCreateUserTable.execute();
            mCreateProfileTable.execute();
            mCreateMessageTable.execute();
            mCreateCommentTable.execute();
            mCreateDownVoteTable.execute();
            mCreateUpVoteTable.execute();

            mCreatePollTable.execute();
            mCreatePollVotesTable.execute();
        } catch (SQLException e) {
            System.err.println("Table is already created");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /** Create table based on parameter given */
    boolean createTable(char action) {
        try {
            if (action == 'U') {            // tblUser
                mCreateUserTable.execute();
                mCreateProfileTable.execute();  // following tables rely on tblUser
                mCreateMessageTable.execute();
                mCreateCommentTable.execute();
                mCreateUpVoteTable.execute();
                mCreateDownVoteTable.execute();
            } else if (action == 'a') {
                mCreateUnauthUserTable.execute();
            } else if (action == 'p') {     // tblProfile
                mCreateProfileTable.execute();
            } else if (action == 'm') {     // tblMessage
                mCreateMessageTable.execute();
                mCreateCommentTable.execute(); // following tables rely on tblMessage
                mCreateUpVoteTable.execute();
                mCreateDownVoteTable.execute();
            } else if (action == 'c') {     // tblComment
                mCreateCommentTable.execute();
            } else if (action == 'u') {     // tblUpVote
                mCreateUpVoteTable.execute();
            } else if (action == 'd') {     // tblDownVote
                mCreateDownVoteTable.execute();
            } else if (action == 'v'){
                mCreatePollTable.execute();
                mCreatePollVotesTable.execute();
            }else {
                System.err.println("Invalid input for creating table.");
                System.err.println("Options are: [U]ser, [p]rofile, [m]essage, [c]omment, [d]ownvote, [u]pvote, [v]poll");
                return false;
            }    
        } catch (SQLException e) {
            System.err.println("Table is already created. Error: " + e);
            return false;
        }
        return true;
    }

    /**
     * Remove tblData from the database.  If it does not exist, this will print
     * an error.
     */
    boolean dropTable(String table) {
        Statement stmt = null;        
        try {
            stmt = mConnection.createStatement();
            String sql = "DROP TABLE " + table + " CASCADE";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("There is no table to drop");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    boolean dropAllTables() {
        boolean result;
        String[] tables = {"tblUpVote", "tblDownVote", "tblComment", "tblProfile", "tblMessage", "tblUser", "tblUnauthUser", "tblPoll", "tblPollVotes"};
        try{
            mCreatePollTable.execute();
            mCreatePollVotesTable.execute();
        }catch(SQLException e){
            //
        }
        for (int i = 0; i < tables.length; i++) {
            result = dropTable(tables[i]);
            if (result == false) {
                return false;
            }
        }
        return true;
    }

    boolean rejectUser(String username) {
        try {
            mRemoveUnauthUserOne.setString(1, username);
            mRemoveUnauthUserOne.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to reject user: " + e);
            return false;
        }
        return true;
    }

    public boolean selectUnauthUserAll() {
        try {
            ResultSet rs = mSelectUnauthUserAll.executeQuery();
            int columnsNumber = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print("   ");
                    String columnValue = rs.getString(i);
                    System.out.print(columnValue);
                }
                System.out.println("");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    boolean addToTblUnauthTable(String username, String realname, String email) {
        try {
            mInsertUserUnauth.setString(1, username);
            mInsertUserUnauth.setString(2, realname);
            mInsertUserUnauth.setString(3, email);
            mInsertUserUnauth.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    boolean authorizeUser(String[] credentials) {   // credentials[username, realname, email, password]
        try {
            String username = credentials[0];
            Password pw = new Password();

            mSelectUnauthUserOne.setString(1, username);
            ResultSet rs = mSelectUnauthUserOne.executeQuery();
            
            if (rs.next())
            {
                String realname = rs.getString("realname");
                credentials[1] = realname;
                String email = rs.getString("email");
                credentials[2] = email;
                mInsertUser.setString(1, username);
                mInsertUser.setString(2, realname);
                mInsertUser.setString(3, email);
                byte [] salt = pw.getSalt();
                String password = pw.getPassword();
                credentials[3] = password;
                byte [] saltedPassword = pw.encryptPw (password, salt);
                mInsertUser.setBytes(4,salt);
                mInsertUser.setBytes(5, saltedPassword);
                mInsertUser.executeUpdate();
                mRemoveUnauthUserOne.setString(1, username);    // remove the authorized user from the unauthuser table
                mRemoveUnauthUserOne.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    String authorizeUserForBackend(String[] credentials) {   // credentials[username, realname, email, password]
        String password = "";        
        try {
            String username = credentials[0];
            Password pw = new Password();

            mSelectUnauthUserOne.setString(1, username);
            ResultSet rs = mSelectUnauthUserOne.executeQuery();
            
            if (rs.next())
            {
                String realname = rs.getString("realname");
                credentials[1] = realname;
                String email = rs.getString("email");
                credentials[2] = email;
                mInsertUser.setString(1, username);
                mInsertUser.setString(2, realname);
                mInsertUser.setString(3, email);
                byte [] salt = pw.getSalt();
                password = "liger";
                credentials[3] = password;
                byte [] saltedPassword = pw.encryptPw (password, salt);
                mInsertUser.setBytes(4,salt);
                mInsertUser.setBytes(5, saltedPassword);
                mInsertUser.executeUpdate();
                mRemoveUnauthUserOne.setString(1, username);    // remove the authorized user from the unauthuser table
                mRemoveUnauthUserOne.executeUpdate();
            }
            else
            {
                System.out.println("username not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return password;
    }

}
  