package by.bsuir.authservice.config;

import by.bsuir.authservice.service.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			String authHeader = request.getHeader("Authorization");
			String userEmail = request.getHeader("X-User-Email");
			String userRole = request.getHeader("X-User-Role");

			// First, try to authenticate via X-User-* headers (from API Gateway)
			if (userEmail != null && !userEmail.isEmpty() && userRole != null && !userRole.isEmpty()) {
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						userEmail,
						null,
						Collections.singleton(new SimpleGrantedAuthority("ROLE_" + userRole))
				);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
			// Otherwise, try JWT token authentication
			else if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);

				if (jwtProvider.validateToken(token)) {
					String email = jwtProvider.getEmailFromToken(token);
					String role = jwtProvider.getRoleFromToken(token);

					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
							email,
							null,
							Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role))
					);

					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			}
		} catch (Exception e) {
		}

		filterChain.doFilter(request, response);
	}
}
