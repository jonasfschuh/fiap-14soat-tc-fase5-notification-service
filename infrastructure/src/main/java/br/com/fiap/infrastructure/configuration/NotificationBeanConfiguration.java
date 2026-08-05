package br.com.fiap.infrastructure.configuration;

import br.com.fiap.domain.ports.in.SendNotificationInputPort;
import br.com.fiap.domain.ports.out.NotificationEmailPort;
import br.com.fiap.domain.usecases.SendNotificationUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationBeanConfiguration {

    @Value("${app.notification.notify-on-processed:false}")
    private boolean notifyOnProcessed;

    @Bean
    public SendNotificationInputPort sendNotificationInputPort(NotificationEmailPort emailPort) {
        return new SendNotificationUseCase(emailPort, notifyOnProcessed);
    }
}
