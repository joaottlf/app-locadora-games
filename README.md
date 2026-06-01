# Game Store

Aplicativo Android nativo para gestão, venda e locação de jogos físicos. Desenvolvido como projeto acadêmico focado nas melhores e mais modernas práticas de desenvolvimento mobile (Jetpack Compose, MVVM e Firebase).

## Funcionalidades

### Visão do Cliente (Usuário)
**Autenticação Segura:** Login, cadastro e recuperação de senha integrados ao Firebase Auth.

**Vitrine Dinâmica:** Listagem de jogos por categorias, com seções de rolagem horizontal para \"Novidades\" e \"Em Alta\".

**Busca em Tempo Real:** Pesquisa de jogos pelo título.

**Locação e Compra:** Motor de cálculo que aplica descontos progressivos para locações de longo prazo.

**Histórico e Alertas:** Perfil do cliente com histórico de pedidos e sistema de cores semânticas avisando sobre a proximidade do prazo de devolução.

### Visão do Administrador
**Painel Gerencial (Dashboard):** Visão geral de faturamento e alertas automáticos de estoque crítico.

**Gestão de Estoque (CRUD):** Adição, edição e exclusão de jogos, incluindo botões rápidos para destacar jogos como novidade ou em alta.

**Fluxo de Caixa Avançado:** Acompanhamento de todas as transações, com filtros cruzados por tipo (compra/aluguel), período (hoje, 7 dias, 30 dias) e busca por e-mail do cliente.

**Inteligência de Devoluções:** Recálculo automático do valor final. O sistema aplica multas de 2x o valor da diária para atrasos e remove descontos indevidos em caso de devolução antecipada.

**Gestão de Equipe:** Sistema interno para elevação de privilégios (promover um usuário comum a administrador).

## Tecnologias Utilizadas

**Linguagem:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material Design 3)
- **Arquitetura:** MVVM (Model-View-ViewModel)
- **Programação Assíncrona:** Coroutines & StateFlow
- **Banco de Dados:** Firebase Cloud Firestore (NoSQL)
- **Autenticação:** Firebase Authentication

## Arquitetura do Projeto

O aplicativo foi estruturado utilizando o padrão **MVVM** com fluxo de dados unidirecional. A interface (View) apenas observa os estados, enquanto as regras de negócio vivem nos ViewModels e a comunicação com a nuvem ocorre através da camada de Repositórios.

Além disso, o banco de dados NoSQL foi modelado utilizando conceitos de **Desnormalização de Dados** (ex: salvar o e-mail do usuário diretamente no documento do pedido) para otimizar as leituras e evitar consultas complexas, garantindo máxima performance.

## Como Executar o Projeto
1. Clone este repositório: `git clone https://github.com/joaottlf/app-locadora-games.git`
2. Abra o projeto no **Android Studio** (Recomendado: Versão Panda ou superior).
3. Crie um projeto no [Firebase Console](https://console.firebase.google.com/).
4. Registre um aplicativo Android no projeto do Firebase com o mesmo `applicationId` deste projeto.
5. Faça o download do arquivo `google-services.json` e coloque-o na pasta `app/` do seu projeto.
6. No Firebase Console, ative os provedores **Authentication** (Provedor: E-mail/Senha) e o **Firestore Database**.
7. Sincronize o projeto no Android Studio e execute em um emulador ou dispositivo físico.

## Contexto Acadêmico

Projeto desenvolvido como requisito avaliativo para o curso de Sistemas de Informação.

**Aluno:** João Augustto Lima de Faria

**Instituição:** Anhanguera

**Turno:** Noturno

**Disciplina:** Desenvolvimento para Dispositivos Móveis

**Professor:** Cláudio Damasceno
