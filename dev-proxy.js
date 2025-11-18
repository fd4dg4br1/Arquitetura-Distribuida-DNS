const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");
const path = require("path");

const app = express();
const FRONT_DIR = path.join(__dirname, "frontend"); // pasta com index.html/app.js/style.css

// Backend target: pode ser configurado via env BACKEND_URL (útil ao rodar em container)
const BACKEND_URL =
  process.env.BACKEND_URL || "http://localhost:8080";

// Serve arquivos estáticos do frontend
app.use(express.static(FRONT_DIR));

// Proxy /api para backend
app.use(
  "/api",
  createProxyMiddleware({
    target: BACKEND_URL,
    changeOrigin: true,
    secure: false,
    // Reescreve domínio do cookie retornado pelo backend para o host do proxy (localhost)
    cookieDomainRewrite: "",
    onProxyReq: (proxyReq, req, res) => {
      // opcional: ajustar cabeçalhos se necessário
    },
    onError: (err, req, res) => {
      console.error("Proxy error:", err.message);
      res.status(502).send("Bad gateway (proxy)");
    },
  })
);

// Fallback para SPA (se quiser navegar com history API)
app.get("*", (req, res) => {
  res.sendFile(path.join(FRONT_DIR, "index.html"));
});

const PORT = process.env.PORT || 5500;
app.listen(PORT, () =>
  console.log(`Dev server + proxy rodando em http://localhost:${PORT}/frontend`)
);
