@echo off
echo ============================================
echo  Iniciando Ambiente de Desenvolvimento
echo  MailHog (SMTP Mock)
echo  Microservico: ms-notification
echo ============================================
echo.

docker-compose up -d mailhog

echo.
echo   Servicos iniciados:
echo    - MailHog UI:   http://localhost:8025
echo    - MailHog SMTP: localhost:1025
echo.
echo  Execute a aplicacao no IntelliJ com as seguintes variaveis de ambiente:
echo    AWS_SQS_ENABLED=true
echo    AWS_ENDPOINT_OVERRIDE=http://localhost:4566
echo    MAIL_HOST=localhost
echo    MAIL_PORT=1025
echo    SPRING_PROFILES_ACTIVE=docker
echo.
echo  Porta local da API: 8087
echo  http://localhost:8087/swagger-ui.html
echo.
