package pardalis;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Singleton;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.eclipse.jetty.servlet.DefaultServlet;
import pardalis.controller.AccountController;
import pardalis.controller.AccountControllerImpl;
import pardalis.service.AccountService;
import pardalis.service.AccountServiceImpl;
import pardalis.validator.RequestValidator;
import pardalis.validator.RequestValidatorImpl;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

public class AppModule extends JerseyServletModule {
    private final String persistenceUnitName;

    AppModule(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    @Override
    public void configureServlets() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);

        bind(DefaultServlet.class).in(Singleton.class);
        bind(RequestValidator.class).to(RequestValidatorImpl.class).in(Singleton.class);
        bind(AccountController.class).to(AccountControllerImpl.class).in(Singleton.class);
        bind(AccountService.class).to(AccountServiceImpl.class).in(Singleton.class);
        bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
        bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
        bind(EntityManagerFactory.class).toInstance(entityManagerFactory);

        serve("/*").with(GuiceContainer.class);
    }
}