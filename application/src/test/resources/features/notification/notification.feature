Feature: Video Notification Service

  Scenario: Receive VIDEO_FAILED event and send failure email
    Given a video-events message with type "VIDEO_FAILED" and videoId "550e8400-e29b-41d4-a716-446655440000"
    When the notification service processes the event
    Then a failure notification email should be sent to the user

  Scenario: Receive VIDEO_PROCESSED event with notify enabled
    Given a video-events message with type "VIDEO_PROCESSED" and videoId "660e8400-e29b-41d4-a716-446655440000"
    And notification for processed videos is enabled
    When the notification service processes the event
    Then a success notification email should be sent to the user

  Scenario: Login endpoint returns SERVICE_UNAVAILABLE when auth lambda not configured
    When a POST request is made to /auth/login with valid credentials
    Then the response status should be 503
