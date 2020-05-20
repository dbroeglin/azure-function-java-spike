package fr.broeglin.azure.functions;

import java.io.File;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobItem;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    public static final String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=e4bbe6fe3e15421a9d5f;AccountKey=xnwX66Vc5oPozZuL1+skX4JYJEd09Rgokchw+hOLka1qwAQcxMdCusS41unJ9V2k8yG3e0VJL9InHw4ox8jklA==;EndpointSuffix=core.windows.net";

    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);

        try {
            File sourceFile = null;


            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(storageConnectionString)
                .buildClient();

            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient("quickstartcontainer");
            if(!blobContainerClient.exists()) {
                blobContainerClient.create();
            }


			sourceFile = File.createTempFile("sampleFile", ".txt");
			System.out.println("Creating a sample file at: " + sourceFile.toString());
			Writer output = new BufferedWriter(new FileWriter(sourceFile));
			output.write("Hello Azure!");
			output.close();

            BlobClient blockBlobClient = blobContainerClient.getBlobClient(sourceFile.getName());

			//Creating blob and uploading file to it
			System.out.println("Uploading the sample file ");
            blockBlobClient.uploadFromFile(sourceFile.getAbsolutePath());

			//Listing contents of container
            for (BlobItem blobItem : blobContainerClient.listBlobs()) {
                System.out.println("Found blob name: " + blobItem.getName());
            }
            
        } catch (Exception e) {
            System.out.println("Storage exception occured: " + e.getMessage());
        }

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }
}
