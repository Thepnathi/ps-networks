/*************************************
 * Filename:  HttpInteract.java
 * Names:
 * Student-IDs:
 * Date:
 *************************************/

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * Class for downloading one object from http server.
 */
public class HttpInteract {
    private String host;
    private String path;
    private String requestMessage;


    private static final int HTTP_PORT = 80;
    private static final String CRLF = "\r\n";
    private static final int BUF_SIZE = 4096;
    private static final int MAX_OBJECT_SIZE = 102400;

    /* Create a HttpInteract object. */
    public HttpInteract(String url) {

        /* Split "URL" into "host name" and "path name", and
         * set host and path class variables.
         * if the URL is only a host name, use "/" as path
         */

        /* Fill in */
        URL urlObject;
        try {
            urlObject = new URL("http://" + url);
        } catch (MalformedURLException e) {
            System.out.println("Incorrect URL");
            return;
        }

        host = urlObject.getHost();
        path = urlObject.getPath();
        if (path == "") {
            path = "/";
        }

        System.out.println("host: " + host);
        System.out.println("path: " + path);



        /* Construct requestMessage, add a header line so that
         * server closes connection after one response. */

        /* Fill in */

        requestMessage = "GET " + path + " HTTP/1.1\r\n"
                + "Host: " + host + "\r\n"
                + "\r\n";

        return;
    }


    /* Send Http request, parse response and return requested object
     * as a String (if no errors),
     * otherwise return meaningful error message.
     * Don't catch Exceptions. EmailClient will handle them. */
    public String send() throws IOException {

        /* buffer to read object in 4kB chunks */
        char[] buf = new char[BUF_SIZE];

        /* Maximum size of object is 100kB, which should be enough for most objects.
         * Change constant if you need more. */
        char[] body = new char[MAX_OBJECT_SIZE];

        String statusLine = "";    // status line
        int status;        // status code
        String headers = "";    // headers
        int bodyLength = -1;    // lenghth of body

        String[] tmp;

        /* The socket to the server */
        Socket connection;

        /* Streams for reading from and writing to socket */
        BufferedReader fromServer;
        DataOutputStream toServer;

        System.out.println("Connecting server: " + host + CRLF);

        /* Connect to http server on port 80.
         * Assign input and output streams to connection. */
        connection = new Socket(host, HTTP_PORT);
        fromServer = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        toServer = new DataOutputStream(connection.getOutputStream());

        System.out.println("Send request:\n" + requestMessage);

        toServer.writeBytes(requestMessage);
        toServer.flush();

        /* Read the status line from response message */
        statusLine = fromServer.readLine();
        System.out.println("Status Line:\n" + statusLine + CRLF);

        /* Extract status code from status line. If status code is not 200,
         * close connection and return an error message.
         * Do NOT throw an exception */
        /* Fill in */
        if (!(statusLine.toLowerCase().contains("200") || statusLine.toLowerCase().contains("301"))) {
            connection.close();
            throw new IOException("200 reply not received from server.");
        }

        /* Read header lines from response message, convert to a string,
         * and assign to "headers" variable.
         * Recall that an empty line indicates end of headers.
         * Extract length  from "Content-Length:" (or "Content-length:")
         * header line, if present, and assign to "bodyLength" variable.
         */
        /* Fill in */        // requires about 10 lines of code
        String line;
        while ((line = fromServer.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }

            if (line.toLowerCase().contains("content-length")) {
                // extract numbers from line
                Pattern p = Pattern.compile("-?\\d+");
                Matcher m = p.matcher(line);
                // conert string to integer
                while (m.find()) {
                    bodyLength = Integer.parseInt(m.group());
                }
            }

            headers += line + CRLF;
        }
        System.out.println("Headers:\n" + headers + CRLF);


        /* If object is larger than MAX_OBJECT_SIZE, close the connection and
         * return meaningful message. */
        /* Fill in */
        if (bodyLength > MAX_OBJECT_SIZE) {
            System.out.println("Object is too large");
            connection.close();
            return ("Requested object is too large. Object size = " + bodyLength);
        }

        /* Read the body in chunks of BUF_SIZE using buf[] and copy the chunk
         * into body[]. Stop when either we have
         * read Content-Length bytes or when the connection is
         * closed (when there is no Content-Length in the response).
         * Use one of the read() methods of BufferedReader here, NOT readLine().
         * Make sure not to read more than MAX_OBJECT_SIZE characters.
         */
        int bytesRead = 0;

        /* Fill in */   // Requires 10-20 lines of code
        while (bytesRead < MAX_OBJECT_SIZE) {
            int numOfCharsRead = fromServer.read(buf, 0, BUF_SIZE);
            if (numOfCharsRead == -1) {
                // end of stream has been reached; read() returns -1
                break;
            }
            // Copy buffer into body
            for (int i = 0; i < numOfCharsRead && (i + bytesRead) < MAX_OBJECT_SIZE; i++) {
                body[bytesRead + i] = buf[i];
            }

            // 1 char takes up 1 byte
            bytesRead += numOfCharsRead;
        }


        /* At this point body[] should hold to body of the downloaded object and
         * bytesRead should hold the number of bytes read from the BufferedReader
         */

        /* Close connection and return object as String. */
        System.out.println("Done reading file. Closing connection.");
        connection.close();
        return (new String(body, 0, bytesRead));
    }
}
