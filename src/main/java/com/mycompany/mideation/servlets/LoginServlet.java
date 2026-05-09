package com.mycompany.mideation.servlets;

import com.google.gson.Gson;
import com.mycompany.mideation.DatabaseManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        Map<String, String> credentials = gson.fromJson(buffer.toString(), Map.class);
        String username = credentials.get("username");
        String password = credentials.get("password");

        Map<String, Object> jsonResponse = new HashMap<>();

        if (DatabaseManager.authenticateUser(username, password)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("user", username);
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Login successful");
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Invalid username or password");
        }

        resp.getWriter().write(gson.toJson(jsonResponse));
    }
}
