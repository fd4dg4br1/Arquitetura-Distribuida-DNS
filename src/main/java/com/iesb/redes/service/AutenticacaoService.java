package com.iesb.redes.service;

import java.net.InetAddress;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iesb.redes.dto.LoginRequestDTO;
import com.iesb.redes.dto.PerfilResponseDTO;
import com.iesb.redes.model.Sessao;
import com.iesb.redes.model.Usuario;
import com.iesb.redes.repository.SessaoRepository;
import com.iesb.redes.repository.UsuarioRepository;

@Service
public class AutenticacaoService {

    @Autowired
    SessaoRepository sessaoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    public String login(String username, String senha) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (passwordEncoder.matches(senha, usuario.getSenhaCriptografada())) {
            // Senha correta!
            Sessao novaSessao = new Sessao(usuario);
            // Salva a sessão no banco centralizado
            sessaoRepository.save(novaSessao);
            // Retorna o ID da sessão para o Controller
            return novaSessao.getIdSessao();
        } else {
            throw new RuntimeException("Senha incorreta");
        }
    }

    
    public void criarUsuario(Usuario usuario, String senhaPura) {
        String hash = passwordEncoder.encode(senhaPura);
        usuario.setSenhaCriptografada(hash);
        usuarioRepository.save(usuario);
    }



    // Valida uma sessão e retorna os dados do perfil.
    public PerfilResponseDTO getPerfilPorSessao(String idSessao) {
        // 1. Busca a sessão no banco
        Sessao sessao = sessaoRepository.findById(idSessao)
                .orElseThrow(() -> new RuntimeException("Sessão não encontrada. Faça o login."));

        // 2. Verifica se a sessão expirou
        if (sessao.getDataExpiracao().isBefore(LocalDateTime.now())) {
            sessaoRepository.delete(sessao); // Limpa do banco
            throw new RuntimeException("Sessão expirada. Faça o login.");
        }

        sessao.setDataExpiracao(LocalDateTime.now().plusMinutes(30));
        sessaoRepository.save(sessao);
        // 3. Se chegou aqui, a sessão é VÁLIDA.
        Usuario usuario = sessao.getUsuario();
        String hostname = getHostnameLocal(); // Pega o hostname do servidor atual

        // 4. Retorna o DTO de resposta
        return new PerfilResponseDTO(
            usuario.getNomeUsuario(),
            sessao.getIdSessao(),
            sessao.getDataLogin(),
            hostname
        );
    }
    
    public Usuario registrarNovoUsuario(LoginRequestDTO dto) {
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username '" + dto.getUsername() + "' já está em uso.");
        }
        
        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(dto.getUsername());
        
        novoUsuario.setNomeUsuario(dto.getNomeUsuario());

        this.criarUsuario(novoUsuario, dto.getPassword());
        return novoUsuario;
    }

    public void logout(String idSessao) {
        // Apenas encontra a sessão pelo ID e a deleta do banco.
        // Se não encontrar, não faz nada (o usuário já não estava logado).
        sessaoRepository.findById(idSessao).ifPresent(sessao -> {
            sessaoRepository.delete(sessao);
        });
    }
    
    //pega o hostname
    private String getHostnameLocal() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "Erro ao obter hostname";
        }
    }
}