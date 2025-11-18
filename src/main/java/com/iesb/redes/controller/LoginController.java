package com.iesb.redes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iesb.redes.dto.LoginRequestDTO;
import com.iesb.redes.dto.LoginCredentialsDTO;
import com.iesb.redes.dto.PerfilResponseDTO;
import com.iesb.redes.model.Usuario;
import com.iesb.redes.service.AutenticacaoService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/api")
public class LoginController {

    @Autowired
    AutenticacaoService autenticacaoService;

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody LoginCredentialsDTO loginRequest,
            HttpServletResponse response) {
        try {
            // 4. Chama o service
            String idSessao = autenticacaoService.login(
                    loginRequest.getUsername(),
                    loginRequest.getPassword());

            // 5. Cria o cookie manualmente
            Cookie sessionCookie = new Cookie("minha-sessao-id", idSessao);
            sessionCookie.setHttpOnly(true); // Segurança
            sessionCookie.setPath("/"); // Disponível em todo o site
            sessionCookie.setMaxAge(30 * 60); // Expira em 30 min
            // 6. Adiciona o cookie na resposta
            response.addCookie(sessionCookie);
            return ResponseEntity.ok("Login bem-sucedido!");

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage()); // 401 Unauthorized
        }
    }

    // funcao de tela depois do login bem sucedido
    @GetMapping(value = "/meu-perfil")
    public ResponseEntity<?> getPerfil(
            @CookieValue(name = "minha-sessao-id", required = false) String idSessao) {

        if (idSessao == null) {
            return ResponseEntity.status(401).body("Acesso negado. Faça o login.");
        }
        try {
            // 8. Chama o service para validar a sessão e pegar os dados
            PerfilResponseDTO perfil = autenticacaoService.getPerfilPorSessao(idSessao);
            return ResponseEntity.ok(perfil);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(@RequestBody LoginRequestDTO request) {
        try {
            Usuario novoUsuario = autenticacaoService.registrarNovoUsuario(request);

            // Retorna uma resposta 201 (Created)
            return ResponseEntity.status(201)
                    .body("Usuário '" + novoUsuario.getUsername() + "' criado com sucesso!");

        } catch (RuntimeException e) {
            // Retorna 400 (Bad Request) se o username já existir
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(name = "minha-sessao-id", required = false) String idSessao,
            HttpServletResponse response) {

        if (idSessao != null) {
            // 1. Manda o Service destruir a sessão no banco
            autenticacaoService.logout(idSessao);
        }

        // 2. Manda o navegador "esquecer" o cookie
        // (Criando um cookie com o mesmo nome, mas que expira imediatamente)
        Cookie cookie = new Cookie("minha-sessao-id", null);
        cookie.setMaxAge(0); // Expira agora
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok("Deslogado com sucesso.");
    }

}