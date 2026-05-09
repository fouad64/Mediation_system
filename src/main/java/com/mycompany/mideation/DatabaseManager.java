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
    // GET RULES
    // =========================
    public static List<MediationRule> getRules() {

        List<MediationRule> rules =
                new ArrayList<>();

        String sql =
                "SELECT source_node_id, " +
                "destination_node_id " +
                "FROM mediation_rules";

        try (
            Connection conn =
                    DriverManager.getConnection(
                            URL, USER, PASSWORD);

            Statement stmt =
                    conn.createStatement();

            ResultSet rs =
                    stmt.executeQuery(sql)
        ) {

            while (rs.next()) {

                rules.add(
                        new MediationRule(
                                rs.getInt("source_node_id"),
                                rs.getInt("destination_node_id")
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rules;
    }

    // =========================
    // GET NODE BY ID
    // =========================
    public static NodeInfo getNodeById(int id) {

        String sql =
                "SELECT * FROM nodes WHERE id = ?";

        try (
            Connection conn =
                    DriverManager.getConnection(
                            URL, USER, PASSWORD);

            PreparedStatement ps =
                    conn.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new NodeInfo(
                        rs.getInt("id"),
                        rs.getString("node_name"),
                        rs.getString("ip_address"),
                        rs.getInt("port")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}