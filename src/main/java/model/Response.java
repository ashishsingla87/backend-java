package model;

public class Response {
    private final String responseType;
    private final String response;

    public String getResponseType() {
        return responseType;
    }

    public String getResponse() {
        return response;
    }

    public Response(String responseType, String response) {
        this.responseType = responseType;
        this.response = response;
    }
}
