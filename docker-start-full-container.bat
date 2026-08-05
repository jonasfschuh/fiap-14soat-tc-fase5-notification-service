@echo off
echo ============================================
echo  Iniciando Ambiente Completo (Container)
echo  App + MailHog (SMTP Mock)
echo  Microservico: ms-notification
echo ============================================

docker-compose up --build -d

echo.
echo    Servicos iniciados:
echo    - API:         http://localhost:8087
echo    - Swagger:     http://localhost:8087/swagger-ui.html
echo    - Actuator:    http://localhost:8087/actuator
echo                   http://localhost:8087/actuator/health
echo    - MailHog UI:  http://localhost:8025
echo    - MailHog SMTP: localhost:1025
echo.
echo    OBS: Requer video-upload-service rodando (LocalStack + fiap-network)
echo.
