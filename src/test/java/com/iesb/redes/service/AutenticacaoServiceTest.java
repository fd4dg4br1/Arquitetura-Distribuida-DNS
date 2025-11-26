package com.iesb.redes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.iesb.redes.dto.LoginRequestDTO;
import com.iesb.redes.dto.PerfilResponseDTO;
import com.iesb.redes.model.Sessao;
import com.iesb.redes.model.Usuario;
import com.iesb.redes.repository.SessaoRepository;
import com.iesb.redes.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class) // Habilita o Mockito
public class AutenticacaoServiceTest {

    @InjectMocks // Injeta os mocks dentro do nosso Service real
    private AutenticacaoService autenticacaoService;

    @Mock // Cria um "Dublê" do repositório
    private UsuarioRepository usuarioRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void deveFazerLoginComSucesso() {
        // 1. CENÁRIO (Arrange)
        String username = "teste";
        String senhaPura = "123";
        String senhaHash = "$2a$10$fakehash...";
        
        Usuario usuarioMock = new Usuario();
        usuarioMock.setUsername(username);
        usuarioMock.setSenhaCriptografada(senhaHash);

        // Ensinamos o Mock a retornar o usuário quando buscarem por "teste"
        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuarioMock));
        // Ensinamos o Mock a dizer que a senha bate
        when(passwordEncoder.matches(senhaPura, senhaHash)).thenReturn(true);

        // 2. AÇÃO (Act)
        String idSessao = autenticacaoService.login(username, senhaPura);

        // 3. VERIFICAÇÃO (Assert)
        assertNotNull(idSessao); // O ID não pode ser nulo
        verify(sessaoRepository, times(1)).save(any(Sessao.class)); // Verifica se o save foi chamado
    }

    @Test
    void deveLancarErroQuandoSenhaEstiverErrada() {
        // 1. CENÁRIO
        String username = "teste";
        Usuario usuarioMock = new Usuario();
        usuarioMock.setUsername(username);
        usuarioMock.setSenhaCriptografada("hash_correto");

        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("senha_errada", "hash_correto")).thenReturn(false); // Senha não bate

        // 2. e 3. AÇÃO e VERIFICAÇÃO
        assertThrows(RuntimeException.class, () -> {
            autenticacaoService.login(username, "senha_errada");
        });
    }

    @Test
    void deveRegistrarUsuarioNovo() {
        // 1. CENÁRIO
        LoginRequestDTO dto = new LoginRequestDTO("Nome Teste", "novo_user", "123");
        
        // O usuário NÃO existe ainda
        when(usuarioRepository.findByUsername("novo_user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123")).thenReturn("hash_novo");

        // 2. AÇÃO
        Usuario novoUsuario = autenticacaoService.registrarNovoUsuario(dto);

        // 3. VERIFICAÇÃO
        assertEquals("novo_user", novoUsuario.getUsername());
        assertEquals("Nome Teste", novoUsuario.getNomeUsuario());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void deveBloquearSessaoExpirada() {
        // 1. CENÁRIO
        String idSessao = "sessao-velha";
        Sessao sessaoMock = new Sessao();
        sessaoMock.setIdSessao(idSessao);
        // Define a data de expiração para ONTEM (já passou)
        sessaoMock.setDataExpiracao(LocalDateTime.now().minusDays(1));

        when(sessaoRepository.findById(idSessao)).thenReturn(Optional.of(sessaoMock));

        // 2. e 3. AÇÃO e VERIFICAÇÃO
        assertThrows(RuntimeException.class, () -> {
            autenticacaoService.getPerfilPorSessao(idSessao);
        });

        // Verifica se o método DELETE foi chamado para limpar a sessão velha
        verify(sessaoRepository, times(1)).delete(sessaoMock);
    }
}