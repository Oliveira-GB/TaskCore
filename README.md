# TaskCore API

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

O TaskCore é uma API RESTful de alta fidelidade para o gerenciamento de tarefas, projetada para oferecer uma experiência robusta, escalável e alinhada com as melhores práticas de desenvolvimento corporativo.

Desenvolvida com Java 21 e Spring Boot 4.0.6, a aplicação implementa padrões avançados como Soft Delete (exclusão lógica), Auditoria de Entidades, Mapeamento de Relacionamentos Complexos (1:N e N:N) e um Motor de Busca Dinâmico com paginação.

## Objetivo
Fornecer um motor backend performático para a organização de fluxos de trabalho, permitindo que tarefas principais sejam desmembradas em subtarefas detalhadas e categorizadas através de tags de forma flexível.

## Tecnologias Utilizadas

* **Linguagem**: Java 21.
* **Framework**: Spring Boot 4.0.6.
* **Banco de Dados**: PostgreSQL.
* **Persistência e Migração**: Spring Data JPA e Flyway.
* **Mapeamento e Utilitários**: MapStruct e Lombok.
* **Documentação**: Springdoc OpenAPI (Swagger UI).
* **Containerização**: Docker e Docker Compose.
* **Recursos Avançados JPA**: Hibernate EntityGraph e JPA Specifications.

## Funcionalidades Principais

* **Gestão de Tarefas (CRUD)**: Operações completas para criação, consulta, atualização e exclusão de tarefas.
* **Exclusão Lógica (Soft Delete)**: Implementação de remoção segura que preserva o histórico de dados no banco, utilizando as anotações @SQLDelete e @SQLRestriction do Hibernate para gerenciar o estado ativo dos registros de forma transparente para a aplicação.
* **Estrutura de Relacionamentos**:
    * **Subtarefas (1:N)**: Suporte à divisão de tarefas principais em unidades menores de trabalho, com gerenciamento de ciclo de vida e persistência em cascata.
    * **Categorização por Tags (N:N)**: Sistema flexível de etiquetas para classificação, gerenciado através de tabelas associativas e otimizado para evitar redundâncias.
* **Motor de Busca Avançado**: Filtragem dinâmica que permite combinar múltiplos critérios, como busca textual (título/descrição), status e tags, implementada via JPA Specifications com suporte a paginação e ordenação de resultados.
* **Tratamento de Erros Padronizado**: Centralização da lógica de exceções através de um manipulador global (@RestControllerAdvice), fornecendo respostas estruturadas e informativas em conformidade com o protocolo HTTP.

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
```


### 2. Subindo a Aplicação
Execute o comando abaixo na raiz do projeto para realizar o build da imagem e iniciar os serviços de banco de dados e API simultaneamente:

```bash
docker-compose up -d
```


Este comando utiliza as instruções do arquivo `docker-compose.yml` para orquestrar o banco de dados PostgreSQL e a aplicação Java, garantindo que a rede e a comunicação entre os serviços sejam estabelecidas automaticamente.

## Documentação da API

A interface de documentação interativa da API é fornecida através do Swagger UI. Com a aplicação em execução, os detalhes técnicos de cada endpoint podem ser acessados em:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

Nesta interface, é possível visualizar:
* Contratos de requisição e resposta (DTOs).
* Códigos de status HTTP esperados para cada operação.
* Realização de testes de integração diretamente nos endpoints mapeados.

## Estrutura do Projeto

O código está organizado de forma modular para garantir a separação de responsabilidades entre a lógica de negócio, persistência e interface:

* **api**: Camada de interface REST, contendo os controladores, objetos de transferência de dados (DTOs), mapeadores (MapStruct) e o manipulador global de exceções.
* **domain**: Núcleo do sistema, contendo as entidades de domínio, interfaces de repositório, serviços de lógica de negócio e componentes de validação customizada.
* **infrastructure**: Camada de suporte contendo configurações gerais (OpenAPI, Auditoria JPA, CORS), classes base compartilhadas e listeners de eventos do sistema.
```