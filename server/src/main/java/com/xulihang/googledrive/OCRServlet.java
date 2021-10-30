package com.xulihang.googledrive;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;


public class OCRServlet extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static Drive googleservice;
	/**
	 * Global instance of the scopes required by this quickstart.
	 * If modifying these scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
	private static String CREDENTIALS_FILE_PATH = "credentials.json";
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.getWriter().write("Please post");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        NetHttpTransport HTTP_TRANSPORT;
        Credential creds;
        String text;
        response.setContentType("text/plain");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			creds = getCredentials(HTTP_TRANSPORT);
			String imagePath = new Date().getTime()+".jpg";
			java.io.File targetFile = new java.io.File(imagePath);
		    java.nio.file.Files.copy(
		      request.getInputStream(), 
		      targetFile.toPath(), 
		      StandardCopyOption.REPLACE_EXISTING);
            
			googleservice = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, creds)
	                .setApplicationName(APPLICATION_NAME)
	                .build();
	        String fileID = uploadImageAsDocument(imagePath);
	    	text = downloadExtractedText(fileID);
	    	deleteFile(fileID);
	    	targetFile.delete();
	    	text=text.replace("________________\n\n", "");
	    	System.out.println(text);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			response.setStatus(500);
			response.getWriter().write(e.getMessage());
			return;
		}
        response.setStatus(200);
		response.getWriter().write(text);
    }
	
	
	
	/**
	 * Creates an authorized Credential object.
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws Exception 
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
	    // Load client secrets.
	    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));
	    FileDataStoreFactory dataStore = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
	    
	    // Build flow and trigger user authorization request.
	    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
	            .setDataStoreFactory(dataStore)
	            .setAccessType("offline")
	            .build();
	    Credential creds = flow.loadCredential("user");
	   
        if (creds == null){
        	throw new Exception("No creds found.");
        }else if (creds.getExpirationTimeMilliseconds()<new Date().getTime()){
        	creds.refreshToken();
        	if (creds.getExpirationTimeMilliseconds()<new Date().getTime()){
        		throw new Exception("Creds expired");	
        	}
	    }
	    return creds;
	}
	
	private static String uploadImageAsDocument(String imagePath) throws IOException, GeneralSecurityException{
		
			java.io.File imageFile = new java.io.File(imagePath);
			File fileMetadata = new File();
			fileMetadata.setName(imageFile.getName());
			fileMetadata.setMimeType("application/vnd.google-apps.document");
			FileContent mediaContent = new FileContent("image/jpeg", imageFile);
			File file = googleservice.files().create(fileMetadata, mediaContent)
				.setFields("id")
				.execute();
			return file.getId();
	}
	
	private static String downloadExtractedText(String id) throws IOException{
		InputStream inputStream = googleservice.files().export(id, "text/plain").executeMedia().getContent();
		String text = new BufferedReader(
			      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
			        .lines()
			        .collect(Collectors.joining("\n"));
		return text;
		
	}
	
	private static void deleteFile(String id) throws IOException{
		googleservice.files().delete(id).execute();
	}
}
