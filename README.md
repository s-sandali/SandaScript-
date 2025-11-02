# SandaScript++

> A Domain-Specific Language for HTTP API Testing

## Table of Contents

- [Introduction](#introduction)
- [System Overview](#system-overview)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Building the Translator](#building-the-translator)
- [Writing Tests](#writing-tests)
  - [DSL Syntax](#dsl-syntax)
  - [Example Test](#example-test)
- [Usage](#usage)
  - [Generate JUnit Tests](#generate-junit-tests)
  - [Start the Backend](#start-the-backend)
  - [Run Tests](#run-tests)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Error Handling](#error-handling)
- [Troubleshooting](#troubleshooting)

---

## Introduction

**SandaScript++** is a custom Domain-Specific Language (DSL) designed to simplify the process of writing and running HTTP API tests. Instead of manually writing JUnit code, you can describe REST API tests (GET, POST, DELETE, PUT) in an easy-to-read `.test` file. The translator reads the `.test` file and generates runnable JUnit 5 test code (`GeneratedTests.java`) that sends real HTTP requests to a backend server and verifies responses including status codes and body contents.

---

## System Overview

### Architecture Pipeline

```
.test file → Lexer (JFlex) → Parser → AST → Validator → CodeGen → GeneratedTests.java
```

### Component Details

1. **Lexical Analysis** (`lexer.flex`)
  - Reads the `.test` file character by character
  - Tokenizes content using JFlex
  - Generates `Lexer.java` automatically

2. **Parsing** (`parser.cup`)
  - Takes tokens from the lexer
  - Builds an Abstract Syntax Tree (AST) using CUP
  - Produces `Parser.java` (actual Java code) and `sym.java` (constants)
  - Outputs a `Program` object as the AST root

3. **AST Structure**
  - **Program.java**: Root of the AST
  - **TestBlock.java**: Represents a single test block
  - **Statement.java**: Individual statements (HTTP requests)
  - **Assertion.java**: Status and response assertions

4. **Semantic Validation** (`Validator.java`)
  - Checks for duplicate variable names
  - Validates missing semicolons
  - Ensures body types are strings
  - Verifies each test has at least one request and two assertions
  - Detects undefined variable references
  - Throws `ParseException` on errors

5. **Code Generation** (`CodeGen.java`)
  - Converts validated AST to JUnit test class
  - Produces `GeneratedTests.java`

6. **Translator** (`Translator.java`)
  - Main class that orchestrates the entire pipeline
  - Reads `.test` file, tokenizes, parses, validates, and generates code

7. **Execution**
  - Maven + JUnit 5 runs HTTP API tests against the backend server

---

## Getting Started

### Prerequisites

Ensure you have the following installed:

- **Java 11+** (JAVA_HOME set to JDK 11 or higher)
- **Maven 3.8+**

#### Verify Installation

```bash
java -version
mvn -version
```

### Building the Translator

From the project root directory:

```bash
mvn -q -DskipTests compile
```

This compiles your JFlex and CUP sources to build the translator.

---

## Writing Tests

### DSL Syntax

SandaScript++ supports the following constructs:

#### Configuration Block

```
config {
  base_url = "http://localhost:8080";
  header "Content-Type" = "application/json";
}
```

#### Variables

```
let name = "value";
let id = 42;
```

Variables can be used as `$name` inside strings and paths.

#### HTTP Requests

**GET/DELETE:**
```
GET "/path_or_url" [ { header ...; } ];
DELETE "/path_or_url" [ { header ...; } ];
```

**POST/PUT:**
```
POST "/path_or_url" {
  [header ...;]
  [body = "...";]
};

PUT "/path_or_url" {
  [header ...;]
  [body = "...";]
};
```

#### Assertions

```
expect status = 200;
expect status in 200..299;
expect header "Content-Type" = "application/json";
expect header "Content-Type" contains "json";
expect body contains "token";
```

### Example Test

See `examples/example_full.test` for a complete reference. Here's a sample:

```
config {
  base_url = "http://localhost:8080";
  header "Content-Type" = "application/json";
}

let user = "admin";
let id = 42;

test Login {
  POST "/api/login" {
    body = "{ \"username\": \"$user\", \"password\": \"1234\" }";
  };
  expect status = 200;
  expect header "Content-Type" contains "json";
  expect body contains "\"token\":";
}

test GetUser {
  GET "/api/users/$id";
  expect status in 200..299;
  expect body contains "\"id\": 42";
}

test DeleteUser {
  DELETE "/api/users/$id";
  expect status = 204;
}
```

---

## Usage

### Generate JUnit Tests

Run the translator on your `.test` file:

#### Windows (cmd.exe)

```cmd
mvn -q org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=translator.Translator -Dexec.args=examples\example_full.test
```

#### Windows (PowerShell) / Linux / macOS

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.1.0:java '-Dexec.mainClass=translator.Translator' '-Dexec.args=examples/example_full.test'
```

**Success Output:**
```
GeneratedTests.java created successfully!
```

The JUnit test class is written to `src/test/java/GeneratedTests.java`.

### Start the Backend

*(Optional)* Use your local backend (e.g., Spring Boot) on `http://localhost:8080` that provides the endpoints used in your tests.

```bash
cd backend-demo
mvn clean package
java -jar target/testlang-demo-0.0.1-SNAPSHOT.jar
```

### Run Tests

#### Compile Test Sources Only

```bash
mvn -q test-compile
```

#### Run Generated Tests

Ensure the backend is running, then:

```bash
mvn -q -Dtest=GeneratedTests test
```

**Expected Output:**

```
Tests run: 3, Failures: 0, Skipped: 0
BUILD SUCCESS
```

---

## Architecture

### Generated Test Details

The `GeneratedTests` class contains:
- One `@Test` method per `test Name { ... }` block
- `HttpClient` with 10-second request timeout
- UTF-8 response body handling
- Sequential request execution
- Assertions checked against the latest response

---

## Project Structure

```
testlang-plus-plus/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── translator/
│   │           ├── Translator.java       # Main entry point
│   │           ├── CodeGen.java          # Code generator
│   │           ├── Validator.java        # Semantic validator
│   │           └── ast/                  # AST classes
│   │               ├── Program.java
│   │               ├── TestBlock.java
│   │               ├── Statement.java
│   │               └── Assertion.java
│   └── test/
│       └── java/
│           └── translator/
│               └── TranslatorParseTests.java  # Parser tests
├── examples/
│   └── example_full.test                 # Complete DSL example
├── lexer.flex                            # Lexer specification
├── parser.cup                            # Parser grammar
└── pom.xml                               # Maven configuration
```

---

## Error Handling

The parser provides diagnostic messages for invalid input:

| Invalid Input | Error Message |
|--------------|---------------|
| `let 2a = "x";` | `Line N: expected IDENT after 'let'` |
| `POST "/x" { body = 123; };` | `Line N: expected STRING after 'body ='` |
| `expect status = "200";` | `Line N: expected NUMBER for status` |
| `GET "/x" expect status = 200;` | `Line N: expected ';' after request` |

---

## Troubleshooting

### Clean and Rebuild

If you encounter build issues:

```bash
mvn -q clean compile
```

### Common Issues

1. **Tests fail to run**: Ensure the backend server is running and accessible
2. **Compilation errors**: Verify Java 11+ is installed and JAVA_HOME is set correctly
3. **Parser errors**: Check your `.test` file syntax against the examples


