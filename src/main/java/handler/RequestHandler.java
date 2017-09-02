package handler;

import endpoint.DataService;
import endpoint.UIEndpoint;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import model.Request;
import model.Response;

import java.io.IOException;
import java.sql.SQLException;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;

public class RequestHandler extends ChannelInboundHandlerAdapter {
    private static final String CREATE_USER = "/createUser";
    private static final String FETCH_MESSAGES = "/fetchMessages";
    private static final String SEND_MESSAGE = "/sendMessage";
    private static final String INVALID_REQUEST = "An Invalid request was made, please request a valid uri";
    private static final String FAVICON_ICO = "/favicon.ico";
    private static final String FAVICON_RESPONSE = "image/x-icon";
    private static final String TEXT_HTML = "text/html";
    private final DataService dataService;
    private final UIEndpoint uiEndpoint;

    public RequestHandler(DataService dataService) {
        this.dataService = dataService;
        this.uiEndpoint = new UIEndpoint();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws SQLException, IOException {
        Request request = (Request) msg;
        switch (request.getRequestPath()) {
            case FAVICON_ICO:
                ctx.writeAndFlush(new Response(FAVICON_RESPONSE, EMPTY_STRING));
                break;
            case CREATE_USER:
                if(request.getParameters().isEmpty()){
                    ctx.writeAndFlush(new Response(TEXT_HTML, uiEndpoint.createUser()));
                }else {
                    ctx.writeAndFlush(new Response(TEXT_HTML, dataService.createUser(request.getParameters())));
                }
                break;
            case SEND_MESSAGE:
                if(request.getParameters().isEmpty()){
                    ctx.writeAndFlush(new Response(TEXT_HTML, uiEndpoint.sendMessage()));
                }else {
                    ctx.writeAndFlush(new Response(TEXT_HTML, dataService.sendMessage(request.getParameters())));
                }
                break;
            case FETCH_MESSAGES:
                if(request.getParameters().isEmpty()){
                    ctx.writeAndFlush(new Response(TEXT_HTML, uiEndpoint.fetchMessages()));
                }else {
                    ctx.writeAndFlush(new Response(TEXT_HTML, dataService.fetchMessages(request.getParameters())));
                }
                break;
            default:
                throw new RuntimeException(INVALID_REQUEST);
        }
    }
}
