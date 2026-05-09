CREATE TABLE Nodes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,

    type VARCHAR(20) CHECK (type IN ('UPSTREAM', 'DOWNSTREAM')),

    protocol VARCHAR(50), 
    -- Example: FTP, SFTP, SCP (you can also normalize this later)

    authentication VARCHAR(50),

    username VARCHAR(100),
    password VARCHAR(100),

    ip_address VARCHAR(45),
    port INT
);
=====================================================
=====================================================
CREATE TABLE Mediation_Rules (
    id SERIAL PRIMARY KEY,

    source_node_id INT NOT NULL,
    destination_node_id INT NOT NULL,

    FOREIGN KEY (source_node_id) REFERENCES Nodes(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_node_id) REFERENCES Nodes(id) ON DELETE CASCADE
);

=====================================================
=====================================================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'admin'
);
