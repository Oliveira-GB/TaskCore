# TaskCore API

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

O TaskCore é uma API RESTful de alta fidelidade para o gerenciamento de tarefas, projetada para oferecer uma experiência robusta, escalável e alinhada com as melhores práticas de desenvolvimento corporativo.

Desenvolvida com Java 21 e Spring Boot, a aplicação implementa padrões avançados como Soft Delete (exclusão lógica), Auditoria de Entidades, Mapeamento de Relacionamentos Complexos (1:N e N:N) e um Motor de Busca Dinâmico com paginação.

## Objetivo
Fornecer um motor backend performático para a organização de fluxos de trabalho, permitindo que tarefas principais sejam desmembradas em subtarefas detalhadas e categorizadas através de tags de forma flexível.

## Tecnologias Utilizadas

* **Linguagem**: Java 21.
* **Framework**: Spring Boot.
* **Banco de Dados**: PostgreSQL.
* **Persistência e Migração**: Spring Data JPA e Flyway.
* **Mapeamento e Utilitários**: MapStruct e Lombok.
* **Documentação**: Springdoc OpenAPI (Swagger UI).
* **Containerização**: Docker e Docker Compose.
* **Recursos Avançados JPA**: Hibernate EntityGraph e JPA Specifications.

## Funcionalidades Principais

* **Gestão de Tarefas (CRUD)**: Operações completas para criação, consulta, atualização e exclusão de tarefas.
* **Exclusão Lógica (Soft Delete)**: Implementação de remoção segura que preserva o histórico de dados no banco, utilizando as anotações `@SQLDelete` e `@SQLRestriction` do Hibernate para gerenciar o estado ativo dos registros de forma transparente para a aplicação.
* **Estrutura de Relacionamentos**:
    * **Subtarefas (1:N)**: Suporte à divisão de tarefas principais em unidades menores de trabalho, com gerenciamento de ciclo de vida e persistência em cascata.
    * **Categorização por Tags (N:N)**: Sistema flexível de etiquetas para classificação, gerenciado através de tabelas associativas e otimizado para evitar redundâncias.
* **Motor de Busca Avançado**: Filtragem dinâmica que permite combinar múltiplos critérios, como busca textual (título/descrição), status e tags, implementada via JPA Specifications com suporte a paginação e ordenação de resultados.
* **Tratamento de Erros Padronizado**: Centralização da lógica de exceções através de um manipulador global (`@RestControllerAdvice`), fornecendo respostas estruturadas e informativas em conformidade com o protocolo HTTP.

## Como Executar

A aplicação está totalmente containerizada, o que simplifica o processo de configuração e execução.

### Pré-requisitos
* Docker instalado.
* Docker Compose instalado.

### 1. Configuração do Ambiente (.env)
A aplicação utiliza variáveis de ambiente para gerenciar configurações sensíveis e de infraestrutura. Na raiz do projeto, crie ou verifique o arquivo `.env`:

```env
DB_URL=jdbc:postgresql://db:5432/taskcore_db
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
PORT=8080