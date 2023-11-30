## Sistemas Distribuídos — Trabalho Prático - Cloud Computing


##  Introdução
Este projeto é uma implementação de um serviço de cloud computing com funcionalidades de Function-as-a-Service (FaaS). Ele permite que os usuários se registrem e autentiquem, enviem tarefas de computação para serem executadas no servidor, e consultem o estado atual de ocupação do serviço.

## Funcionalidades
- Autenticação e Registo de utilizadores:  Permite que os utilizadores criem uma conta e façam login no sistema.


- Execução de Tarefas: 
Os utilizadores podem enviar tarefas de computação junto com a quantidade de memória necessária. As tarefas são processadas pelo servidor.


- Consulta de Estado: 
Os utilizadores podem verificar a memória disponível e o número de tarefas pendentes no servidor.


- Submissão Assíncrona de Tarefas: 
Suporte para envio de novas tarefas sem a necessidade de esperar pelas respostas de tarefas anteriores.



## Pré-requisitos
Java JDK 11 

## Instalação
Clone o repositório:
```bash

git clone https://link-para-o-repositorio
```
Navegue até à pasta do projeto:
```bash

cd sistemasdistribuidos
```
Compile o projeto (exemplo usando o Maven):
```bash

mvn clean install
```
## Uso
Instruções sobre como usar o projeto após a instalação. Por exemplo:

Para iniciar o servidor, execute:

```bash

java -jar path/to/server.jar
```
Para executar o cliente, use:

```bash

java -jar path/to/client.jar
```
