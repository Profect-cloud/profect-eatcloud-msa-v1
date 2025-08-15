package com.eatcloud.authservice.login.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eatcloud.authservice.login.dto.LoginResponseDto;
import com.eatcloud.authservice.login.dto.SignupRedisData;
import com.eatcloud.authservice.login.dto.SignupRequestDto;
import com.eatcloud.authservice.security.jwt.JwtTokenProvider;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, Object> redisTemplate;
	private final MailService mailService;
	private final RefreshTokenService refreshTokenService;
	private final RestTemplate restTemplate;

	public AuthService(PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate, MailService mailService, RestTemplate restTemplate, RefreshTokenService refreshTokenService) {
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.redisTemplate = redisTemplate;
		this.mailService = mailService;
		this.restTemplate = restTemplate;
		this.refreshTokenService = refreshTokenService;
	}

	// 1) 로그인
	public LoginResponseDto login(String email, String password) {
		// 1) Admin 조회
		Object admin = getUserByEmail("admin", email);
		if (admin != null) {
			String encodedPassword = getPasswordFromUser(admin);
			if (!passwordEncoder.matches(password, encodedPassword)) {
				throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
			}
			UUID id = getIdFromUser(admin);
			String accessToken = jwtTokenProvider.createToken(id, "admin");
			return new LoginResponseDto(accessToken, null, "admin");
		}

		// 2) Manager 조회
		Object manager = getUserByEmail("manager", email);
		if (manager != null) {
			String encodedPassword = getPasswordFromUser(manager);
			if (!passwordEncoder.matches(password, encodedPassword)) {
				throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
			}
			UUID id = getIdFromUser(manager);
			String accessToken = jwtTokenProvider.createToken(id, "manager");
			String refreshToken = jwtTokenProvider.createRefreshToken(id, "manager");
			LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
			refreshTokenService.saveOrUpdateToken("manager", id, refreshToken, expiryDate);
			return new LoginResponseDto(accessToken, refreshToken, "manager");
		}

		// 3) Customer 조회
		Object customer = getUserByEmail("customer", email);
		if (customer == null) {
			throw new UsernameNotFoundException("존재하지 않는 사용자입니다: " + email);
		}
		String encodedPassword = getPasswordFromUser(customer);
		if (!passwordEncoder.matches(password, encodedPassword)) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}
		UUID id = getIdFromUser(customer);
		String accessToken = jwtTokenProvider.createToken(id, "customer");
		String refreshToken = jwtTokenProvider.createRefreshToken(id, "customer");
		LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
		refreshTokenService.saveOrUpdateToken("customer", id, refreshToken, expiryDate);
		return new LoginResponseDto(accessToken, refreshToken, "customer");
	}

	private Object getUserByEmail(String role, String email) {
		String url = "gatewayUrl + /api/" + role + "s/search?email=" + email;
		try {
			return restTemplate.getForObject(url, Object.class);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 유저 객체에서 UUID 추출
	 * 실제 구현 시 DTO 클래스를 만들어서 캐스팅 후 getter 사용
	 */
	private UUID getIdFromUser(Object user) {
		// TODO: DTO 매핑 로직 구현
		return UUID.fromString(((java.util.Map<?, ?>) user).get("id").toString());
	}

	/**
	 * 유저 객체에서 암호 추출
	 */
	private String getPasswordFromUser(Object user) {
		return ((java.util.Map<?, ?>) user).get("password").toString();
	}

	// 2) 회원가입 (Customer 예시)
	public void tempSignup(SignupRequestDto req) {
		if (getUserByEmail("customer", req.getEmail()) != null) {
			throw new RuntimeException("이미 존재하는 이메일입니다.");
		}

		String verificationCode = UUID.randomUUID().toString().substring(0, 6);

		String subject = "이메일 인증 코드";
		String text = "회원가입 인증 코드: " + verificationCode;

		try {
			mailService.sendMail(req.getEmail(), subject, text);
		} catch (MailException e) {
			throw new RuntimeException("이메일 전송에 실패했습니다. 다시 시도해주세요.", e);
		}

		SignupRedisData data = new SignupRedisData(req, verificationCode);
		redisTemplate.opsForValue().set("signup:" + req.getEmail(), data, 10, TimeUnit.MINUTES);
	}

	public void confirmEmail(String email, String code) {
		String key = "signup:" + email;
		SignupRedisData data = (SignupRedisData) redisTemplate.opsForValue().get(key);

		if (data == null) {
			throw new RuntimeException("만료되었거나 존재하지 않는 인증 요청입니다.");
		}
		if (!data.getCode().equals(code)) {
			throw new RuntimeException("인증 코드가 일치하지 않습니다.");
		}

		restTemplate.postForObject("http://customer-service/customers", data.getRequest(), Void.class);
		redisTemplate.delete(key);
	}

	public void signupWithoutEmailVerification(SignupRequestDto req) {
		if (getUserByEmail("customer", req.getEmail()) != null) {
			throw new RuntimeException("이미 존재하는 이메일입니다.");
		}
		// Gateway에 회원가입 요청
		restTemplate.postForObject("http://customer-service/customers", req, Void.class);
	}
}
