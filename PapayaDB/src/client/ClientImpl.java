package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class ClientImpl implements Client {
	private final String host;
	private final HttpClient httpClient;

	/**
	 * @author jlilimk
	 * @param httpClient
	 */
	public ClientImpl(HttpClient httpClient) {
		this.httpClient = httpClient;
		this.host = "http://localhost:8080";
	}

	/**
	 * @author jlilimk
	 * @param uri
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void createDocumentUri(String uri) throws IOException, InterruptedException {
		URI completeUri;
		try {
			completeUri = new URI(host + uri);
			Builder httpRequestBuilder = httpClient.request(completeUri);
			HttpResponse httpResponse = httpRequestBuilder.setHeader("Content-Type", "application/json").POST()
					.response();
			System.out.println(httpResponse.body(HttpResponse.asString()));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.out.println("Invalid URI");
		}
	}

	/**
	 * @author jlilimk
	 * @param uri
	 * @throws IOException
	 */
	public void createDocumentUrl(String uri) throws IOException {
		URL completeUrl;
		completeUrl = new URL(host + uri);
		HttpURLConnection httpUrlConnection = (HttpURLConnection) completeUrl.openConnection();
		httpUrlConnection.setRequestMethod("POST");
		httpUrlConnection.setDoOutput(true);
		String urlParameters = "name=Mohamed Kacem&id=11&age=26&nationalite=tunisienne";
		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
		httpUrlConnection.addRequestProperty("Content-Type", "application/json");
		httpUrlConnection.addRequestProperty("Content-Length", Integer.toString(urlParameters.length()));
		httpUrlConnection.setRequestProperty("Accept", "application/json");
		DataOutputStream wr = new DataOutputStream(httpUrlConnection.getOutputStream());
		wr.write(postData);

		BufferedReader bufferdReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
		String outPut;
		System.out.println("Response from the Server : ");
		while ((outPut = bufferdReader.readLine()) != null) {
			System.out.println(outPut);
		}
		httpUrlConnection.getInputStream().reset();
		httpUrlConnection.disconnect();
	}

	/**
	 * @author jlilimk
	 */
	public void getAllDocuments() {
		try {
			URL getAll = new URL("http://localhost:8080/");
			HttpURLConnection conn = (HttpURLConnection) getAll.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("url invalide");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author jlilimk
	 */
	@Override
	public void displayApplicationMenu() {
		System.out.println("*** Welcome on the PapayaDB Application ***");
		System.out.println("We present to you our features :");
		System.out.println("#####################################################");
		System.out.println("#  Note that your request will be case sensitive !  #");
		System.out.println("#####################################################");
		System.out.println("To create a new database please enter : createNewDataBase /:databasename");
		System.out.println("To select another database please enter : selectDatabase /");
		System.out.println("To delete a specific database please enter : deleteExistingDataBase /:databasename");
		System.out.println(
				"To insert a new document into a database please enter : insertDocumentIntoDatabase /:databasename/:documentname/key1=value1&key2=value2...");
		System.out.println(
				"To select a specific document please enter : selectDocumentFromDatabase /:databasename?filter1=value1&filter2=value2...");
		System.out.println(
				"To delete a specific document please enter : deleteDocumentFromDatabase /:databasename/:documentname");
	}

	/**
	 * @author jlilimk
	 */
	@Override
	public void dispatchRequest(String clientRequest) {
		String[] splitClientRequest = clientRequest.split(" ");
		StringBuilder uriAsString = new StringBuilder();
		Arrays.stream(splitClientRequest).skip(1).forEach(element -> uriAsString.append(element));
		String finalClientRequestAsString = uriAsString.toString();
		switch (splitClientRequest[0]) {
		case "createNewDataBase":
			this.sendPostRequest(finalClientRequestAsString);
			break;
		case "selectDatabase":
			this.sendGetRequest(finalClientRequestAsString);
			break;
		case "deleteExistingDataBase":
			this.sendDeleteRequest(finalClientRequestAsString);
			break;
		case "insertDocumentIntoDatabase":
			this.sendPostRequest(finalClientRequestAsString);
			break;
		case "selectDocumentFromDatabase":
			this.sendGetRequest(finalClientRequestAsString);
			break;
		case "deleteDocumentFromDatabase":
			this.sendDeleteRequest(finalClientRequestAsString);
			break;
		default:
			System.out.println("Sorry we cannot answer your request");
			displayApplicationMenu();
			break;
		}
	}

	public static void main(String[] args) throws IOException {
		Client papayaClient = new ClientImpl(HttpClient.getDefault());
		papayaClient.displayApplicationMenu();
		Scanner requestScanner = new Scanner(System.in);
		System.out.println("Enter a choice please :");
		String request = requestScanner.nextLine();
		while (!request.contentEquals("quit")) {
			papayaClient.dispatchRequest(request);
			System.out.println("Enter a choice please :");
			request = requestScanner.nextLine();
			if (request.contentEquals("menu")) {
				Runtime.getRuntime().exec("cls");
				papayaClient.displayApplicationMenu();
				System.out.println("Enter a choice please :");
				request = requestScanner.nextLine();
			}
		}
		requestScanner.close();
		System.out.println("The job is done!");
	}
}