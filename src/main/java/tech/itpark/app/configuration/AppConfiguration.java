package tech.itpark.app.configuration;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.itpark.app.controller.UserController;
import tech.itpark.framework.bodyconverter.BodyConverter;
import tech.itpark.framework.bodyconverter.GsonBodyConverter;
import tech.itpark.framework.crypto.PasswordHasher;
import tech.itpark.framework.crypto.PasswordHasherDefaultImpl;
import tech.itpark.framework.crypto.TokenGenerator;
import tech.itpark.framework.crypto.TokenGeneratorDefaultImpl;
import tech.itpark.framework.http.Handler;
import tech.itpark.framework.http.Methods;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Configuration
public class AppConfiguration {
    @Bean
    public DataSource dataSource() throws NamingException {
        // Spring way:
        // return (DataSource) new JndiTemplate().lookup("java:/comp/env/jdbc/db");
        final var cxt = new InitialContext();
        return (DataSource) cxt.lookup("java:/comp/env/jdbc/db");
    }

    @Bean
    public PasswordHasher passwordHasher(MessageDigest digest) {
        return new PasswordHasherDefaultImpl(digest);
    }

    @Bean
    public MessageDigest messageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256");
    }

    @Bean
    public TokenGenerator tokenGenerator() {
        return new TokenGeneratorDefaultImpl();
    }

    @Bean
    public List<BodyConverter> bodyConverters() {
        return List.of(
                new GsonBodyConverter(new Gson())
        );
    }

    @Bean
    public Map<String, Map<String, Handler>> routes(UserController controller) {
        return Map.of(
                "/api/auth/registration", Map.of(
                        Methods.POST, controller::register
                ),
                "/api/auth/login", Map.of(
                        Methods.POST, controller::login
                ),
                "/api/auth/reset-password", Map.of(
                        Methods.POST, controller::passwordReset
                ),
                "/api/users/all", Map.of(
                        Methods.GET, controller::getAll
                )
        );
    }
}
