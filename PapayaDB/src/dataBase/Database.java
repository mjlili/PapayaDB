package dataBase;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class Database extends AbstractVerticle {

	/**
	 * @author jlilimk
	 */
	@Override
	public void start() {
		Router router = Router.router(vertx);

		// A BodyHandler to use the body of our requests
		router.route("/*").handler(BodyHandler.create());

		// route to GET REST APIs
		router.get("/").handler(null);
		router.get("/:databasename?*").handler(null);

		// route to POST REST Methods
		router.post("/:databasename").handler(null);
		router.post("/:databasename/:documentname").handler(null);
		router.post("/:databasename/:documentname/*").handler(null);

		// route to DELETE REST Methods
		router.delete("/:databasename").handler(null);
		router.delete("/:databasename/:documentname").handler(null);

		// otherwise serve static pages
		router.route().handler(StaticHandler.create());

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept).listen(3306);
		System.out.println("listen on port 3306");
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new Database());
	}
}
