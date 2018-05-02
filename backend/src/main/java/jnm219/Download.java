package jnm219;

import java.io.*;


/**
 * Helper Method for downloading a file
 */
public class Download {

    static void download(InputStream initialStream) {

        try {
            byte[] buffer = new byte[initialStream.available()];
            File targetFile = new File("C:\\Zebra.png");

            OutputStream outStream = new FileOutputStream(targetFile);
            outStream.write(buffer);
            outStream.close();
            /*
            ReadableByteChannel rbc = Channels.newChannel(initialStream);
            FileOutputStream fos = new FileOutputStream("pic.png");
            fos.getChannel().transferFrom(buffer, 0, Long.MAX_VALUE);
            */
        }catch(IOException e){
            System.out.println(e);
        }
    }
}