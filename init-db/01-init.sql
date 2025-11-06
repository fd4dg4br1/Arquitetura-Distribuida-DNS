/* Cria o schema 'dns' para a aplicação Spring */
CREATE SCHEMA IF NOT EXISTS dns;

/* 
 * NOTA: Estes comandos só funcionarão DEPOIS que o Spring (ddl-auto)
 * rodar e criar as tabelas. 
 * * Se você rodar o 'docker-compose up' pela primeira vez, 
 * esses comentários podem falhar (pois as tabelas ainda não existem).
 * Mas na *segunda* vez que você rodar (docker-compose down/up), 
 * eles funcionarão.
 *
 * (Para este trabalho, o mais importante é o schema 'dns' existir)
 */

-- Adiciona comentário ao Schema
COMMENT ON SCHEMA dns IS 'Schema para os dados de balanceamento de carga e sessão centralizada.';

-- Adiciona comentários às tabelas (só funciona se a tabela já existir)
COMMENT ON TABLE dns.tb_usuario IS 'Contém os dados dos usuários do sistema.';
COMMENT ON COLUMN dns.tb_usuario.id_usuario IS 'Identificador numérico e único para cada usuário.';
COMMENT ON COLUMN dns.tb_usuario.nome_usuario IS 'Nome completo do usuário';
COMMENT ON COLUMN dns.tb_usuario.username IS 'Nome de login único (ex: "fulano123") usado para autenticação.';
COMMENT ON COLUMN dns.tb_usuario.senha_criptografada IS 'Hash da senha do usuário (padrão BCrypt, 60 caracteres).';

COMMENT ON TABLE dns.tb_sessao IS 'Tabela centralizada para armazenar sessões ativas.';
COMMENT ON COLUMN dns.tb_sessao.id_sessao IS 'Identificador da sessão (UUID) enviado ao cliente.';
COMMENT ON COLUMN dns.tb_sessao.id_usuario IS 'Identificador que vincula esta sessão a um usuário.';
COMMENT ON COLUMN dns.tb_sessao.data_login IS 'Timestamp exato (data e hora) em que a sessão foi criada.';
COMMENT ON COLUMN dns.tb_sessao.data_expiracao IS 'Timestamp que define quando esta sessão se tornará inválida.';