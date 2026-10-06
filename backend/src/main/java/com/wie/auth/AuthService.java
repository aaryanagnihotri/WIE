package com.wie.auth;
import com.wie.auth.AuthDtos.*;
import com.wie.exception.ApiException;
import com.wie.model.*;
import com.wie.repository.*;
import com.wie.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthService {
  static final String COOKIE = "refresh_token";
  private static final Map<Role,List<String>> REQUIRED = Map.of(
    Role.STUDENT, List.of("college","degree","branch","graduationYear"),
    Role.RECRUITER, List.of("company","designation","industry","companySize"),
    Role.COURSE_AGENCY, List.of("contactPerson","specialization","deliveryMode","location"));
  private final UserRepository users; private final RefreshTokenRepository tokens;
  private final PasswordEncoder encoder; private final JwtService jwt;
  private final String refreshSecret; private final int refreshDays; private final boolean secureCookie;
  private final SecureRandom rnd = new SecureRandom();

  public AuthService(UserRepository users, RefreshTokenRepository tokens, PasswordEncoder encoder, JwtService jwt,
      @Value("${jwt.refresh-secret}") String refreshSecret, @Value("${jwt.refresh-days}") int refreshDays,
      @Value("${app.cookie-secure}") boolean secureCookie){
    this.users=users; this.tokens=tokens; this.encoder=encoder; this.jwt=jwt;
    this.refreshSecret=refreshSecret; this.refreshDays=refreshDays; this.secureCookie=secureCookie; }

  public AuthResponse register(RegisterRequest r, HttpServletResponse res){
    Map<String,Object> p = r.profile() == null ? Map.of() : r.profile();
    for (String k : REQUIRED.get(r.role()))
      if (p.get(k) == null || p.get(k).toString().isBlank())
        throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Missing profile field: " + k);
    String email = r.email().trim().toLowerCase();
    if (users.existsByEmail(email)) throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Email already registered");
    User u = users.save(new User(null, r.name().trim(), email, encoder.encode(r.password()), r.role(), false, p, Instant.now()));
    return issue(u, res);
  }

  public AuthResponse login(LoginRequest r, HttpServletResponse res){
    User u = users.findByEmail(r.email().trim().toLowerCase()).orElse(null);
    if (u == null || !encoder.matches(r.password(), u.passwordHash()))
      throw new ApiException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Invalid email or password");
    return issue(u, res);
  }

  /** Rotation: the presented token is consumed; a new one is issued. Unknown/expired -> 401. */
  public AuthResponse refresh(String raw, HttpServletResponse res){
    if (raw == null) throw expired();
    RefreshToken t = tokens.findByTokenHash(hash(raw)).orElseThrow(this::expired);
    tokens.delete(t);
    if (t.expiresAt().isBefore(Instant.now())) throw expired();
    User u = users.findById(t.userId()).orElseThrow(this::expired);
    return issue(u, res);
  }

  public void logout(String raw, HttpServletResponse res){
    if (raw != null) tokens.findByTokenHash(hash(raw)).ifPresent(tokens::delete);
    res.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
  }

  private ApiException expired(){ return new ApiException(HttpStatus.UNAUTHORIZED, "SESSION_EXPIRED", "Please log in again"); }

  private AuthResponse issue(User u, HttpServletResponse res){
    byte[] b = new byte[32]; rnd.nextBytes(b);
    String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    tokens.save(new RefreshToken(null, hash(raw), u.id(), Instant.now().plus(Duration.ofDays(refreshDays))));
    res.addHeader(HttpHeaders.SET_COOKIE, cookie(raw, Duration.ofDays(refreshDays)).toString());
    return new AuthResponse(jwt.generate(u.id()), UserDto.of(u));
  }

  private ResponseCookie cookie(String v, Duration age){
    return ResponseCookie.from(COOKIE, v).httpOnly(true).secure(secureCookie).sameSite("Strict").path("/api/auth").maxAge(age).build(); }

  private String hash(String raw){
    try { Mac m = Mac.getInstance("HmacSHA256");
      m.init(new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return Base64.getEncoder().encodeToString(m.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException e){ throw new IllegalStateException(e); } }
}
