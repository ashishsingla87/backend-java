package endpoint;

import com.google.common.collect.Lists;
import io.netty.util.internal.StringUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

import static endpoint.DataService.URL_CONTENT_TYPE.IMAGE;
import static endpoint.DataService.URL_CONTENT_TYPE.OTHER;
import static endpoint.DataService.URL_CONTENT_TYPE.VIDEO;
import static endpoint.UIEndpoint.CREATE_USER_HTML;
import static endpoint.UIEndpoint.FETCH_MESSAGES_HTML;
import static endpoint.UIEndpoint.SEND_MESSAGE_HTML;

public class DataService {
    private static final String CREATE_USER = "insert into user_info (user_id, password) values (?,?)";
    private static final String SEND_MESSAGE = "insert into messages (sender, receiver, message) values (?,?,?)";
    private static final String SEND_MESSAGE_IMAGE_URL = "insert into messages (sender, receiver, message, image_width, image_height, message_type) values (?,?,?,?,?,?)";
    private static final String SEND_MESSAGE_VIDEO_URL = "insert into messages (sender, receiver, message, video_length, video_source, message_type) values (?,?,?,?,?,?)";
    private static final String FETCH_MESSAGE_IDS = "select message_id from messages where (sender = ? and receiver = ?) or (sender = ? and receiver = ?) order by message_id asc";
    private static final String FETCH_MESSAGES = "select message from messages where message_id in";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAIL";
    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";
    private static final String MESSAGE = "message";
    private static final String USER = "user";
    private static final String PASSWORD = "pass";
    private static final String PAGE_NUM = "pageNum";
    private static final String NUM_MESSAGES = "numMessages";
    private static final String VID_SOURCE = "YOUTUBE";
    private static final int IMG_WIDTH = 100;
    private static final int IMG_HEIGHT = 100;
    private static final int VID_LENGTH_IN_SECONDS = 100;
    private static final int RADIX = 10;
    private static final Pattern URL_PATTERN = Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$");
    private final BasicDataSource dataSource;

