package com.wynd.vop.framework.autoconfigure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@EnableConfigurationProperties({VopRestClientProperties.class })
public class RemoveHeaderFilter extends OncePerRequestFilter {

    @Autowired
    private VopRestClientProperties vopRestClientProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        // loop through to hide header values based on key
        if (vopRestClientProperties.getHideHeaders()){
            String headersToHide = vopRestClientProperties.getHeadersToHide();
            String[] headerNames = headersToHide.split(",\\s*");

            // Set each header to an empty string
            for (String headerName: headerNames) {
                response.setHeader(headerName, "");
            }
        }
        filterChain.doFilter(request, response);
    }
}