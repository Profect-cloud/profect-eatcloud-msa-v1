package com.eatcloud.managerservice;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
	public static String getCurrentUsername() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null
			|| !auth.isAuthenticated()
			|| auth instanceof AnonymousAuthenticationToken) {
			return "SYSTEM";
		}

		return auth.getName();
	}
}