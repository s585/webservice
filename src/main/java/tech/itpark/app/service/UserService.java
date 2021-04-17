package tech.itpark.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.itpark.app.dto.*;
import tech.itpark.framework.crypto.PasswordHasher;
import tech.itpark.framework.crypto.TokenGenerator;
import tech.itpark.app.exception.PermissionDeniedException;
import tech.itpark.app.model.TokenAuth;
import tech.itpark.app.model.User;
import tech.itpark.app.repository.UserRepository;
import tech.itpark.framework.security.AuthProvider;
import tech.itpark.framework.security.Auth;

import java.util.List;
import java.util.Optional;
import java.util.Set;

// @Component -> @Controller, @Service, @Repository
@Service
@RequiredArgsConstructor
public class UserService implements AuthProvider {
  private final UserRepository repository;
  private final PasswordHasher passwordHasher;
  private final TokenGenerator tokenGenerator;

  public List<User> getAll(Auth auth) {
    // authorization
    if (!auth.hasRole("ROLE_ADMIN")) {
      // InsufficientPrivilegesException
      throw new PermissionDeniedException();
    }

    return repository.getAll();
  }

  public Optional<User> getById() {
    return Optional.empty();
  }

  public User save(User user) {
    return user;
  }

  public void deleteById(long id) {
  }

  public RegistrationResponseDto register(RegistrationRequestDto request) {
    // --1. Свободен ли логин
    // 2. Длина пароля и т.д. и т.п.
    // 3. Хеш пароля
    // -> 4. Сохранение в БД
    // TODO: Чистота данных
    // TODO: " admin ", ADMIN, aDmIN
    // TODO: sanitizing (очистка данных) <- bad idea
    // TODO: pattern matching (whitelist/allowlist)
    // TODO: abcdef...0-9 (best practice)
    // Regexp:
    // TODO: https://regex101.com/
    // TODO: ^ смотрим с начала строки
    // TODO: $ смотрим до конца строки
    // TODO: ^admin$
    // TODO: [abc...zA...Z0...9]
    // TODO: [a-zA-Z0-9]
    // TODO: квантификаторы:
    // TODO: ? - 0-1 символ
    // TODO: * - 0+ символ
    // TODO: + - 1+ символ
    // TODO: {min}, {min, max}
    if (request.getLogin() == null) {
      throw new RuntimeException("login can't be null");
    }

    if (!request.getLogin().matches("^[a-z0-9]{5,10}$")) {
      throw new RuntimeException("bad login");
//      throw new BadLoginException();
    }

    if (request.getPassword() == null) {
      throw new RuntimeException("password can't be null");
    }

    if (request.getPassword().length() < 8) {
      throw new RuntimeException("minimal length of password must be greater than 8");
    }

    // hash - ???
    // TODO: 1. солить и хешировать +
    // TODO: 2. blacklist простых паролей

    final var hash = passwordHasher.hash(request.getPassword());

    // register
    final var saved = repository.save(
        new User(0, request.getLogin(), hash, request.getSecret(), Set.of("ROLE_USER"))
    );
    return new RegistrationResponseDto(
        saved.getId()
    );
  }

  public LoginResponseDto login(LoginRequestDto request) {
    final var user = repository.getByLogin(request.getLogin())
        .orElseThrow(() -> new RuntimeException("user with such login didn't found"));

    if (!passwordHasher.matches(user.getHash(), request.getPassword())) {
      throw new RuntimeException("passwords not match");
    }

    final var token = tokenGenerator.generate();
    // UserService.login
    repository.save(new TokenAuth(user.getId(), token));
    return new LoginResponseDto(token);
  }

  @Override
  public Auth provide(String token) {
    // BL -> Optional.empty -> Anonymous
    return repository.getByToken(token)
        .map(o -> (Auth) o)
        .orElse(Auth.anonymous())
    ;
  }

  public PasswordResetResponseDto resetPassword(PasswordResetRequestDto requestDto) {
    User user = repository.getByLogin(requestDto.getLogin())
            .orElseThrow(() -> new RuntimeException("user with such login didn't found"));
    if (!requestDto.getSecret().equals(user.getSecret())) {
      throw new PermissionDeniedException("you shall not pass");
    }
    final var hash = passwordHasher.hash(requestDto.getPassword());
    user.setHash(hash);
    repository.updatePassword(user);
    return new PasswordResetResponseDto(user.getId());
  }
}
