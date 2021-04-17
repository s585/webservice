package tech.itpark.framework.security;

@FunctionalInterface
public interface AuthProvider {
  Auth provide(String token);
}
