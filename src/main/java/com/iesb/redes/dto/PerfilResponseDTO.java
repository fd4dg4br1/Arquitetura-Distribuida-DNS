package com.iesb.redes.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PerfilResponseDTO {

    private String nomeUsuario;
    private String codigoSessao;
    private LocalDateTime dataLogin;
    private String hostnameServidor;
}
