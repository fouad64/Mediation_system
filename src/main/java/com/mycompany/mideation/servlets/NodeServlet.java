package com.mycompany.mideation.servlets;

import com.google.gson.Gson;
import com.mycompany.mideation.DatabaseManager;
import com.mycompany.mideation.NodeInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/nodes/*")
public class NodeServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            List<NodeInfo> nodes = DatabaseManager.getAllNodes();
            resp.getWriter().write(gson.toJson(nodes));
        } else {
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                NodeInfo node = DatabaseManager.getNodeById(id);
                if (node != null) {
                    resp.getWriter().write(gson.toJson(node));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Node not found\"}");
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid node ID\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        NodeInfo node = parseBody(req, NodeInfo.class);
        if (node != null && DatabaseManager.addNode(node)) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"success\": true, \"message\": \"Node created successfully\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\": false, \"error\": \"Failed to create node\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Node ID required for update\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            NodeInfo node = parseBody(req, NodeInfo.class);
            
            // In NodeInfo we don't have setId, so we recreate the object or rely on updating fields.
            // Let's create a new NodeInfo with the ID from the URL path.
            if (node != null) {
                NodeInfo updatedNode = new NodeInfo(id, node.getNodeName(), node.getHost(), node.getPort());
                if (DatabaseManager.updateNode(updatedNode)) {
                    resp.getWriter().write("{\"success\": true, \"message\": \"Node updated successfully\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("{\"success\": false, \"error\": \"Failed to update node\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid node ID\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Node ID required for deletion\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            if (DatabaseManager.deleteNode(id)) {
                resp.getWriter().write("{\"success\": true, \"message\": \"Node deleted successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"success\": false, \"error\": \"Failed to delete node\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid node ID\"}");
        }
    }

    private <T> T parseBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return gson.fromJson(buffer.toString(), clazz);
    }
}
