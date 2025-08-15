package com.eatcloud.authservice.security.userDetails;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.client.RestTemplate;

@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

	private final RestTemplate restTemplate;
	private final String gatewayUrl = "http://gateway"; // Gateway 주소

	public CustomUserDetailsService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/**
	 * UUID와 사용자 타입("admin", "manager", "customer")을 받아 UserDetails 반환
	 */
	public UserDetails loadUserByUsername(UUID id, String type) {
		Map<?, ?> user = getUserFromGateway(type, id);

		if (user == null) {
			throw new UsernameNotFoundException(type + "을 찾을 수 없습니다: " + id);
		}

		String password = user.get("password").toString();
		String role = switch (type.toLowerCase()) {
			case "admin" -> "ADMIN";
			case "manager" -> "MANAGER";
			case "customer" -> "CUSTOMER";
			default -> throw new IllegalArgumentException("알 수 없는 사용자 타입: " + type);
		};

		return User.builder()
				.username(id.toString())
				.password(password)
				.roles(role)
				.build();
	}

	/**
	 * Gateway를 통해 유저 정보 조회
	 */
	private Map<?, ?> getUserFromGateway(String type, UUID id) {
		String url = gatewayUrl + "/api/" + type + "s/" + id;
		try {
			return restTemplate.getForObject(url, Map.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		throw new UnsupportedOperationException("loadUserByUsername(UUID id, String type)을 사용하세요.");
	}
}
