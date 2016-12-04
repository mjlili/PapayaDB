package fr.umlv.papayadbapplication.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import io.vertx.core.json.JsonObject;

public interface Server {
	/**
	 * creates the uri request from the json request and send a Get request to
	 * the data base server
	 * 
	 * @param requestAsJson
	 *            - the applicant server request to be send to the data base
	 *            server
	 * @return the data base's response
	 * @throws IOException
	 *             or InterruptedException if the data base server is not
	 *             available
	 */
	public default String sendGetRequest(JsonObject requestAsJson) {
		try {
			HttpResponse httpResponse = HttpRequest.create(URI.create("http://localhost:3306/select"))
					.setHeader("Content-Type", "application/json")
					.body(HttpRequest.fromString(requestAsJson.encodePrettily())).GET().response();
			return httpResponse.body(HttpResponse.asString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * creates the uri request from the json request and send a POST request to
	 * the data base server
	 * 
	 * @param requestAsJson
	 *            - the applicant server request to be send to the data base
	 *            server
	 * @return the data base's response
	 * @throws IOException
	 *             or InterruptedException if the data base server is not
	 *             available
	 */
	public default String sendPostRequest(JsonObject requestAsJson) {
		try {
			HttpResponse httpResponse = HttpRequest.create(URI.create("http://localhost:3306/insert"))
					.setHeader("Content-Type", "application/json")
					.body(HttpRequest.fromString(requestAsJson.encodePrettily())).POST().response();
			return httpResponse.body(HttpResponse.asString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * creates the uri request from the json request and send a PUT request to
	 * the data base server
	 * 
	 * @param requestAsJson
	 *            - the applicant server request to be send to the data base
	 *            server
	 * @return the data base's response
	 * @throws IOException
	 *             or InterruptedException if the data base server is not
	 *             available
	 */
	public default String sendUpdateRequest(JsonObject requestAsJson) {
		try {
			HttpResponse httpResponse = HttpRequest.create(URI.create("http://localhost:3306/update"))
					.setHeader("Content-Type", "application/json")
					.body(HttpRequest.fromString(requestAsJson.encodePrettily())).PUT().response();
			return httpResponse.body(HttpResponse.asString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * creates the uri request from the json request and send a DELETE request
	 * to the data base server
	 * 
	 * @param requestAsJson
	 *            - the applicant server request to be send to the data base
	 *            server
	 * @return the data base's response
	 * @throws IOException
	 *             or InterruptedException if the data base server is not
	 *             available
	 */
	public default String sendDeleteRequest(JsonObject requestAsJson) {
		try {
			HttpResponse httpResponse = HttpRequest.create(URI.create("http://localhost:3306/delete"))
					.setHeader("Content-Type", "application/json")
					.body(HttpRequest.fromString(requestAsJson.encodePrettily())).method("DELETE").response();
			return httpResponse.body(HttpResponse.asString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

}