const express = require('express');
const axios = require('axios');
const path = require('path');

const app = express();
const PORT = 3000;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));

// Rota principal - Tela de e-mail
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Envia o e-mail para o User Service para gerar o código
app.post('/send-code', async (req, res) => {
    const { email } = req.body;
    try {
        await axios.post('http://localhost:8081/auth/request-code', { email });
        res.redirect(`/verify?email=${encodeURIComponent(email)}`);
    } catch (error) {
        console.error('Erro ao solicitar código:', error.message);
        res.send(`
            <html>
                <head>
                    <title>Erro</title>
                    <style>
                        body { font-family: sans-serif; background: #0f172a; color: #f1f5f9; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
                        .card { background: #1e293b; padding: 2.5rem; border-radius: 12px; border: 1px solid #334155; text-align: center; max-width: 450px; }
                        h1 { color: #ef4444; }
                        p { color: #94a3b8; font-size: 1.1rem; }
                        a { display: inline-block; margin-top: 1.5rem; background: #6366f1; color: white; padding: 0.75rem 1.5rem; text-decoration: none; border-radius: 6px; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h1>Erro ao Solicitar Código</h1>
                        <p>Ocorreu uma falha ao enviar a solicitação. Verifique se o e-mail está no formato correto e se o serviço está online.</p>
                        <a href="/">Tentar Novamente</a>
                    </div>
                </body>
            </html>
        `);
    }
});

// Rota para a tela de verificação do código
app.get('/verify', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'verify.html'));
});

// Valida o e-mail e o código no User Service
app.post('/verify-code', async (req, res) => {
    const { email, code } = req.body;
    try {
        const response = await axios.post('http://localhost:8081/auth/verify-code', { email, code });
        const token = response.data.token;
        
        // Se receber o token, salva no sessionStorage no navegador do cliente e redireciona
        res.send(`
            <html>
                <body>
                    <script>
                        sessionStorage.setItem('token', '${token}');
                        window.location.href = '/dashboard';
                    </script>
                </body>
            </html>
        `);
    } catch (error) {
        console.error('Erro ao validar código:', error.message);
        res.send(`
            <html>
                <head>
                    <title>Erro de Validação</title>
                    <style>
                        body { font-family: sans-serif; background: #0f172a; color: #f1f5f9; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
                        .card { background: #1e293b; padding: 2.5rem; border-radius: 12px; border: 1px solid #334155; text-align: center; max-width: 450px; }
                        h1 { color: #ef4444; }
                        p { color: #94a3b8; font-size: 1.1rem; }
                        a { display: inline-block; margin-top: 1.5rem; background: #6366f1; color: white; padding: 0.75rem 1.5rem; text-decoration: none; border-radius: 6px; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h1>Código Inválido ou Expirado</h1>
                        <p>O código inserido é incorreto ou o tempo limite de 5 minutos expirou. Por favor, solicite um novo código.</p>
                        <a href="/verify?email=${encodeURIComponent(email)}">Tentar Novamente</a>
                    </div>
                </body>
            </html>
        `);
    }
});

// Tela de Dashboard (semana 4 placeholder)
app.get('/dashboard', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'dashboard.html'));
});

app.listen(PORT, () => {
    console.log(`Servidor frontend rodando em http://localhost:${PORT}`);
});
