package tech.itpark.app.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tech.itpark.app.exception.DataAccessException;
import tech.itpark.framework.jdbc.JdbcTemplate;
import tech.itpark.app.model.TokenAuth;
import tech.itpark.app.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserRepository {
  // вместо Connection работает с DataSource, который может выдавать Connection по запросу
  private final DataSource ds;
  private final JdbcTemplate template = new JdbcTemplate();

  public List<User> getAll() {
    return Collections.emptyList();
  }

  public User save(User user) {
    try (
        final var connection = ds.getConnection();
        // Java 15+ - Text Blocks (многострочные строки)
        final var statement = connection.prepareStatement("""
                INSERT INTO users(login, password, secret)
                VALUES (?, ?, ?);
            """, Statement.RETURN_GENERATED_KEYS); // альтернатива RETURNING
    ) {
      // look where you jump
      // try then sorry
      var index = 0;
      statement.setString(++index, user.getLogin());
      statement.setString(++index, user.getHash());
      statement.setString(++index, user.getSecret());
      statement.executeUpdate();

      final var keys = statement.getGeneratedKeys();
      if (!keys.next()) {
        throw new DataAccessException("no keys in result");
      }

      // проставили сгенерированный id и вернули обратно
      // но, по-хорошему, надо делать копию
      user.setId(keys.getLong(1));
      return user;
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  public void updatePassword(User user) {
    try (
            final var connection = ds.getConnection();
            // Java 15+ - Text Blocks (многострочные строки)
            final var statement = connection.prepareStatement(
                "UPDATE users SET password = ? WHERE login = ?"
            )
    ) {
      var index = 0;
      statement.setString(++index, user.getHash());
      statement.setString(++index, user.getLogin());
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  public Optional<User> getByLogin(String login) {
    try (
        final var connection = ds.getConnection();
        final var statement = connection.prepareStatement("""
              SELECT id, login, password, secret, roles FROM users WHERE login = ?
            """);
    ) {
      var index = 0;
      statement.setString(++index, login);
      try (
          final var resultSet = statement.executeQuery();
      ) {
        return resultSet.next() ? Optional.of(
            new User(
                resultSet.getLong("id"),
                resultSet.getString("login"),
                resultSet.getString("password"),
                resultSet.getString("secret"),
                Set.of((String[])resultSet.getArray("roles").getArray())
            )
        ) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  public void save(TokenAuth auth) {
    try (
        final var conn = ds.getConnection();
    ) {
      // language=PostgreSQL
      template.update(conn, """
          INSERT INTO tokens(userId, token) VALUES (?, ?);
          """, auth.getUserId(), auth.getToken());
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  public Optional<User> getByToken(String token) {
    // copied from getByLogin
    try (
        final var connection = ds.getConnection();
        final var statement = connection.prepareStatement("""
              SELECT u.id, u.login, '**MASKED**' AS password, '**MASKED**' AS secret u.roles FROM users u
              JOIN tokens t ON u.id = t.userid
              WHERE t.token = ?
            """);
    ) {
      var index = 0;
      statement.setString(++index, token);
      try (
          final var resultSet = statement.executeQuery();
      ) {
        return resultSet.next() ? Optional.of(
            new User(
                resultSet.getLong("id"),
                resultSet.getString("login"),
                resultSet.getString("password"),
                resultSet.getString("secret"),
                Set.of((String[])resultSet.getArray("roles").getArray())
            )
        ) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }
}
