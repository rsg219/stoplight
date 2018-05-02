package jnm219;
/** Basic App.java class used to get a handle of uploading onto AWS */
// Import the Spark package, so that we can make use of the "get" function to
// create an HTTP GET route
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.*;
import com.google.gson.Gson;
import jnm219.models.incoming.CreateFolder;
import jnm219.models.outgoing.GetFolders;
import jnm219.models.outgoing.GetTasks;
import jnm219.models.outgoing.StructuredIntResponse;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

// Import Google's JSON library
import com.google.gson.*;

import java.sql.*;

import java.io.*;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Permission;
import java.util.*;
import javax.servlet.*;

//Importing the ability to access the database from Postgres
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.activation.MimetypesFileTypeMap;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.servlet.MultipartConfigElement;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Part;
import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;
import java.io.IOException;
import java.util.Map;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import spark.utils.IOUtils;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static net.spy.memcached.ops.StoreType.set;

public class App
{
    private static final HashMap<String, String> corsHeaders = new HashMap<String, String>();

    static {
        corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
        corsHeaders.put("Access-Control-Allow-Origin", "*");
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
        corsHeaders.put("Access-Control-Allow-Credentials", "true");
    }

    public static void main(String[] args) {
        PreparedStatement getTasks;
        // Get the port on which to listen for requests
        Spark.port(getIntFromEnv("PORT", 4567));

        String static_location_override = System.getenv("STATIC_LOCATION");
        if (static_location_override == null) {
            Spark.staticFileLocation("/web");
        } else {
            Spark.staticFiles.externalLocation(static_location_override);
        }
        final Gson gson = new Gson();
        Database db = Database.getDatabase(2);

        // Give the Database object a connection, fail if we cannot get one
        try {
            String dbUrl = System.getenv("JDBC_DATABASE_URL"); // Url for heroku database connection
            Connection conn = DriverManager.getConnection(dbUrl);
            getTasks = conn.prepareStatement("select * from tasks");

            if (conn == null) {
                System.err.println("Error: DriverManager.getConnection() returned a null object");
            }
            //db.mConnection = conn;
        } catch (SQLException e) {
            System.err.println("Error: DriverManager.getConnection() threw a SQLException");
            e.printStackTrace();
        }
        spark.Filter filter = new Filter() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                corsHeaders.forEach((key, value) -> {
                    response.header(key, value);
                });
            }
        };

        try {
            // Build a new authorized API client service.
            Drive service = GDrive.getDriveService();
            // Print the names and IDs for up to 10 files.
            FileList result = service.files().list()
                    .setMaxResults(10)
                    //.setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getItems();
            if (files == null || files.size() == 0) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File file : files) {
                    System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }


        Spark.before(filter);
        Spark.path("", () -> {

        // Set up a route for serving the main page
        Spark.get("/messages", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            return gson.toJson(new StructuredResponse("ok", null, db.selectAllMessages()));
        });
            //image tag points to spark route and wraps the return value of get statement
            Spark.post("/:projectId/:folderId/file", (request, response) -> {
                //System.out.println("Entering Messages");
                response.status(200);
                response.type("application/json");
                request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
                int suc = 0;
                String fileName = request.raw().getParameter("mFileName");
                String id = "Error";
                FileRet file = null;

                if (fileName == null) {
                    System.out.println("File Name Null");
                    fileName = "Error";
                }
                try (InputStream is = request.raw().getPart("mFile").getInputStream()) {
                    // Use the input stream to create a file
                    file = uploadFile(is, fileName);
                    id = file.id;
                    System.out.println("File Uploaded Successfully");
                } catch (Exception e) {
                    System.out.println("Failure: " + e);
                }
                boolean newId = db.insertFile(fileName, id);
                if (!newId) {
                    return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
                } else {
                    return gson.toJson(new StructuredResponse("ok", null, null));
                }
            });

            //Route for downloading a google drive file, given its unique id
            Spark.get("/:projectId/:folderId/:fileId/download", (request, response) -> {
                //SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
                String id = request.params("mFileId");
                response.status(200);
                response.type("image/png");
                Drive service;
                try {
                    service = GDrive.getDriveService();
                    OutputStream outputStream = new ByteArrayOutputStream();
                    String mimeType = service.files().get(id).execute().getMimeType();
                    System.out.println("Mime Type: " + mimeType);
                    response.type(mimeType);
                    service.files().get(id)
                            .executeMediaAndDownloadTo(outputStream);
                    ByteArrayOutputStream bos = (ByteArrayOutputStream) outputStream;
                    response.raw().getOutputStream().write(bos.toByteArray());
                    response.raw().getOutputStream().flush();
                    response.raw().getOutputStream().close();
                } catch (GoogleJsonResponseException e) {
                    System.out.println("Google Drive Connection Failure " + e);
                    GoogleJsonError error = e.getDetails();
                    System.out.print(error);
                }
                return response.raw();
            });


        Spark.get("/:projectId/:folderId/folder", (req,res) -> {
            res.status(200);
            res.type("application/json");
            System.out.println("Spark Called");
            ArrayList<GetFolders>data = db.selectAllFolders();
            return gson.toJson(new StructuredResponse("ok", null, data));
        });

        Spark.post("/:projectId/:folderId/folder", (request, response) -> {
            response.status(200);
            response.type("application/json");
            int id = Integer.parseInt(request.params("folderId"));
            System.out.println("Entering Folder");
            System.out.println(request.body().toString());
            CreateFolder incoming = gson.fromJson(request.body(), CreateFolder.class);
            System.out.println(incoming.mFolderName);
            String folderName = incoming.mFolderName;
            int newId = db.createFolder(folderName, id);
            if (newId == -1) {
                System.out.println("ERROR");
                return gson.toJson(new StructuredIntResponse("error", -1, null));
            } else {
                System.out.println("OK");
                return gson.toJson(new StructuredIntResponse("ok", newId, null));
            }
        });
        Spark.get("/:projectId/:folderId/file", (request, response) -> {
            response.status(200);
            response.type("application/json");
            int id = Integer.parseInt(request.params("folderId"));
            System.out.println("Spark Called");
            return gson.toJson(new StructuredResponse("ok", null, db.selectAllFiles(id)));

        });

        Spark.get("/:projectId/:folderId/:fileId/tasks", (req,res) -> {
            res.status(200);
            res.type("application/json");
            System.out.println("Spark Called");
            ArrayList<GetTasks>data = db.getTasks();
            return gson.toJson(new StructuredResponse("ok", null, data));
        });




        Spark.get("/", (req, res) -> {
            res.redirect("/index.html");
            return "";
        });
        Spark.get("/hello", (request, response) -> {
            return "Hello World!";
        });
    });
    }
    /**
     * Get an integer environment varible if it exists, and otherwise return the
     * default value.
     *
     * @envar      The name of the environment variable to get.
     * @defaultVal The integer value to use as the default if envar isn't found
     *
     * @returns The best answer we could come up with for a value for envar
     */
    static int getIntFromEnv(String envar, int defaultVal) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get(envar) != null) {
            return Integer.parseInt(processBuilder.environment().get(envar));
        }
        return defaultVal;
    }

    //Code for Uploading a file to our google drive
    //This first looks at the file name to figure out the MiMe type, then converts the contents to a File,
    //Next it uploads the file to Google Drive
    //Finally, it returns the name of the file and the unique file id
    public static FileRet uploadFile(InputStream in, String fileName) throws IOException {
        Drive service;
        String id = "error";
        int size = 0;
        String mimeFull = "image/png";
        FileRet ret = new FileRet(id, fileName);
        try {
            service = GDrive.getDriveService();

            String[] parts = fileName.split("\\.");
            String name = parts[0];
            String mime = parts[1];
            System.out.println(name + " " + mime);
            File body = new File();
            body.setTitle(name);
            body.setDescription("Description");
            if (mime.equals("png") || mime.equals("jpeg")) {
                mimeFull = "image/" + mime;
            }
            else if(mime.equals("txt")){
                mimeFull = "text/plain";
            }
            else if (mime.equals("pdf")) {
                mimeFull = "application/pdf";
            }
            body.setMimeType(mime);

            File file = service.files().insert(body,
                    new InputStreamContent(
                            mimeFull,
                            new ByteArrayInputStream(
                                    IOUtils.toByteArray(in)))).setFields("id").execute();
            id = file.getId();
            fileName = file.getTitle();

            System.out.println("INPUT ID: " + id + "FileName: " + fileName);

        } catch (GoogleJsonResponseException e) {
            System.out.println("Google Drive Connection Failure " + e);
            GoogleJsonError error = e.getDetails();
            System.out.print(error);
            return ret;
        }
        FileList result = service.files().list()
                .execute();
        List<File> files = result.getItems();
        if (files == null || files.size() == 0) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
            }
        }
        ret.fileName = fileName;
        ret.id = id;

        return ret;
    }
}



