package tech.itpark.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.itpark.framework.security.Auth;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User implements Auth {
  private long id;
  private String login;
  private String hash; // FIXME: желательно не таскать соль + хеш пароля (по всему приложению)
  private String secret;
  private Set<String> roles;
}
