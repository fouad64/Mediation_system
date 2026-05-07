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

The system applies strict multi-layered filtering during the final stage:

*   **MSC (Mobile Switching Center)**:
    *   **Name Validation**: `network_element_id` must contain "MSC".
    *   **State Validation**: `status` must be "normal".
    *   **Business Rules**: `duration_sec > 0` and must not be a short code.
*   **PGW (Packet Gateway)**:
    *   **Name Validation**: `network_element_id` must contain "PGW".
    *   **State Validation**: `status` must be "active".
    *   **Business Rules**: Must have `uplink` or `downlink` data > 0.
*   **SMSC (SMS Center)**:
    *   **Name Validation**: `network_element_id` must contain "SMSC".
    *   **State Validation**: `status` must be "delivered".
    *   **Business Rules**: Sender/Receiver must not be short numbers.

## 🚦 Getting Started

### 1. Automation Script (Recommended)
The easiest way to run the entire project is using the provided automation script. It handles infrastructure startup, status checks, and pipeline execution in one command:
```bash
chmod +x run_pipeline.sh
./run_pipeline.sh
```

### 2. Manual Steps
If you prefer manual execution:
1.  **Start Infrastructure**: `docker-compose up -d`
2.  **Verify Status**: `docker-compose ps`
3.  **Run Mediation**: `mvn clean compile exec:java`

## 📝 Output
The final, cleaned CDR files will be available in the `filtered/` directory.
