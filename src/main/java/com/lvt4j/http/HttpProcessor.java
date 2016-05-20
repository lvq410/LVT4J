package com.lvt4j.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.Servlet;

import com.lvt4j.basic.TLog;
import com.lvt4j.office.TWeb;

/**
 * @author LV
 */
public class HttpProcessor extends Thread {

    private HttpServer server;
    private Socket socket;
    private HttpRequest request;
    private HttpResponse response;

    public HttpProcessor(HttpServer server,Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            request = new HttpRequest(socket);
        } catch (Exception e) {
            TLog.w("Parse request fail", e);
            try {
                socket.close();
            } catch (IOException e1) {
                TLog.w("Failed on close socket", e);
            }
            return;
        }
        try {
            response = new HttpResponse(socket,request);
        } catch (Exception e) {
            TLog.w("Build response fail", e);
            try {
                socket.close();
            } catch (IOException e1) {
                TLog.w("Failed on close socket", e);
            }
            return;
        }
        
        if ("".equals(request.getRequestURI()) || "/".equals(request.getRequestURI())) {
            request.setRequestURI(server.getWelcomeURI());
        }
        
        try {
            for (Entry<Servlet,Set<String>> servlet: server.getServlets().entrySet()) {
                for (String urlPattern: servlet.getValue()) {
                    if (request.getRequestURI().matches(urlPattern)) {
                        servlet.getKey().service(request, response);
                        response.finishResponse();
                        socket.close();
                        socket = null;
                        return;
                    }
                }
            }
            sendStaticResource();
            socket.close();
            socket = null;
        } catch (Exception e) {
            TLog.e("Error on handle http", e);
        } finally {
            try {
                if (socket!=null) {
                    socket.close();
                }
            } catch (IOException e) {
                TLog.w("Failed on close socket", e);
            }
        }
    }
    
    private void sendStaticResource() {
        byte[] bytes = new byte[1024];
        FileInputStream fis = null;
        try {
            File file = new File(server.getWebRoot(),
                    request.getRequestURI());
            fis = new FileInputStream(file);
            String[] tmpStrs = file.getName().split("[.]");
            String exFileName = tmpStrs[tmpStrs.length-1];
            response.setContentType(TWeb.getContentType(exFileName));
            response.setContentLength((int) file.length());
            if (TWeb.DefaultContentType.equals(response.getContentType())) {
                response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            }
            int ch = fis.read(bytes, 0, 1024);
            while (ch != -1) {
                response.write(bytes, 0, ch);
                ch = fis.read(bytes, 0, 1024);
            }
        } catch (FileNotFoundException e) {
            try {
                response.setStatus(404, "File Not Found");
                response.setContentType("text/html");
                response.setContentLength(23);
                response.write("<h1>File Not Found</h1>".getBytes());
            } catch (IOException e1) {
                // ignore
                TLog.w("Can't send 404 info", e1);
            }
        }catch (Exception e) {
            TLog.e("Can't sendStaticResource", e);
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (Exception e2) {
                    // ignore
                    TLog.w("Can't close FileInputStream", e2);
                }
            }
        }
    }
}
