/*************************************
 * Filename:  EmailMessage.java
 *************************************/

import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.*;

public class EmailMessage {
    /* SMTP-sender of the message (in this case, contents of From-header. */
    public String Sender;
    /* SMTP-recipient, or contents of To-header. */
    // EXTRA - CHANGED TO AN ARRAY
    public String[] Recipient;
    // EXTRA CC
    public String[] Cc;
    public boolean hasCc;
    /* Target MX-host */
    public String DestHost;
    private InetAddress DestAddr;

    public int DestHostPort;

    /* The headers and the body of the message. */
    public String Headers;
    public String Body;

    /* To make it look nicer */
    private static final String CRLF = "\r\n";

    /*
     * Create the message object by inserting the required headers from RFC 822
     * (From, To, Date).
     * EXTRA ADDED Cc String
     */
    public EmailMessage(String from, String to, String cc, String subject, String text,
                        String localServer, int localServerPort) throws UnknownHostException {

        if (cc != null && cc == "") {
            Cc = cc.split(";");
            hasCc = true;
        } else {
            hasCc = false;
        }

        // GET ARRAY OF RECEPENTS AND CCs
        Recipient = to.split(";");

        /* Remove whitespace */
        Sender = from.trim();
        for (String rec : Recipient) {
            rec = rec.trim();
        }
        if (hasCc) {
            for (String c : Cc) {
                c = c.trim();
            }
        }


        // ADDED CC HEADER, MULTIPLE CCs and RECIPIENTS
        Headers = "From: " + Sender + CRLF;

        Headers += "To: " + Recipient[0];
        for (int i = 1; i < Recipient.length; i++) {
            Headers += " ," + Recipient[i];
        }
        Headers += CRLF;

        if (hasCc) {
            Headers += "Cc: " + Cc[0];
            for (int i = 1; i < Cc.length; i++) {
                Headers += " ," + Cc[i];
            }
            Headers += CRLF;
        }

        Headers += "Subject: " + subject.trim() + CRLF;

        /*
         * A close approximation of the required format. Unfortunately only GMT.
         */
        SimpleDateFormat format = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss 'GMT'");
        String dateString = format.format(new Date());
        Headers += "Date: " + dateString + CRLF;

        /*
         * Get message. We must escape the message to make sure that there are
         * no single periods on a line. This would mess up sending the mail.
         */
        Body = escapeMessage(text);

        /*
         * Take the name of the local mailserver and map it into an InetAddress
         */
        DestHost = localServer;
        try {
            DestAddr = InetAddress.getByName(DestHost);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + DestHost);
            System.out.println(e);
            throw e;
        }
        DestHostPort = localServerPort;
    }

    /*
     * Check whether the message is valid. In other words, check that both
     * sender and recipient contain only one @-sign.
     * EXTRA - VALIDATE MULTIPLE RECEIPIENTS AND CCS
     */
    public boolean isValid() {
        int fromat = Sender.indexOf('@');

        if (fromat < 1 || (Sender.length() - fromat) <= 1) {
            System.out.println("Sender address is invalid");
            return false;
        }
        if (fromat != Sender.lastIndexOf('@')) {
            System.out.println("Sender address is invalid");
            return false;
        }

        for (String rec : Recipient) {
            int toat = rec.indexOf('@');

            if (toat < 1 || (rec.length() - toat) <= 1) {
                System.out.println(rec + " is invalid");
                return false;
            }
            if (toat != rec.lastIndexOf('@')) {
                System.out.println(rec + " is invalid");
                return false;
            }
        }

        if (hasCc) {
            for (String c : Cc) {
                int ccat = c.indexOf('@');

                if (ccat < 1 || (c.length() - ccat) <= 1) {
                    System.out.println(c + " is invalid");
                    return false;
                }
                if (ccat != c.lastIndexOf('@')) {
                    System.out.println(c + " is invalid");
                    return false;
                }
            }
        }

        return true;
    }

    /* For printing the message. */
    public String toString() {
        String res;

        res = "Sender: " + Sender + '\n';
        res += "Recipient: " + Recipient + '\n';
        res += "MX-host: " + DestHost + ", address: " + DestAddr + '\n';
        res += "Message:" + '\n';
        res += Headers + CRLF;
        res += Body;

        return res;
    }

    /*
     * Escape the message by doubling all periods at the beginning of a line.
     */
    private String escapeMessage(String body) {
        String escapedBody = "";
        String token;
        StringTokenizer parser = new StringTokenizer(body, "\n", true);

        while (parser.hasMoreTokens()) {
            token = parser.nextToken();
            if (token.startsWith(".")) {
                token = "." + token;
            }
            escapedBody += token;
        }
        return escapedBody;
    }
}
