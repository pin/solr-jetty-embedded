package org.popov;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.nio.*;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.*;
import org.eclipse.jetty.webapp.*;

// based on http://steveliles.github.io/setting_up_embedded_jetty_8_and_spring_mvc_with_maven.html
public class WebServer {
	private static final String LOG_PATH = "./var/logs/access/yyyy_mm_dd.request.log";
	private static final String WEB_XML = "WEB-INF/web.xml";
	private static final Logger log = Log.getLogger(WebServer.class);

	public static interface WebContext {
		public File getWarPath();
		public String getContextPath();
	}

	private Server server;
	private int port;
	private String bindInterface;

	public WebServer(int port) {
		this(port, null);
	}

	public WebServer(int port, String bindInterface) {
		this.port = port;
		this.bindInterface = bindInterface;
	}

	public void start() throws Exception {
		server = new Server();
		server.setThreadPool(createThreadPool());
		server.addConnector(createConnector());
		server.setHandler(createHandlers());
		server.setStopAtShutdown(true);
		server.start();
	}

	public void join() throws InterruptedException {
		server.join();
	}

	public void stop() throws Exception {
		server.stop();
	}

	private ThreadPool createThreadPool() {
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMinThreads(8);
		threadPool.setMaxThreads(32);
		return threadPool;
	}

	private SelectChannelConnector createConnector() {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		connector.setHost(bindInterface);
		return connector;
	}

	private HandlerCollection createHandlers() {
		WebAppContext ctx = new WebAppContext();
		ctx.setContextPath("/");
		ctx.setWar(getShadedWarUrl());
		log.info("shaded war URL: " + getShadedWarUrl());
		ctx.addOverrideDescriptor(getShadedWarUrl() + "override-web.xml");

		List<Handler> handlers = new ArrayList<Handler>();
		handlers.add(ctx);

		HandlerList contexts = new HandlerList();
		contexts.setHandlers(handlers.toArray(new Handler[0]));

		RequestLogHandler log = new RequestLogHandler();
		log.setRequestLog(createRequestLog());

		HandlerCollection result = new HandlerCollection();
		result.setHandlers(new Handler[] { contexts, log });

		return result;
	}

	private RequestLog createRequestLog() {
		NCSARequestLog log = new NCSARequestLog();
		File logPath = new File(LOG_PATH);
		logPath.getParentFile().mkdirs();
		log.setFilename(logPath.getPath());
		log.setRetainDays(90);
		log.setExtended(false);
		log.setAppend(true);
		log.setLogTimeZone("GMT");
		log.setLogLatency(true);
		return log;
	}

	private URL getResource(String resource) {
		return Thread.currentThread().getContextClassLoader().getResource(resource);
	}

	private String getShadedWarUrl() {
		String urlStr = getResource(WEB_XML).toString();
		return urlStr.substring(0, urlStr.length() - 15); // Strip off "WEB-INF/web.xml"
	}
}
