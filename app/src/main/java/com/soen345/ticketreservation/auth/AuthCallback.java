package com.soen345.ticketreservation.auth;

public interface AuthCallback {
    void onSuccess();
    void onFailure(String errorMessage);
}
