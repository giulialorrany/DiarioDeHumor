# Diário de Humor

## Como Rodar:

1. Rodar o MySQL server <span style="color: #AF55FF">(no caso foi usado o do *[XAMPP](https://www.apachefriends.org/pt_br/index.html)*)</span>
2. Criar o [banco.sql](model/banco.sql)
3. Adaptar as configurações do banco em [Conexao.java](model/Conexao.java) <span style="color: #6BFF08">(linhas 13-16)</span>
    ```java
        // Config do banco
        String url = "jdbc:mysql://localhost:3307/diario_de_humor"; //  jdbc:mysql://localhost:[número_da_port]/[nome_do_banco]
        String username = "root"; //    [usuário]
        String password = ""; //    [senha]
    ```
4. Rodar [Main.java](Main.java)
5. Abrir o link (*[http://localhost:8080](http://localhost:8080)*) no navegador <span style="color: #AF55FF">(ou troque o número 8080 se for modificado em [application.properties](../../../../resources/application.properties))</span>