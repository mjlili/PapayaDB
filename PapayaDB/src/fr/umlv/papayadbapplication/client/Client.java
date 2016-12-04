package fr.umlv.papayadbapplication.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface Client {

	public void displayApplicationMenu();

	public void dispatchRequest(String clientRequest);

	/**
	 * creates a uri from the client request and send a GET request to the
	 * applicant server. prints the result on the console
	 * 
	 * @param the
	 *            client request as a string
	 * @throws IOException
	 *             or InterruptedException if the server is not available
	 */
	public default void sendGetRequest(String clientRequest) {
		try {
			HttpResponse httpResponse = HttpRequest.create(URI.create("http://localhost:8080" + clientRequest))
					.setHeader("Content-Type", "application/json").GET().response();
			System.out.println(httpResponse.body(HttpResponse.asString()));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * creates a uri from the client request and send a POST request to the
	 * applicant server. prints the result on the console
	 * 
	 * @param the
	 *            client request as a string
	 * @throws IOException
	 *             or InterruptedException if the server is not available
	 */
	public default void sendPostRequest(String clientRequest) {
		try {
			HttpResponse httpResponse = HttpRequest.create(URI.create("http://localhost:8080" + clientRequest))
					.setHeader("Content-Type", "application/json").POST().response();
			System.out.println(httpResponse.body(HttpResponse.asString()));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * creates a uri from the client request and send a PUT request to the
	 * applicant server. prints the result on the console
	 * 
	 * @param the
	 *            client request as a string
	 * @throws IOException
	 *             or InterruptedException if the server is not available
	 */
	public default void sendUpdateRequest(String clientRequest) {
		try {
			HttpResponse httpResponse = HttpRequest.create(URI.create("http://localhost:8080" + clientRequest))
					.setHeader("Content-Type", "application/json").PUT().response();
			System.out.println(httpResponse.body(HttpResponse.asString()));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * creates a uri from the client request and send a DELETE request to the
	 * applicant server. prints the result on the console
	 * 
	 * @param the
	 *            client request as a string
	 * @throws IOException
	 *             or InterruptedException if the server is not available
	 */
	public default void sendDeleteRequest(String clientRequest) {
		try {
			HttpResponse httpResponse = HttpRequest.create(URI.create("http://localhost:8080" + clientRequest))
					.setHeader("Content-Type", "application/json").method("DELETE").response();
			System.out.println(httpResponse.body(HttpResponse.asString()));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}