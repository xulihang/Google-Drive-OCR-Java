package com.xulihang.googledrive;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;


/**
 * Hello world!
 *
 */
public class App 
{    
	
    private static Server server;
    public static void main( String[] args ) throws Exception 
    {
    	int port = 8090;
    	if (args.length==1){
    		port=Integer.valueOf(args[0]);
    	}
    	server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new Connector[] {connector});
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(OCRServlet.class, "/ocr");
        server.setHandler(servletHandler);
        server.start();
    }
}
