package com.eatcloud.authservice.jwt;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.eatcloud.authservice.userDetails.CustomUserDetailsService;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService customUserDetailsService;

	public JwtAuthorizationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.customUserDetailsService = customUserDetailsService;
	}

	// 로그인, 회원가입, OAuth 콜백 등은JWT 인증 없이 접근 가능해야 한다.
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		// 인증/가입/토큰교환 등 auth 전용 엔드포인트는 JWT 필터 제외
		if (path.startsWith("/api/v1/auth/")) {
			return true;
		}
		// OAuth2 콜백 등 추가 예외
		if (path.startsWith("/oauth2/") || path.startsWith("/auth/success")) {
			return true;
		}
		return false;
	}

	// 요청마다 JWT 토큰을 꺼내서 검증하고, 인증 객체(SecurityContext)에 등록함.
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		// 1) 토큰필요없는 공용 API는 JWT 필터를 건너뛴다.-> 필요시 추가

		String path = request.getServletPath();
		if (path.startsWith("/api/v1/unauth/**")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = jwtTokenProvider.resolveToken(request);

		if (token != null) {
			try {
				if (!jwtTokenProvider.validateToken(token)) {
					UUID userId = jwtTokenProvider.getIdFromToken(token);
					String userType = jwtTokenProvider.getTypeFromToken(token);
					String newToken = jwtTokenProvider.createToken(userId, userType);
					response.setHeader("Authorization", "Bearer " + newToken);
					token = newToken;
				}

				UUID userId = jwtTokenProvider.getIdFromToken(token);
				String userType = jwtTokenProvider.getTypeFromToken(token);

				UserDetails userDetails = customUserDetailsService.loadUserByIdAndType(userId, userType);

				UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (ExpiredJwtException e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("{\"message\": \"세션이 만료되었습니다. 다시 로그인하세요.\"}");
				return;
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		filterChain.doFilter(request, response);
	}

}

