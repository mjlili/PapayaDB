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

import dataBase.DataBaseCrud;

public class ClientImpl implements Client, DataBaseCrud {
	private final String host;
	private final HttpClient httpClient;

	public ClientImpl(HttpClient httpClient) {
		this.httpClient = httpClient;
		this.host = "http://localhost:8080";
	}

	@Override
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

	@Override
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
		httpUrlConnection.disconnect();
	}

	@Override
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

	public static void main(String[] args) throws IOException, InterruptedException {
		Client papayaClient = new ClientImpl(HttpClient.getDefault());
		// papayaClient.createDocumentUri("/Duris/1000");
		papayaClient.createDocumentUrl("/test");
		papayaClient.getAllDocuments();
	}
}