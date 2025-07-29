package com.susang.stellarVision.common.jwt.error;

import org.springframework.http.HttpStatus;

public abstract class RefreshTokenError extends RuntimeException {
  private final String code;
  private final HttpStatus status;

  protected RefreshTokenError(String code, String message, HttpStatus status) {
    super(message);
    this.code = code;
    this.status = status;
  }
  public String getCode() { return code; }
  public HttpStatus getStatus() { return status; }


  public static class NotFound extends RefreshTokenError {
    public NotFound() {
      super("AUTH_REFRESH_TOKEN_NOT_FOUND",
              "Refresh token not found",
              HttpStatus.UNAUTHORIZED);
    }
  }
  public static class Mismatch extends RefreshTokenError {
    public Mismatch() {
      super("AUTH_REFRESH_TOKEN_MISMATCH",
              "Refresh token mismatch",
              HttpStatus.UNAUTHORIZED);
    }
  }
  public static class Missing extends RefreshTokenError {
    public Missing() {
      super("AUTH_REFRESH_TOKEN_MISSING",
              "Refresh token is required",
              HttpStatus.BAD_REQUEST);
    }
  }
  public static class ClaimMissing extends RefreshTokenError {
    public ClaimMissing() {
      super("AUTH_REFRESH_TOKEN_CLAIM_MISSING",
              "Email claim missing in token",
              HttpStatus.UNAUTHORIZED);
    }
  }

  public static class Invalid  extends RefreshTokenError {
    public Invalid () {
      super("AUTH_REFRESH_TOKEN_CLAIM_MISSING",
              "Email claim missing in token",
              HttpStatus.UNAUTHORIZED);
    }
  }
}
