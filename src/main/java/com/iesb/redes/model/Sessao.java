package com.iesb.redes.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_sessao", schema = "dns")
public class Sessao{


    @Id
    @Column(name="id_sessao")
    String idSessao;

    @Column(name="data_login")
    LocalDateTime dataLogin;

    @Column(name="data_expiracao")
    LocalDateTime dataExpiracao;

    @ManyToOne 
    @JoinColumn(name = "id_usuario")
    Usuario usuario;    

    public Sessao(Usuario usuario) {
        this.usuario = usuario;
        this.idSessao = UUID.randomUUID().toString(); //gera o id, nao é do tipo long pra evitar sequences que poderia ocasionar session jihacking aleatorio
        this.dataLogin = LocalDateTime.now();
        this.dataExpiracao = LocalDateTime.now().plusMinutes(30); // sessao dura só 30 min
    }
}