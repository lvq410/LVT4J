package com.lvt4j.http;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;

import com.lvt4j.basic.TLog;

/**
 * @author LV
 */
public class HttpServer extends Thread{
    
    private String webRoot = System.getProperty("user.dir");
    private int port;
    private Map<Servlet, Set<String>> servlets = new HashMap<Servlet, Set<String>>();
    
    private String welcomeURI;
    
    private boolean open = true;
    @Override
    public void run() {
        open = true;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            TLog.i("Open http server in port 8080.");
        } catch (Exception e) {
            TLog.i("Can't open http server in port 8080,change...");
            try {
                serverSocket = new ServerSocket();
                TLog.i("Open http server in port "+serverSocket.getLocalPort()+".");
            } catch (Exception e1) {
                System.exit(1);
            }
        }
        while (open) {
            try {
                Socket socket = serverSocket.accept();
                if (socket!=null) {
                    new HttpProcessor(this,socket).start();
                }
            } catch (Exception e) {
                TLog.w("Get client socket fail.", e);
                continue;
            }
        }
    }
    
    public void open() {
        start();
    }
    
    public void close() {
        open = false;
        interrupt();
    }
    
    public String getWebRoot() {
        return webRoot;
    }
    
    public int getPort() {
        return port;
    }
    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public void setWelcomeURI(String welcomeURI) {
        this.welcomeURI = welcomeURI;
    }
    
    public String getWelcomeURI() {
        return welcomeURI;
    }
    
    public void registerServlet(String urlPattern, Servlet servlet) {
        Set<String> urlPatterns = servlets.get(servlet);
        if (urlPatterns==null) {
            urlPatterns = new HashSet<String>();
            servlets.put(servlet, urlPatterns);
        }
        urlPatterns.add(urlPattern);
    }
    
    protected Map<Servlet, Set<String>> getServlets() {
        return servlets;
    }
}
