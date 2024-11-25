package com.xulihang.googledrive;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
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

/**
 * Hello world!
 *
 */
public class App 
{    
	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static Drive service;
	/**
	 * Global instance of the scopes required by this quickstart.
	 * If modifying these scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
	private static String CREDENTIALS_FILE_PATH = "credentials.json";
	
	
	/**
	 * Creates an authorized Credential object.
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
	    // Load client secrets.
	    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));
	    FileDataStoreFactory dataStore = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
	    
	    // Build flow and trigger user authorization request.
	    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
	            .setDataStoreFactory(dataStore)
	            .setAccessType("offline")
	            .build();
	    
	    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8081).build();
	    
	    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}
	
	private static String uploadImageAsDocument(String imagePath) throws IOException, GeneralSecurityException{
		
			java.io.File imageFile = new java.io.File(imagePath);
			File fileMetadata = new File();
			fileMetadata.setName(imageFile.getName());
			fileMetadata.setMimeType("application/vnd.google-apps.document");
			FileContent mediaContent = new FileContent("image/jpeg", imageFile);
			File file = service.files().create(fileMetadata, mediaContent)
				.setFields("id")
				.execute();
			return file.getId();
	}
	
	private static String downloadExtractedText(String id) throws IOException{
		InputStream inputStream = service.files().export(id, "text/plain").executeMedia().getContent();
		String text = new BufferedReader(
			      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
			        .lines()
			        .collect(Collectors.joining("\n"));
		return text;
		
	}
	

	private static void writeFileWithBufferedWriter(String text, String outputPath) throws IOException {
			FileOutputStream writerStream = new FileOutputStream(outputPath);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8")); 
	        writer.write(text);
	        writer.close();
	    }
	
	
    public static void main( String[] args ) throws IOException, GeneralSecurityException
    {
    	String outputPath = "out.txt";
    	String imagePath = "image.jpg";
    	if (args.length>1){
    		try {
        		imagePath = args[0];
        		outputPath = args[1];
        		CREDENTIALS_FILE_PATH = args[2];
        	} catch (Exception e) {
        		System.out.println("Usage: imagePath ouputPath credentialsPath");
        		return;
        	}   	
        		
    	}
    	try {
    		OCR(imagePath,outputPath);
    	}catch (Exception e) {
    		e.printStackTrace();
    		if (e instanceof TokenResponseException) {
    			deleteExpiredToken();
        		OCR(imagePath,outputPath);
    		}
    	}
    }
    
    private static void OCR(String imagePath,String outputPath) throws GeneralSecurityException, IOException{
    	final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        String fileID = uploadImageAsDocument(imagePath);
    	String text = downloadExtractedText(fileID);
    	text=text.replace("________________\n\n", "");
    	writeFileWithBufferedWriter(text,outputPath);
    	System.out.println(text);
    }
    
    private static void deleteExpiredToken(){
    	java.io.File file = new java.io.File(TOKENS_DIRECTORY_PATH,"StoredCredential");
    	if (file.exists()) {
    		file.delete();
    	}
    }
}
