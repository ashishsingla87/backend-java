package model;

import java.util.Map;

public class Request {
    private final String requestPath;
    private final Map<String, String> parameters;

    public String getRequestPath() {
        return requestPath;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Request(String requestPath, Map<String, String> parameters) {
        this.requestPath = requestPath;
        this.parameters = parameters;
    }
}
