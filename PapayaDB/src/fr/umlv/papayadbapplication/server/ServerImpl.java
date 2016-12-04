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
	 * @author jlilimk
	 */
	@Override
	public void start() {
		Router router = Router.router(vertx);

		// A BodyHandler to use the body of our requests
		router.route("/*").handler(BodyHandler.create());

		// route to GET REST APIs
		router.get("/").handler(this::getAllDatabases);
		router.get("/:databasename?*").handler(this::getDocumentWithFilters);

		// route to POST REST Methods
		router.post("/:databasename").handler(this::createNewDataBase);
		router.post("/:databasename/:documentname/*").handler(this::insertDocumentIntoDatabase);

		// route to DELETE REST Methods
		router.delete("/:databasename").handler(null);
		router.delete("/:databasename/:documentname").handler(null);

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
	 * @author jlilimk
	 * @param routingContext
	 */
	private void getDocumentWithFilters(RoutingContext routingContext) {

	}

	/**
	 * @author jlilimk
	 * @param routingContext
	 */
	private void createNewDataBase(RoutingContext routingContext) {
		String databaseName = requireNonNull(routingContext.request().getParam("databasename"));
		JsonObject requestAsJson = new JsonObject().put("databasename", databaseName);
		String responseAsString = this.sendPostRequest(requestAsJson);

		if (responseAsString == null) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry, there are a database problem");
			return;
		}
		routingContext.response().putHeader("Content-Type", "application/json").end(responseAsString);
	}

	/**
	 * @author jlilimk
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

	
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new ServerImpl());
	}

}
