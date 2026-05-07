# CDR Mediation System

A robust, Java-based mediation pipeline designed to process, transport, and filter Call Detail Records (CDRs) across a distributed SFTP environment.

## 🚀 Overview

The system automates the lifecycle of CDR data by splitting raw logs, distributing them to specialized remote servers, retrieving them for verification, and applying advanced business logic to filter out invalid or low-value records.

## 🏗️ System Architecture

The pipeline consists of four distinct phases:

1.  **Process Phase**: Splits the primary `input.csv` file into three specialized streams: **MSC**, **SMSC**, and **PGW**.
2.  **Upload Phase**: Securely transfers the split files to their respective SFTP containers using the **JSch** library.
3.  **Download Phase**: Synchronizes the remote data back to a local `down/` folder to ensure data integrity.
4.  **Filter Phase**: Applies strict business rules to the downloaded data, saving the cleaned results into the `filtered/` directory.

## 🛠️ Technology Stack

*   **Language**: Java 17
*   **Build Tool**: Maven
*   **Libraries**: JSch (SFTP), Apache Commons CSV
*   **Infrastructure**: Docker Compose (mocking multiple SFTP nodes)

## 📁 Project Structure

```text
├── src/main/java/com/mycompany/mideation/
│   ├── MideationMain.java    # Orchestrates the pipeline
│   ├── SftpFilter.java       # Custom business logic & filtering
│   ├── SftpUploader.java     # Secure upload handling
│   ├── SftpDownloader.java   # Secure download handling
│   ├── CdrProcessor.java     # Initial file splitting
│   └── ConfigLoader.java     # Configuration manager
├── docker-compose.yml         # Defines SFTP server infrastructure
├── pom.xml                   # Maven dependencies
└── src/main/resources/
    └── config.properties      # Network and credential settings
```

## 🔍 Filtering Logic (SftpFilter)

The system applies the following rules during the final filtering stage:

*   **MSC (Mobile Switching Center)**:
    *   Drops records with `duration = 0`.
    *   Drops records involving **Short Codes** (2-6 digits).
    *   Drops records where the release case is not `normal`.
*   **PGW (Packet Gateway)**:
    *   Drops records where both `uplink` and `downlink` bytes are `0`.
*   **SMSC (SMS Center)**:
    *   Drops records involving **Short Numbers**.
    *   Drops records where status is not `delivered`.

## 🚦 Getting Started

### 1. Start the SFTP Infrastructure
Ensure Docker is running, then start the containers:
```bash
docker-compose up -d
```

### 2. Prepare Input Data
Place your raw CDR file in the `in/` folder (configured as `input_1.csv` by default).

### 3. Run the Pipeline
Execute the full mediation process using Maven:
```bash
mvn clean compile exec:java
```

## 📝 Output
The final, cleaned CDR files will be available in the `filtered/` directory.
