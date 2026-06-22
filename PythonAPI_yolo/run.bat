@echo off
cd /d %~dp0

echo ==========================================
echo YOLO FastAPI API
echo ==========================================
echo.

py -3.11 --version >nul 2>&1
if errorlevel 1 (
    echo Python 3.11 nao encontrado.
    echo Instale com: py install 3.11
    echo Depois rode este arquivo novamente.
    pause
    exit /b 1
)

if not exist venv (
    echo Criando ambiente virtual com Python 3.11...
    py -3.11 -m venv venv
)

call venv\Scripts\activate.bat

echo Atualizando pip...
python -m pip install --upgrade pip setuptools wheel

echo Instalando dependencias...
pip install -r requirements.txt

if errorlevel 1 (
    echo.
    echo Falha ao instalar dependencias.
    pause
    exit /b 1
)

echo.
echo Iniciando API em http://127.0.0.1:8001
echo Swagger: http://127.0.0.1:8001/docs
echo.
python -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8001

pause
