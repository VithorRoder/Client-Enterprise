# Client Web Enterprise

Aplicação **web** com servidor HTTP embutido em Java que disponibiliza uma interface moderna em **HTML + Tailwind CSS**
para consumir a [**Enterprise API**](https://github.com/VithorRoder/API-Enterprise). A aplicação continua utilizando
OkHttp e Jackson para se autenticar na API e realizar o CRUD de clientes, mas agora entrega a experiência diretamente
no navegador.

---

## 🧩 Stack

- **Java 21** (servidor HTTP leve utilizando `com.sun.net.httpserver`)
- **HTML + Tailwind CSS (via CDN)** para o layout responsivo
- **JavaScript moderno (ES6)** para consumir os endpoints locais
- **OkHttp** (HTTP client) + **Jackson** (JSON) para conversar com a Enterprise API
- **Maven** para build/empacotamento (gera JAR com servidor + front-end estático)

---

## ✨ Funcionalidades

- Login com usuário/senha (`POST /api/login` da Enterprise API) e obtenção de **JWT**
- CRUD de **Customers** (nome/e-mail) protegido por bearer token
- Interface web responsiva com componentes estilizados via Tailwind
- Build gera um único JAR: ao executar, um servidor HTTP local (porta padrão `8080`) entrega o front-end

---

## 📦 Requisitos

- **Java 21+** instalado (`java -version`)
- **Maven 3.9+** (`mvn -v`)
- **Enterprise API** rodando (padrão `http://localhost:8081`)

> A aplicação web continua consumindo a API REST — ela não acessa o banco de dados diretamente.

---

## ⚙️ Configuração da URL da API

A URL base da API pode ser ajustada via variável de ambiente:

```bash
export API_BASE_URL="http://SEU_HOST:8081"
```

Por padrão (`API_BASE_URL` não definida) será utilizado `http://localhost:8081`.

---

## ▶️ Como executar (dev)

```bash
mvn clean package
java -jar target/client-web-enterprise-2.0.0-jar-with-dependencies.jar
```

Depois acesse <http://localhost:8080> no navegador. As credenciais padrão da API são `admin` / `admin123`.

Para alterar a porta HTTP do cliente web utilize a variável `APP_PORT`:

```bash
APP_PORT=9090 java -jar target/client-web-enterprise-2.0.0-jar-with-dependencies.jar
```

---

## 🔐 Fluxo de Autenticação

1. O usuário informa login e senha na tela web
2. O cliente envia `POST /api/login` para a API Enterprise
3. A API responde com o **JWT**
4. O token é armazenado no navegador e enviado nas próximas requisições (`Authorization: Bearer <token>`)

---

## 🧪 Teste rápido da API

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## 🧹 Estrutura do projeto

```
client-web-enterprise/
 ├─ src/main/java/br/com/phoenix/client
 │   ├─ config/         # Configuração da URL da API
 │   ├─ model/          # DTOs simples
 │   ├─ net/            # Cliente HTTP (OkHttp + bearer token)
 │   ├─ service/        # Serviços que consomem a API Enterprise
 │   └─ Main.java       # Servidor HTTP + roteamento/rest proxy
 ├─ src/main/resources/web/
 │   ├─ index.html      # UI em HTML + Tailwind
 │   └─ app.js          # Lógica de front-end (login + CRUD)
 ├─ pom.xml
 └─ README.md
```

---

## 🛡️ Boas práticas

- Configure a API com HTTPS em ambientes de produção
- Utilize variáveis de ambiente para segredos/URLs
- Considere adicionar autenticação de sessão/cookies se for expor o cliente na internet

---

## 🧑‍💻 Autor original

**Vithor Roder** — Full Stack (Java / Spring / Desktop)
Rio de Janeiro — BR
