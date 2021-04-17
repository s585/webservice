package tech.itpark.framework.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ContextLoadDestroyListener implements ServletContextListener {
  private ConfigurableApplicationContext context;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    final var servletContext = sce.getServletContext();
    final var basePackage = servletContext.getInitParameter("base-package");
    context = new AnnotationConfigApplicationContext(basePackage);
    servletContext.setAttribute("CONTEXT", context);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    context.close();
  }
}
