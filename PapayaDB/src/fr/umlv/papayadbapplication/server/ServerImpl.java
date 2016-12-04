package fr.umlv.papayadbapplication.server;

import static java.util.Objects.requireNonNull;

import java.util.Map.Entry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class ServerImpl extends AbstractVerticle implements Server {
	/**
	 * this methods start the applicant server on port 8080 and routes the
	 * client request to the right method
	 * 
	 * @author jlilimk
	 */
	@Override
	public void start() {
		Router router = Router.router(vertx);

		// A BodyHandler to use the body of our requests
		router.route("/*").handler(BodyHandler.create());

		// route to GET REST APIs
		router.get("/").handler(this::getAllDatabases);
		router.get("/:databasename").handler(this::selectAllFromDatabase);
		router.get("/:databasename?*").handler(this::getDocumentWithFilters);

		// route to POST REST Methods
		router.post("/:databasename/:username/:password").handler(this::createNewDataBase);
		router.post("/:databasename/:documentname/*").handler(this::insertDocumentIntoDatabase);

		// route to DELETE REST Methods
		router.delete("/:databasename/:username/:password").handler(this::deleteDatabase);
		router.delete("/:databasename/:documentname/:username/:password").handler(null);

		// otherwise serve static pages
		router.route().handler(StaticHandler.create());

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept).listen(8080);
		System.out.println("listen on port 8080");
	}

	/**
	 * Displays the list of all the existing databases
	 * 
	 * @author jlilimk
	 * @param routingContext
	 *            the request given by the client
	 */
	private void getAllDatabases(RoutingContext routingContext) {
		JsonObject requestAsJson = new JsonObject().put("uri", routingContext.request().uri());
		String responseAsString = this.sendGetRequest(requestAsJson);
		if (responseAsString == null) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry, there are a database problem");
			return;
		}
		routingContext.response().putHeader("Content-Type", "application/json").end(responseAsString);
	}

	/**
	 * this method send a GET request to the data base server to get all the
	 * elements of a given data base
	 * 
	 * @param routingContext
	 *            the request given by the client
	 */
	private void selectAllFromDatabase(RoutingContext routingContext) {
		JsonObject requestAsJson = new JsonObject().put("databasename",
				routingContext.request().getParam("databasename"));
		String responseAsString = this.sendGetRequest(requestAsJson);
		if (responseAsString == null) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry, there are a database problem");
			return;
		}
		routingContext.response().putHeader("Content-Type", "application/json").end(responseAsString);
	}

	/**
	 * this method send a select request with filters to the data base server
	 * the method isn't implemented yet
	 * 
	 * @author jlilimk
	 * @param routingContext
	 * 
	 */
	private void getDocumentWithFilters(RoutingContext routingContext) {

	}

	/**
	 * this method send a post request to the data base server to create a new
	 * data base the creation of a data base need a username and a password
	 * default user = "root" and default password is "root"
	 * 
	 * @author jlilimk
	 * @param routingContext
	 */
	private void createNewDataBase(RoutingContext routingContext) {
		String username = requireNonNull(routingContext.request().getParam("username"));
		String password = requireNonNull(routingContext.request().getParam("password"));
		if (!username.equals("root") || !password.equals("root")) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry but you are not authorized to create new databases");
			return;
		}
		String databaseName = requireNonNull(routingContext.request().getParam("databasename"));
		JsonObject requestAsJson = new JsonObject().put("databasename", databaseName).put("username", username)
				.put("password", password);
		String responseAsString = this.sendPostRequest(requestAsJson);
		if (responseAsString == null) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry, there are a database problem");
			return;
		}
		routingContext.response().putHeader("Content-Type", "application/json").end(responseAsString);
	}

	/**
	 * this method send a post request to the data base server to add a document
	 * to the data base
	 * 
	 * @param routingContext
	 */
	private void insertDocumentIntoDatabase(RoutingContext routingContext) {
		String[] uriContent = routingContext.request().uri().split("/");
		String[] documentContent = uriContent[3].split("&");
		MultiMap params = routingContext.request().params();
		String[] split;
		for (String element : documentContent) {
			split = element.split("=");
			params.add(split[0], split[1]);
		}
		JsonObject requestAsJson = new JsonObject();
		for (Entry<String, String> entry : params) {
			requestAsJson.put(entry.getKey(), entry.getValue());
		}
		String responseAsString = this.sendPostRequest(requestAsJson);

		routingContext.response().putHeader("Content-Type", "application/json")
				.end("Document has been created successfully");
		if (responseAsString == null) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry, there are a database problem");
			return;
		}
	}

	/**
	 * this method send a delete request to the data base server to delete a
	 * data base the delete of a data base need a username and a password
	 * default user = "root" and default password is "root"
	 * 
	 * @author jlilimk
	 * @param routingContext
	 */
	public void deleteDatabase(RoutingContext routingContext) {
		String username = requireNonNull(routingContext.request().getParam("username"));
		String password = requireNonNull(routingContext.request().getParam("password"));
		if (!username.equals("root") || !password.equals("root")) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry but you are not authorized to delete databases");
			return;
		}
		String databaseName = requireNonNull(routingContext.request().getParam("databasename"));
		JsonObject requestAsJson = new JsonObject().put("databasename", databaseName);
		String responseAsString = this.sendDeleteRequest(requestAsJson);
		if (responseAsString == null) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry, there are a database problem");
			return;
		}
		routingContext.response().putHeader("Content-Type", "application/json").end(responseAsString);
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new ServerImpl());
	}

}