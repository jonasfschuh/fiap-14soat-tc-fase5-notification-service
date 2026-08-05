package br.com.fiap.application.bdd.steps;

import br.com.fiap.application.dtos.AuthLoginRequest;
import br.com.fiap.domain.model.NotificationResult;
import br.com.fiap.domain.model.VideoNotificationEvent;
import br.com.fiap.domain.ports.out.NotificationEmailPort;
import br.com.fiap.domain.usecases.SendNotificationUseCase;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class NotificationSteps {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private NotificationEmailPort notificationEmailPort;
    private VideoNotificationEvent event;
    private NotificationResult notificationResult;
    private boolean notifyOnProcessed;
    private ResponseEntity<Object> loginResponse;

    @Before
    public void setUp() {
        notificationEmailPort = mock(NotificationEmailPort.class);
        notifyOnProcessed = false;
        event = null;
        notificationResult = null;
        loginResponse = null;
    }

    @Given("a video-events message with type {string} and videoId {string}")
    public void aVideoEventsMessageWithTypeAndVideoId(String eventType, String videoId) {
        event = new VideoNotificationEvent(
                videoId,
                "user-1",
                "user@example.com",
                "sample-video.mp4",
                eventType,
                "Falha ao processar o vídeo",
                "2026-01-01T10:00:00Z"
        );
    }

    @And("notification for processed videos is enabled")
    public void notificationForProcessedVideosIsEnabled() {
        notifyOnProcessed = true;
    }

    @When("the notification service processes the event")
    public void theNotificationServiceProcessesTheEvent() {
        SendNotificationUseCase useCase = new SendNotificationUseCase(notificationEmailPort, notifyOnProcessed);
        notificationResult = useCase.send(event);
    }

    @Then("a failure notification email should be sent to the user")
    public void aFailureNotificationEmailShouldBeSentToTheUser() {
        verify(notificationEmailPort).sendFailureEmail(any(VideoNotificationEvent.class));
        verify(notificationEmailPort, never()).sendSuccessEmail(any(VideoNotificationEvent.class));
        assertThat(notificationResult.isSuccess()).isTrue();
    }

    @Then("a success notification email should be sent to the user")
    public void aSuccessNotificationEmailShouldBeSentToTheUser() {
        verify(notificationEmailPort).sendSuccessEmail(any(VideoNotificationEvent.class));
        verify(notificationEmailPort, never()).sendFailureEmail(any(VideoNotificationEvent.class));
        assertThat(notificationResult.isSuccess()).isTrue();
    }

    @When("a POST request is made to \\/auth\\/login with valid credentials")
    public void aPostRequestIsMadeToAuthLoginWithValidCredentials() {
        loginResponse = testRestTemplate.postForEntity("/auth/login",
                new AuthLoginRequest("admin", "admin123"), Object.class);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(Integer status) {
        assertThat(loginResponse.getStatusCode().value()).isEqualTo(status);
    }
}
