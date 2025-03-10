/*
 * Copyright (C) 2015, 2024 Green Screens Ltd.
 */
package io.greenscreens.quark.internal;

import java.lang.ScopedValue.Carrier;
import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkProducer;
import io.greenscreens.quark.async.QuarkAsyncContext;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.reflection.IQuarkHandle;
import io.greenscreens.quark.util.QuarkUtil;
import io.greenscreens.quark.web.QuarkContext;
import jakarta.enterprise.inject.Vetoed;
import jakarta.servlet.AsyncContext;

/**
 * Class to execute controller bean
 */
@Vetoed
public class QuarkBeanCaller implements Runnable {

    final static private Logger LOG = LoggerFactory.getLogger(QuarkBeanCaller.class);

    final IQuarkHandle beanHandle;
    final Object[] params;

    final QuarkHandler handler;
    final boolean isVoid;
    final boolean isAsync;

    private AsyncContext context = null;
    private IDestructibleBeanInstance<?> di = null;

    public QuarkBeanCaller(final QuarkHandler handler, final IQuarkHandle handle, final Object[] params) {
        super();
        this.beanHandle = handle;
        this.handler = handler;
        this.params = params;
        this.isAsync = isAsync();
        this.isVoid = handle.isVoid();
    }

    public void call() {
        if (initialize()) {            
            if (isAsync) {
                runAsync();
            } else {
                run();
            }
        }
    }

    private void runAsync() {
        if (beanHandle.isVirtual()) {
            Thread.ofVirtual().name(beanHandle.toString()).start(this);
        } else {
            final QuarkBeanCaller caller = this;
            CompletableFuture.runAsync(caller).handle((r, e) -> {
                if (Objects.nonNull(e)) {
                    final String msg = QuarkUtil.toMessage(e);
                    LOG.error(msg);
                }
                // cleanup in case thread terminated
                caller.release(di);
                caller.release(context);
                return r;
            });
        }
    }

    @Override
    public void run() {
        try {
            handler.send(call(di));
        } catch (Throwable e) {
            QuarkUtil.printError(e, LOG);
            handler.send(e);
        } finally {
            release(di);
            release(context);
        }
    }

    // attach bean instance within servlet thread to allow injections
    private boolean initialize() {
        try {
            if (isAsync) context = handler.getContext();
            di = attach().map(scope -> scope.call(() -> beanHandle.instance())).orElse(null);
        } catch (Throwable e) {
            QuarkUtil.printError(e, LOG);
            handler.send(e);
            di = null;
            release(context);
        } finally {
            detach();
        }
        return Objects.nonNull(di);
    }

    /**
     * Attach WebSocket or Servlet context to current thread before controller
     * execution
     */
    private Optional<Carrier> attach() {

        if (Objects.nonNull(handler.getWsSession())) {
            return QuarkProducer.attachSession(handler.getWsSession());
        } else if (asAsync()) {
            return QuarkProducer.attachAsync(new QuarkAsyncContext(handler));
        } else {
            return QuarkProducer.attachRequest(QuarkContext.create(handler.getHttpRequest(), handler.getHttpResponse()));
        }

    }

    /**
     * Release thread context
     */
    private void detach() {
        // not used for ScopedVariables
    }

    private boolean asAsync() {
        return beanHandle.isAsyncArgs() && isVoid;
    }

    private boolean isAsync() {
        return handler.isSupportAsync() && beanHandle.isAsync();
    }

    private MethodHandle methodHandle() throws NoSuchMethodException, IllegalAccessException {
        return beanHandle.methodHandle();
    }

    /**
     * Safe controller bean destruction
     * 
     * @param bean
     */
    private void release(final IDestructibleBeanInstance<?> bean) {
        Optional.ofNullable(bean).ifPresent(b -> b.release());
    }

    private void release(final AsyncContext ctx) {
        Optional.ofNullable(ctx).ifPresent(c -> c.complete());
    }

    /**
     * Execute Controller bean and get response for requester
     * 
     * @param bean
     * @return
     * @throws Throwable
     * @throws NoSuchMethodException
     */
    private ExtJSResponse call(final IDestructibleBeanInstance<?> bean) throws NoSuchMethodException, Throwable {
        final Object beanInstance = bean.getInstance();
        QuarkValidator.validateParameters(beanHandle, beanInstance, params);
        final Object obj = methodHandle().invoke(beanInstance, params);
        if (asAsync())
            return null;
        return QuarkHandlerUtil.toResponse(obj, beanHandle);
    }

    /**
     * Public controller initializer
     * 
     * @param handler
     * @param bean
     * @param method
     * @param params
     * @return
     */
    public static final QuarkBeanCaller get(final QuarkHandler handler, final IQuarkHandle handle, final Object[] params) {
        return new QuarkBeanCaller(handler, handle, params);
    }

}
