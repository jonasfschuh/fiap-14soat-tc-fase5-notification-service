@echo off
echo ============================================
echo  Executando Testes com Cobertura JaCoCo
echo  Microservico: ms-notification
echo ============================================
echo.

call mvnw.cmd clean verify

echo.
echo  Relatorio de cobertura:
echo  report-aggregate\target\site\jacoco-aggregate\index.html
echo.
