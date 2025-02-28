/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.async;

import java.io.IOException;

import jakarta.enterprise.inject.Vetoed;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.greenscreens.quark.QuarkProducer;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.internal.QuarkErrors;
import io.greenscreens.quark.internal.QuarkHandlerUtil;
import io.greenscreens.quark.web.ServletUtils;

/**
 * Handles async controllers execution. Also, if timeouts, send an error
 * response to requester.
 */
@Vetoed
final class QuarkAsyncListener implements AsyncListener {

    @Override
    public void onComplete(final AsyncEvent event) throws IOException {
        QuarkProducer.releaseAsync();
    }

    @Override
    public void onTimeout(final AsyncEvent event) throws IOException {
        final HttpServletResponse response = response(event);
        if (!response.isCommitted()) {          
            final HttpServletRequest request = request(event);
            final boolean compress = ServletUtils.supportGzip((HttpServletRequest) request);
            final ExtJSResponse result = QuarkHandlerUtil.getError(QuarkErrors.E7777);
            ServletUtils.sendResponse(ServletUtils.wrap(response), result, compress);
        }
        QuarkProducer.releaseAsync();
    }

    @Override
    public void onError(final AsyncEvent event) throws IOException {
        QuarkProducer.releaseAsync();
    }

    @Override
    public void onStartAsync(final AsyncEvent event) throws IOException {
        // not used
    }

    public HttpServletRequest request(final AsyncEvent event) {
        return ServletUtils.wrap(event.getAsyncContext().getRequest());
    }

    public HttpServletResponse response(final AsyncEvent event) {
        return ServletUtils.wrap(event.getAsyncContext().getResponse());
    }

    public static QuarkAsyncListener create() {
        return new QuarkAsyncListener();
    }
}
