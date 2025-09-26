[README.md](https://github.com/user-attachments/files/22566771/README.md)
# Client Swing Enterprise

Aplicativo **desktop Java Swing** que autentica e consome a **Phoenix Enterprise API** (Spring Boot 3) — a API, por sua vez, persiste dados em **PostgreSQL** via JPA/Hibernate e controla acesso com **JWT**.

---

## 🧩 Stack

- **Java 21**
- **Swing** + FlatLaf (tema moderno)
- **OkHttp** (HTTP client)
- **Jackson** (JSON)
- **Maven** (build/empacotamento)
- **Phoenix Enterprise API** (backend) com **PostgreSQL** (via API)

---

## ✨ Funcionalidades

- Login com **usuário/senha** → recebe **JWT** da API
- CRUD de **Customers** (nome/e-mail) via endpoints protegidos
- Armazena/tokeniza requisições automaticamente (Bearer)
- JAR único “**jar-with-dependencies**” para execução direta

---

## 📦 Requisitos

- **Java 21+** instalado (`java -version`)
- **Maven 3.9+** (`mvn -v`)
- **API** rodando (https://github.com/VithorRoder/API-Enterprise) (por padrão em `http://localhost:8081`)
  > A API fala com o PostgreSQL; o Swing **não acessa DB diretamente**.

---

## ⚙️ Configuração da URL da API

### Opção A — Classe genérica (pública no GitHub)
No arquivo `br/com/phoenix/client/config/AppConfigGithub.java`:

```java
public class AppConfigGithub {
    // Usar localhost no GitHub para não expor IP da VPN/servidor
    public static final String API_BASE_URL_GITHUB = "http://localhost:8081";
}
```

### Opção B — Variável de ambiente (mais profissional)
```java
public final class AppConfig {
    public static final String API_BASE_URL =
        System.getenv().getOrDefault("API_BASE_URL", "http://localhost:8081");
    private AppConfig() {}
}
```

No Windows (PowerShell):
```powershell
setx API_BASE_URL "http://SEU_IP_OU_DOMINIO:8081"
```

---

## ▶️ Como executar (dev)

```bash
mvn clean package
java -jar target/client-swing-enterprise-1.0.0-jar-with-dependencies.jar
```

---

## 🔐 Fluxo de Autenticação

1. Usuário preenche **login** e **senha**  
2. Cliente chama `POST /api/auth/login`  
3. API retorna **JWT**  
4. Cliente armazena e envia `Authorization: Bearer <token>` nas próximas chamadas

---

## 🌐 Executando pela rede (ex.: Radmin VPN)

- Deixe a **API** bindando externamente (`server.address=0.0.0.0`)
- Porta 8081 liberada no firewall
- No **Swing**, aponte `API_BASE_URL` para o **IP da VPN** do servidor (ex.: `http://26.xxx.xxx.xxx:8081`)

---

## 🧪 Teste de Integração (manual)

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/auth/login" `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin123"}'
```

---

## 🛠️ Build do JAR executável

```bash
mvn clean package
java -jar target/client-swing-enterprise-1.0.0-jar-with-dependencies.jar
```

---

## 🧹 Organização e Git

**Inclua**:
- `pom.xml`, `src/`, `README.md`, `.gitignore`
- `AppConfigGithub.java` (com `localhost`)

**Exclua** (`.gitignore`):
```
target/
*.log
*.rar
*.zip
*.iml
*.idea/
*.class
.DS_Store
src/main/java/br/com/phoenix/client/config/AppConfig.java
.env
```

---

## 🧩 Estrutura (sugestão)

```
client-swing-enterprise/
 ├─ src/main/java/br/com/phoenix/client
 │   ├─ config/         # AppConfigGithub / AppConfig (não versionado)
 │   ├─ net/            # ApiHttpClient (OkHttp + bearer)
 │   ├─ service/        # AuthService, CustomerService
 │   └─ ui/             # LoginFrame, MainFrame, etc.
 ├─ src/main/resources/
 ├─ pom.xml
 └─ README.md
```

---

## 🛡️ Segurança

- **Nunca** suba IPs/segredos da sua infraestrutura pública
- Prefira **variáveis de ambiente**
- Para produção: use **HTTPS**, **proxy reverso** e **rate limit**

---

## 🧑‍💻 Autor

**Vithor Roder** — Full Stack (Java / Spring / Desktop)  
Rio de Janeiro — BR
