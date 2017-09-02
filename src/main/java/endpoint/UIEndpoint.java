package endpoint;

import java.io.IOException;
import java.sql.*;

public class UIEndpoint {
    static final String CREATE_USER_HTML = "<html><head><title>Create User</title></head>" +
            "   <body>\n" +
            "      <div align='center'>" +
            "          <h2>Create User</h2>\n" +
            "          <form action='/createUser' method=\"post\"><br/><br>\n" +
            "           <table>"+
            "              <tr><td>User Name</td><td><input type=\"text\" name=\"user\"></td><tr>" +
            "              <tr><td>Password</td><td><input type=\"password\" name=\"pass\"></td><tr>" +
            "           </table>"+
            "              <input type=\"submit\" value=\"submit\" name=\"submit\" />" +
            "          </form>" +
            "          <p>%s</p>"+
            "      </div>" +
            "   </body>" +
            "</html>";

    static final String SEND_MESSAGE_HTML = "<html><head><title>Send Message</title></head>" +
            "   <body>\n" +
            "      <div align='center'>" +
            "          <h2>Send Message</h2><br/>\n" +
            "          <form action='/sendMessage' method=\"post\">\n" +
            "           <table>"+
            "              <tr><td>Sender</td><td><input type=\"text\" name=\"sender\"></td><tr>" +
            "              <tr><td>Receiver</td><td><input type=\"text\" name=\"receiver\"></td><tr>" +
            "              <tr><td>Message</td><td><input type=\"text\" name=\"message\"></td><tr>" +
            "           </table>"+
            "              <input type=\"submit\" value=\"submit\" name=\"submit\" />" +
            "          </form>" +
            "          <p>%s</p>"+
            "      </div>" +
            "   </body>" +
            "</html>";

    static final String FETCH_MESSAGES_HTML = "<html><head><title>Fetch Messages</title>" +
            "</head>" +
            "   <body>\n" +
            "      <div align='center'>" +
            "          <h2>Fetch Messages</h2>\n" +
            "          <form action='/fetchMessages' method=\"post\"><br/>\n" +
            "           <table>"+
            "              <tr><td>First User</td><td><input type=\"text\" name=\"sender\"></td><tr>" +
            "              <tr><td>Second User</td><td><input type=\"text\" name=\"receiver\"></td><tr>" +
            "              <tr><td>Number Of Messages</td><td><input type=\"number\"  min=\"0\" onkeypress=\"return isNumberKey(event)\" name=\"numMessages\"></td><tr>" +
            "              <tr><td>Page Number</td><td><input type=\"number\"  min=\"0\" onkeypress=\"return isNumberKey(event)\" name=\"pageNum\"></td><tr>" +
            "           </table>"+
            "              <input type=\"submit\" value=\"submit\" name=\"submit\" />" +
            "          </form>" +
            "          <p>%s</p>"+
            "      </div>" +
            "   </body>" +
            "</html>";

    public String createUser() throws SQLException {
        return String.format(CREATE_USER_HTML, "");
    }

    public String sendMessage() throws SQLException, IOException {
        return String.format(SEND_MESSAGE_HTML, "");
    }

    public String fetchMessages() throws SQLException, IOException {
        return String.format(FETCH_MESSAGES_HTML, "");
    }
}
