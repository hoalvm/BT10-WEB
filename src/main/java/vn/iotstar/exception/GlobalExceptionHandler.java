package vn.iotstar.exception;

import com.nimbusds.jose.JOSEException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.ParseException;

/**
 * Global exception handler cho JWT REST API (Bai 4).
 * Bat cac exception lien quan den xac thuc va JWT, tra ve ProblemDetail (RFC 7807).
 * Tuong tu slide Trang 30.
 *
 * Bang loi:
 * - BadCredentialsException          → 401 (dang nhap sai)
 * - AccountStatusException           → 403 (tai khoan bi khoa)
 * - AccessDeniedException            → 403 (khong co quyen)
 * - JOSEException (Nimbus)           → 401 (JWT signature khong hop le)
 * - ParseException (JWT parse)       → 401 (JWT khong dung dinh dang)
 * - RuntimeException "het han"/"sai" → 401
 * - Exception (fallback)             → 500
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, ex.getMessage());
        detail.setProperty("description", "Ten dang nhap hoac mat khau khong chinh xac");
        return detail;
    }

    @ExceptionHandler(AccountStatusException.class)
    public ProblemDetail handleAccountStatus(AccountStatusException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, ex.getMessage());
        detail.setProperty("description", "Tai khoan bi khoa hoac chua kich hoat");
        return detail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, ex.getMessage());
        detail.setProperty("description", "Ban khong co quyen truy cap tai nguyen nay");
        return detail;
    }

    /**
     * JOSEException: loi tu Nimbus khi verify JWT signature that bai.
     * Tuong duong SignatureException trong JJWT.
     */
    @ExceptionHandler(JOSEException.class)
    public ProblemDetail handleJOSEException(JOSEException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, ex.getMessage());
        detail.setProperty("description", "JWT signature khong hop le");
        return detail;
    }

    /**
     * ParseException: JWT khong dung dinh dang compact (3 doan).
     */
    @ExceptionHandler(ParseException.class)
    public ProblemDetail handleParseException(ParseException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, ex.getMessage());
        detail.setProperty("description", "JWT khong hop le hoac sai dinh dang");
        return detail;
    }

    /**
     * RuntimeException tu JwtService (het han, sai chu ky, v.v.)
     */
    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("het han") || msg.contains("expired") ||
            msg.contains("signature") || msg.contains("jwt") ||
            msg.contains("token") || msg.contains("parse")) {
            ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED, ex.getMessage());
            detail.setProperty("description", "JWT khong hop le hoac da het han");
            return detail;
        }
        // Fallback cho RuntimeException khac
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        detail.setProperty("description", "Loi he thong noi bo");
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        detail.setProperty("description", "Loi he thong khong xac dinh");
        return detail;
    }
}
