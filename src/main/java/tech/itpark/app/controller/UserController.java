package tech.itpark.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import tech.itpark.app.dto.PasswordResetRequestDto;
import tech.itpark.framework.bodyconverter.BodyConverter;
import tech.itpark.app.dto.LoginRequestDto;
import tech.itpark.app.dto.RegistrationRequestDto;
import tech.itpark.framework.http.ContentTypes;
import tech.itpark.app.model.User;
import tech.itpark.framework.security.HttpServletRequestAuth;
import tech.itpark.app.service.UserService;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
  private final UserService service;
  private final List<BodyConverter> converters;

  // users -> ADMIN
  public void getAll(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // можем доставать auth только из request <- ThreadLocal
    // composable software
    final var auth = HttpServletRequestAuth.auth(request);
    final var data = service.getAll(auth);
    write(data, "application/json", response);
  }
  public void getById(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.getWriter().write("getById");
  }
  public void save(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // 1. Подготовка данных для сервиса
    // 2. Вызов сервиса
    // 3. Обработка ответа
    final var dto = read(User.class, request);
    final var saved = service.save(dto);
    write(saved, "application/json", response);
  }
  public void deleteById(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.getWriter().write("deleteById");
  }

  public void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final var requestDto = read(RegistrationRequestDto.class, request);
    final var responseDto = service.register(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  public void passwordReset(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final var requestDto = read(PasswordResetRequestDto.class, request);
    final var responseDto = service.resetPassword(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final var requestDto = read(LoginRequestDto.class, request);
    final var responseDto = service.login(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  public <T> T read(Class<T> clazz, HttpServletRequest request) {
    for (final var converter : converters) {
      if (!converter.canRead(request.getContentType(), clazz)) {
        continue;
      }

      try {
        return converter.read(request.getReader(), clazz);
      } catch (IOException e) {
        e.printStackTrace();
        // TODO: convert to special exception
        throw new RuntimeException(e);
      }
    }
    // TODO: convert to special exception
    throw new RuntimeException("no converters support given content type");
  }

  public void write(Object data, String contentType, HttpServletResponse response) {
    for (final var converter : converters) {
      if (!converter.canWrite(contentType, data.getClass())) {
        continue;
      }

      try {
        response.setContentType(contentType);
        converter.write(response.getWriter(), data);
        return;
      } catch (IOException e) {
        e.printStackTrace();
        // TODO: convert to special exception
        throw new RuntimeException(e);
      }
    }
    // TODO: convert to special exception
    throw new RuntimeException("no converters support given content type");
  }
}
