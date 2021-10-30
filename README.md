# Google-Drive-OCR-Java

A Java client using Google Drive v3 API to OCR images.

There two versions: command line version and server version.

## Command Line

Process:

1. Run the program.
2. If there is not a valid access token stored in the disk, the program will direct users to a OAuth consent page using the system's browser to authorize the program to have access to his Google Drive.
3. Upload an images and set its mimetype as `application/vnd.google-apps.document`.
4. Download the extracted text.

Usage:

```
java -jar google_drive_ocr.jar [optional]imagePath [optional]outputTxtPath [optional]credentialsPath
```

## Server

Ther HTTP server receives requests and returns the extracted text. It is designed to run on a public server. It does not open the browser for oauth. The tokens should be managed manually.

Usage:

```
java -jar google_drive_ocr.jar [optional]port
```

## How to get credentials.json

Check out Google's official docs: <https://developers.google.com/drive/api/v3/quickstart/java#prerequisites>

## References

<https://github.com/hrishikeshrt/google_drive_ocr>