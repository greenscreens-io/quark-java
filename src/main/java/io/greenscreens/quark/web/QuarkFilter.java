/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Generic filter that handle errors without sending full error stack trace to front
 */
public abstract class QuarkFilter extends HttpFilter {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException {
        try {
            onFilter(req, res, chain);
        } catch (Exception e) {
            ServletUtils.log(e, req, res);
        } finally {
            onFinish(req, res);
        }
    }
    
    protected void onFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (res.isCommitted()) return;
        if (req.isAsyncStarted()) return;
        final boolean sts = onFilter(req, res);
        if (res.isCommitted()) return; 
        if (sts) {
            chain.doFilter(req, res);       
        } else {
            ServletUtils.sendError(res, HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Used by extended classes. Return true if extended class sent a response to the client, otherwise return false. 
     * @param req
     * @param res
     * @return Return true to indicate, processing is done, false to indicate default processing
     * @throws IOException
     */
    protected abstract boolean onFilter(final HttpServletRequest req, final HttpServletResponse res) throws IOException;
    
    /**
     * Used by extended classes. Called when all processing is done and response is sent to the client
     * @param req
     * @param res
     * @throws IOException
     */
    protected void onFinish(final HttpServletRequest req, final HttpServletResponse res) throws IOException {};

}