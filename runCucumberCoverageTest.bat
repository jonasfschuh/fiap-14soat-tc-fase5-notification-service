@echo off
echo ============================================
echo  Executando Testes BDD (Cucumber)
echo  Microservico: ms-notification
echo ============================================
echo.

call mvnw.cmd test -pl application

echo.
echo  Relatorio Cucumber:
echo  application\target\cucumber-reports\report.html
echo.
