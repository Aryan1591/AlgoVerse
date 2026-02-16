package com.algoverse.platform.security_config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Collection;

public class CsrfCookieFilter extends OncePerRequestFilter {

  // avoid running on forwards to /error and similar
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/error") || uri.startsWith("/auth/") || uri.startsWith("/.well-known/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    // If response is already committed we should not try to modify headers
    if (response.isCommitted()) {
      filterChain.doFilter(request, response);
      return;
    }

    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    if (csrfToken != null) {
      String token = csrfToken.getToken();

      // If cookie exists and matches, do nothing
      Cookie existing = WebUtils.getCookie(request, "XSRF-TOKEN");
      if (existing == null || !token.equals(existing.getValue())) {

        // Optional: compute approximate total header length so we avoid pushing it over the limit
        int headerBytes = estimateHeaderBytes(response);
        // 8KB is default Tomcat header buffer — be conservative
        if (headerBytes < 7_000) {
          Cookie cookie = new Cookie("XSRF-TOKEN", token);
          cookie.setPath("/");
          cookie.setHttpOnly(false); // JS must read it
          // cookie.setSecure(true); // enable in prod over HTTPS
          response.addCookie(cookie);
        } else {
         logger.debug("Skipping setting XSRF cookie (headers already large: {} bytes) for {}");
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private int estimateHeaderBytes(HttpServletResponse response) {
    // This only sees headers already set on response (approximate).
    // It's a safe heuristic — not exact.
    int sum = 0;
    Collection<String> names = response.getHeaderNames();
    for (String name : names) {
      for (String v : response.getHeaders(name)) {
        sum += name.length() + (v == null ? 0 : v.length());
      }
    }
    return sum;
  }
}