    public DataService(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public enum URL_CONTENT_TYPE{
        IMAGE,
        VIDEO,
        OTHER
    }

    public String createUser(Map<String, String> params) throws SQLException {
        String status = SUCCESS;
        Assert.notNullOrEmpty(params, USER);
        Assert.notNullOrEmpty(params, PASSWORD);
        try(Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(CREATE_USER);
            preparedStatement.setString(1, params.get(USER));
            preparedStatement.setString(2, BCrypt.hashpw(params.get(PASSWORD), BCrypt.gensalt()));
            preparedStatement.execute();
        }catch (SQLIntegrityConstraintViolationException e){
            status = FAILURE+": User name "+ params.get("user")+" already exists, please choose a different username";
        }
        return String.format(CREATE_USER_HTML, status);
    }

    public String sendMessage(Map<String, String> params) throws SQLException, IOException {
        String status = FAILURE;
        if(!params.get(SENDER).equals(params.get(RECEIVER))){
            Assert.notNullOrEmpty(params, SENDER);
            Assert.notNullOrEmpty(params, RECEIVER);
            Assert.notNullOrEmpty(params, MESSAGE);
            PreparedStatement preparedStatement;
            try (Connection connection = dataSource.getConnection()) {
                if (URL_PATTERN.matcher(params.get(MESSAGE)).find()) {
                    MessageContentType messageContentType = getContentType(params.get(MESSAGE));
                    switch (messageContentType.contentType) {
                        case IMAGE:
                            preparedStatement = minimumPrepStmtForSendMessage(params, connection, SEND_MESSAGE_IMAGE_URL);
                            preparedStatement.setInt(4, IMG_WIDTH);
                            //noinspection SuspiciousNameCombination
                            preparedStatement.setInt(5, IMG_HEIGHT);
                            preparedStatement.setString(6, messageContentType.contentTypeVal);
                            break;
                        case VIDEO:
                            preparedStatement = minimumPrepStmtForSendMessage(params, connection, SEND_MESSAGE_VIDEO_URL);
                            preparedStatement.setInt(4, VID_LENGTH_IN_SECONDS);
                            preparedStatement.setString(5, VID_SOURCE);
                            preparedStatement.setString(6, messageContentType.contentTypeVal);
                            break;
                        default:
                            preparedStatement = minimumPrepStmtForSendMessage(params, connection, SEND_MESSAGE);
                            break;
                    }
                } else {
                    preparedStatement = minimumPrepStmtForSendMessage(params, connection, SEND_MESSAGE);
                }
                try {
                    preparedStatement.execute();
                    status = SUCCESS;
                }catch (Throwable t){
                    status = FAILURE;
                }
            }
        }
        return String.format(SEND_MESSAGE_HTML, status);
    }

    public String fetchMessages(Map<String, String> params) throws SQLException, IOException {
        Assert.notNullOrEmpty(params, SENDER);
        Assert.notNullOrEmpty(params, RECEIVER);
        try(Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(FETCH_MESSAGE_IDS);
            preparedStatement.setString(1, params.get(SENDER));
            preparedStatement.setString(2, params.get(RECEIVER));
            preparedStatement.setString(3, params.get(RECEIVER));
            preparedStatement.setString(4, params.get(SENDER));
            List<Integer> messageIds = new ArrayList<>();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    messageIds.add(resultSet.getInt(1));
                }
            }
            if(params.get(PAGE_NUM)!=null && params.get(NUM_MESSAGES)!=null && isInteger(params.get(PAGE_NUM))&& isInteger(params.get(NUM_MESSAGES))){
                int pageNum = Integer.parseInt(params.get(PAGE_NUM));
                int numMessages = Integer.parseInt(params.get(NUM_MESSAGES));
                int maxMessageIndex = pageNum*numMessages;
                int minMessageIndex = maxMessageIndex - numMessages;
                if(messageIds.size()>minMessageIndex && minMessageIndex>=0){
                    messageIds = messageIds.subList(minMessageIndex, messageIds.size()>maxMessageIndex?maxMessageIndex:messageIds.size());
                }else {
                    messageIds = Collections.emptyList();
                }
            }

            List<List<Integer>> messageIdChunks = Lists.partition(messageIds, 100);
            StringBuilder outputMessage = new StringBuilder();
            for(List<Integer> chunk: messageIdChunks){
                StringBuilder builder = new StringBuilder().append("(");
                for( int i = 0 ; i < chunk.size(); i++ ) {
                    builder.append("?,");
                }
                builder.setCharAt( builder.length() -1, ')' );
                String query = FETCH_MESSAGES + builder.toString();
                preparedStatement = connection.prepareStatement(query);
                int index = 1;
                for( int messageId : chunk ) {
                    preparedStatement.setInt(  index++, messageId );
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        outputMessage.append(resultSet.getString(1)).append("<br/>");
                    }
                }
            }
            String output = outputMessage.toString().trim();
            return String.format(FETCH_MESSAGES_HTML, "".equals(output)?"No Messages to display":output);
        }
    }



    private PreparedStatement minimumPrepStmtForSendMessage(Map<String, String> params, Connection connection, String sendMessage) throws SQLException {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(sendMessage);
        preparedStatement.setString(1, params.get(SENDER));
        preparedStatement.setString(2, params.get(RECEIVER));
        preparedStatement.setString(3, params.get(MESSAGE));
        return preparedStatement;
    }

    private static class Assert {
        private static void notNullOrEmpty(Map<String, String> params, String param) {
            if (StringUtil.isNullOrEmpty(params.get(param))) {
                throw new RuntimeException  (param +" cannot be empty for request "+params.toString());
            }
        }
    }

    private static class MessageContentType{
        private final URL_CONTENT_TYPE contentType;
        private final String contentTypeVal;

        private MessageContentType(URL_CONTENT_TYPE contentType, String contentTypeVal) {
            this.contentType = contentType;
            this.contentTypeVal = contentTypeVal;
        }
    }

    private static MessageContentType getContentType(String url) throws IOException {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection con =
                (HttpURLConnection) new URL(url).openConnection();
        String contentType = con.getContentType();
        if(contentType!=null && contentType.startsWith("image/")){
            return new MessageContentType(IMAGE, contentType);
        }else if(contentType!=null && contentType.startsWith("video/")){
            return new MessageContentType(VIDEO, contentType);
        }else {
            return new MessageContentType(OTHER, contentType);
        }
    }

    private static boolean isInteger(String s) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i), RADIX) < 0) return false;
        }
        return true;
    }
}