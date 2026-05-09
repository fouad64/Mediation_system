/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mideation;

/**
 *
 * @author roqaya
 */

import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private static final String URL =
                                    "jdbc:postgresql://ep-wild-credit-aqie09pe-pooler.c-8.us-east-1.aws.neon.tech:5432/neondb?sslmode=require";

    private static final String USER = "neondb_owner";

    private static final String PASSWORD = "npg_2lIRsfruo5vD";

    // =========================
    // STATIC INIT
    // =========================
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // AUTHENTICATION
    // =========================
    public static boolean authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Returns true if a match is found
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================
    // GET RULES
    // =========================
    public static List<MediationRule> getRules() {
        List<MediationRule> rules = new ArrayList<>();
        String sql = "SELECT id, source_node_id, destination_node_id FROM mediation_rules";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rules.add(new MediationRule(
                        rs.getInt("id"),
                        rs.getInt("source_node_id"),
                        rs.getInt("destination_node_id")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rules;
    }

    public static boolean addRule(MediationRule rule) {
        String sql = "INSERT INTO mediation_rules (source_node_id, destination_node_id) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rule.getSourceNodeId());
            ps.setInt(2, rule.getDestinationNodeId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateRule(MediationRule rule) {
        String sql = "UPDATE mediation_rules SET source_node_id = ?, destination_node_id = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rule.getSourceNodeId());
            ps.setInt(2, rule.getDestinationNodeId());
            ps.setInt(3, rule.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteRule(int id) {
        String sql = "DELETE FROM mediation_rules WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================
    // GET NODES
    // =========================
    public static List<NodeInfo> getAllNodes() {
        List<NodeInfo> nodes = new ArrayList<>();
        String sql = "SELECT * FROM nodes";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                nodes.add(new NodeInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("ip_address"),
                        rs.getInt("port")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nodes;
    }

    public static NodeInfo getNodeById(int id) {
        String sql = "SELECT * FROM nodes WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new NodeInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("ip_address"),
                        rs.getInt("port")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addNode(NodeInfo node) {
        String sql = "INSERT INTO nodes (name, ip_address, port) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, node.getNodeName());
            ps.setString(2, node.getHost());
            ps.setInt(3, node.getPort());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateNode(NodeInfo node) {
        String sql = "UPDATE nodes SET name = ?, ip_address = ?, port = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, node.getNodeName());
            ps.setString(2, node.getHost());
            ps.setInt(3, node.getPort());
            ps.setInt(4, node.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteNode(int id) {
        String sql = "DELETE FROM nodes WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}