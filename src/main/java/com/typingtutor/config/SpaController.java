package com.typingtutor.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards all non-API, non-asset requests to index.html so React Router
 * can handle client-side navigation (e.g. /dashboard, /lessons/1).
 */
@Controller
public class SpaController {

    @RequestMapping(value = {
        "/",
        "/login",
        "/register",
        "/dashboard",
        "/lessons/**",
        "/placement",
        "/exam/**",
        "/profile",
        "/analytics",
        "/certificates",
        "/help",
        "/admin",
        "/verify-email",
        "/change-password"
    })
    public String forward(HttpServletRequest request) {
        return "forward:/index.html";
    }
}
