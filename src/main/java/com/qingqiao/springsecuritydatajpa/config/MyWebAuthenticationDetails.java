package com.qingqiao.springsecuritydatajpa.config;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

public class MyWebAuthenticationDetails extends WebAuthenticationDetails {

    private boolean isPassed;

    public MyWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        String code = request.getParameter("code");
        String s = code.toLowerCase(Locale.ROOT);
        String verify_code = (String) request.getSession().getAttribute("verify_code");
        String s1 = verify_code.toLowerCase(Locale.ROOT);
        if (code == null || verify_code == null || !s.equals(s1)) {
            isPassed = true;
        }
    }

    public boolean isPassed() {
        return isPassed;
    }
}
