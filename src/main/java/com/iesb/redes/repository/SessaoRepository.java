package com.iesb.redes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iesb.redes.model.Sessao;

@Repository
public interface SessaoRepository extends JpaRepository<Sessao, String>{
    
}