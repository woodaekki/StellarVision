package com.susang.stellarVision.application.member.dto;

public enum AuthProvider {
    GOOGLE;

    public static AuthProvider from(String providerName) {
        if (providerName == null) {
            return null;
        }
        try {
            return AuthProvider.valueOf(providerName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown AuthProvider: " + providerName);
        }
    }
}
