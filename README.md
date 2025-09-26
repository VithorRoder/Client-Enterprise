# Client Swing Enterprise

Cliente desktop em Java Swing que autentica e consome a API (`api-enterprise`).

## Rodar
- Ajuste a URL da API em `AppConfig.java` (por padrão `http://localhost:8080`).
- Rode pelo NetBeans/IDE ou:
  ```bash
  mvn -q -DskipTests package
  java -cp target/client-swing-enterprise-1.0.0.jar br.com.phoenix.client.Main
  ```
