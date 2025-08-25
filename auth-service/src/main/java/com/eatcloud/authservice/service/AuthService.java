package com.eatcloud.authservice.service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import com.eatcloud.authservice.dto.UserDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eatcloud.authservice.dto.LoginResponseDto;
import com.eatcloud.authservice.dto.SignupRedisData;
import com.eatcloud.authservice.dto.SignupRequestDto;
import com.eatcloud.authservice.jwt.JwtTokenProvider;
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
		UserDto user = getUserByEmail(email);
		if (user == null) {
			throw new UsernameNotFoundException("존재하지 않는 사용자입니다: " + email);
		}

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		String accessToken = jwtTokenProvider.createToken(user.getId(), user.getRole());
		String refreshToken = null;

		if (!user.getRole().equals("admin")) {
			refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRole());
			LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
			refreshTokenService.saveOrUpdateToken(user, refreshToken, expiryDate);
		}

		return new LoginResponseDto(accessToken, refreshToken, user.getRole());
	}

	private UserDto getUserByEmail(String email) {
		// 순서대로 조회: Admin → Manager → Customer
		try {
			UserDto admin = restTemplate.getForObject(
					"http://admin-service/api/v1/admin/search?email=" + email, UserDto.class
			);
			if (admin != null) {
				admin.setRole("admin");
				return admin;
			}
		} catch (Exception ignored) {}

		try {
			UserDto manager = restTemplate.getForObject(
					"http://manager-service/api/v1/manager/search?email=" + email, UserDto.class
			);
			if (manager != null) {
				manager.setRole("manager");
				return manager;
			}
		} catch (Exception ignored) {}

		try {
			UserDto customer = restTemplate.getForObject(
					"http://customer-service/api/v1/customers/search?email=" + email, UserDto.class
			);
			if (customer != null) {
				customer.setRole("customer");
				return customer;
			}
		} catch (Exception ignored) {}

		return null; // 어디에서도 찾지 못함
	}

	// 2) 회원가입 (Customer 예시)
	public void tempSignup(SignupRequestDto req) {
		if (getCustomerByEmail(req.getEmail()) != null) {
			throw new RuntimeException("이미 존재하는 이메일입니다.");
		}

		String verificationCode = java.util.UUID.randomUUID().toString().substring(0, 6);
		String subject = "이메일 인증 코드";
		String text = "회원가입 인증 코드: " + verificationCode;

		try {
			mailService.sendMail(req.getEmail(), subject, text);
		} catch (MailException e) {
			throw new RuntimeException("이메일 전송에 실패했습니다.", e);
		}

		String encodedPassword = passwordEncoder.encode(req.getPassword());
		SignupRequestDto encodedReq = SignupRequestDto.builder()
			.email(req.getEmail())
			.password(encodedPassword)
			.name(req.getName())
			.nickname(req.getNickname())
			.phone(req.getPhone())
			.points(req.getPoints()) // 포인트 추가
			.build();

		SignupRedisData data = new SignupRedisData(encodedReq, verificationCode);
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

		restTemplate.postForObject("http://customer-service/api/v1/customers/signup", data.getRequest(), Void.class);
		redisTemplate.delete(key);
	}

	public void signupWithoutEmailVerification(SignupRequestDto req) {
		if (getCustomerByEmail(req.getEmail()) != null) {
			throw new RuntimeException("이미 존재하는 이메일입니다.");
		}

		SignupRequestDto encodedReq = SignupRequestDto.builder()
			.email(req.getEmail())
			.password(passwordEncoder.encode(req.getPassword()))
			.name(req.getName())
			.nickname(req.getNickname())
			.phone(req.getPhone())
			.points(req.getPoints()) // 포인트 추가
			.build();

		// Gateway에 회원가입 요청
		restTemplate.postForObject("http://customer-service/api/v1/customers/signup", encodedReq, Void.class);
	}

	private UserDto getCustomerByEmail(String email) {
		try {
			UserDto customer = restTemplate.getForObject(
					"http://customer-service/api/v1/customers/search?email=" + email,
					UserDto.class
			);
			if (customer != null) {
				customer.setRole("customer");
				return customer;
			}
		} catch (Exception ignored) {}
		return null;
	}
}
