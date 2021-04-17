package tech.itpark.framework.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import tech.itpark.framework.security.AuthProvider;

import java.io.IOException;

public class AuthFilter extends HttpFilter {
  private AuthProvider provider;
  @Override
  public void init() throws ServletException {
    final var context = (ApplicationContext) getServletContext().getAttribute("CONTEXT");
    provider = context.getBean(AuthProvider.class);
  }

  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
    final var token = req.getHeader("Authorization");
    final var auth = provider.provide(token);
    req.setAttribute("AUTH", auth);
    chain.doFilter(req, res);
  }
}
