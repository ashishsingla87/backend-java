package handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import model.Request;
import model.Response;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;

public class CodecHandler extends ChannelDuplexHandler {
    private static final String QUERY_DELIMITER = "/?";
    private static final String UTF_8 = "UTF-8";
    private static final String INVALID_REQUEST = "An Invalid request was made, please request a valid uri";
    private static final String PARAM_JOINER = ",";
    private static final String TEXT_PLAIN = "text/plain";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        try {
            FullHttpRequest request = (FullHttpRequest) msg;
            QueryStringDecoder queryStringDecoder;
            String path;
            if (request.getMethod() == HttpMethod.GET) {
                queryStringDecoder = new QueryStringDecoder(request.getUri());
                path = queryStringDecoder.path();
            } else if (request.getMethod() == HttpMethod.POST) {
                path = request.getUri();
                String param = request.content().toString(Charset.forName(UTF_8));
                queryStringDecoder = new QueryStringDecoder(QUERY_DELIMITER + param);
            } else {
                throw new RuntimeException(INVALID_REQUEST);
            }
            Map<String, String> params = queryStringDecoder.parameters().entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> String.join(PARAM_JOINER, e.getValue())
            ));
            ctx.fireChannelRead(new Request(path, params));
        }catch (Throwable throwable){
            throw new RuntimeException(INVALID_REQUEST, throwable);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise){
        if(msg instanceof  Response) {
            final Response response = (Response) msg;
            final ByteBuf buffer = Unpooled.copiedBuffer(response.getResponse(), CharsetUtil.UTF_8);
            final HttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buffer);
            final HttpHeaders headers = httpResponse.headers();
            headers.add(HttpHeaders.Names.CONTENT_TYPE, response.getResponseType());
            headers.add(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes());
            ctx.write(httpResponse, promise);
        }else{
            final Throwable throwable = (Throwable) msg;
            final StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            final ByteBuf buffer = Unpooled.copiedBuffer(sw.toString(), CharsetUtil.UTF_8);
            final HttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buffer);
            final HttpHeaders headers = httpResponse.headers();
            headers.add(HttpHeaders.Names.CONTENT_TYPE, TEXT_PLAIN);
            headers.add(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes());
            ctx.write(httpResponse, promise);
        }
    }
}
