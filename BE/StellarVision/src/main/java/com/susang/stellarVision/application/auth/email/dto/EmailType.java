package com.susang.stellarVision.application.auth.email.dto;

public enum EmailType {
    SIGNUP("email/signup"),
    SIGNUP_CONFIRM("email/signup_confirm");

    private final String templateName;
    EmailType(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }

}
