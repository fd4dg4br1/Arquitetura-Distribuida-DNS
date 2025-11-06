package com.iesb.redes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.iesb.redes.model.Usuario;
import com.iesb.redes.repository.UsuarioRepository;
import com.iesb.redes.service.AutenticacaoService;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AutenticacaoService autenticacaoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- Iniciando verificação de usuários iniciais... ---");

        if (usuarioRepository.findByUsername("usuario1").isEmpty()) {
            System.out.println("Criando usuário 'usuario1'...");
            Usuario user1 = new Usuario();
            user1.setNomeUsuario("Usuário de Teste Um");
            user1.setUsername("usuario1");
            
            autenticacaoService.criarUsuario(user1, "12345");
            System.out.println("Usuário 'usuario1' criado.");
        }

        if (usuarioRepository.findByUsername("usuario2").isEmpty()) {
            System.out.println("Criando usuário 'usuario2'...");
            Usuario user2 = new Usuario();
            user2.setNomeUsuario("Usuário de Teste Dois");
            user2.setUsername("usuario2");
            
            autenticacaoService.criarUsuario(user2, "abcde");
            System.out.println("Usuário 'usuario2' criado.");
        }

        System.out.println("--- Verificação de usuários terminada. Aplicação pronta! ---");
    }
}