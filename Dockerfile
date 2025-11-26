# Estágio 1: Build
# Usa uma imagem do Maven (baseada em Java 17) para compilar seu código
FROM maven:3.9.6-eclipse-temurin-17-focal AS build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o pom.xml primeiro (para aproveitar o cache do Docker)
COPY pom.xml .
# Baixa as dependências
RUN mvn dependency:go-offline

# Copia o resto do seu código-fonte
COPY src ./src

# Compila o projeto e cria o arquivo .jar (pulando testes)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

# Copia o .jar que foi criado no Estágio 1 para esta nova imagem
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta 8080 (que o Spring usa)
EXPOSE 8080

# O comando que será executado quando o contêiner iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]