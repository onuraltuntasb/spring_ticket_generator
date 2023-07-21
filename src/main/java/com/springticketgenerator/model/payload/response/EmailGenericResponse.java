package com.springticketgenerator.model.payload.response;

public class EmailGenericResponse {
    private String message;
    private String error;

    public EmailGenericResponse(String message) {
        super();
        this.message = message;
    }

    public EmailGenericResponse(String message, String error) {
        super();
        this.message = message;
        this.error = error;
    }
}
