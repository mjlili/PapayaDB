package client;

import static java.util.Objects.requireNonNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class Client extends AbstractVerticle {

	@Override
	public void start() {
		Router router = Router.router(vertx);
		// router.route().handler(CookieHandler.create());
		// router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		// router.route().handler(UserSessionHandler.create(null));
		//
		// AuthHandler basicAuthHandler = BasicAuthHandler.create(null);
		// router.route("/private/*").handler(basicAuthHandler);
		// route to JSON REST APIs
		router.get("/").handler(this::displayIndexWebPage);
		router.get("/get/:name/:id").handler(this::getAdocument);

		// route to POST REST Method
		router.route().handler(BodyHandler.create());
		router.post("/add_document/:name/:id").handler(this::addJSonDocument);
		router.post("/add_documents").handler(this::addJSonDocuments);

		// otherwise serve static pages
		router.route().handler(StaticHandler.create());

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept).listen(8080);

		System.out.println("listen on port 8080");
	}

	private void pushAJsonDocument(RoutingContext routingContext, ByteBuffer byteBuffer) {
		HttpServerResponse response = routingContext.response();
		Path path = Paths.get("./DataBase/dataBase.json");
		Objects.requireNonNull(path);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream("./DataBase/dataBase.json", true);
			FileChannel fileChannel = fileOutputStream.getChannel();
			fileChannel.write(byteBuffer);
			fileChannel.write(ByteBuffer.wrap("\n".getBytes()));
			fileChannel.close();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			response.putHeader("Content-Type", "text/html").end("Fichier de base de données introuvable\n");
		} catch (IOException e) {
			e.printStackTrace();
			response.putHeader("Content-Type", "text/html").end("Erreur d'ouverture de la base de données\n");
		}
		response.putHeader("Content-type", "text/html").end("Le document est bien ajouté\n");
	}

	private void pushAJsonDocumentWithMap(RoutingContext routingContext, ByteBuffer byteBuffer) {
		HttpServerResponse response = routingContext.response();
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile("./DataBase/dataBase.json", "rw");
			FileChannel fileChannel = randomAccessFile.getChannel();
			//On peut utiliser la taille du mappedByteBuffer pour l'indice du document
			MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileChannel.size());
			fileChannel.position(fileChannel.size());
			fileChannel.write(byteBuffer);
			fileChannel.write(ByteBuffer.wrap("\n".getBytes()));
			fileChannel.close();
			randomAccessFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			response.putHeader("Content-Type", "text/html").end("Fichier de base de données introuvable\n");
		} catch (IOException e) {
			e.printStackTrace();
			response.putHeader("Content-Type", "text/html").end("Erreur d'ouverture de la base de données\n");
		}
		response.putHeader("Content-type", "text/html").end("Le document est bien ajouté\n");
	}

	private void addJSonDocument(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		HttpServerRequest request = routingContext.request();
		System.out.println(request.method().name());
		String name = requireNonNull(request.getParam("name"));
		int id = Integer.parseInt(request.getParam("id"));
		if (name.isEmpty() || id < 0) {
			response.setStatusCode(404).end();
			return;
		}

		byte data[] = Json.encodePrettily(Map.of("id", "" + id, "name", name)).getBytes();
		Path path = Paths.get("./DataBase/dataBase.json");
		Objects.requireNonNull(path);
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream("./DataBase/dataBase.json", true);
			FileChannel fileChannel = fileOutputStream.getChannel();
			fileChannel.write(byteBuffer);
			fileChannel.write(ByteBuffer.wrap("\n".getBytes()));
			fileChannel.close();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		response.putHeader("Content-Type", "application/json").end("Document added successfully.");
	}

	private void displayIndexWebPage(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "text/html").sendFile("index.html");
	}

	private void getAdocument(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		HttpServerRequest request = routingContext.request();
		String name = requireNonNull(request.getParam("name"));
		int id = Integer.parseInt(request.getParam("id"));
		if (name.isEmpty() || id < 0) {
			response.setStatusCode(404).end();
			return;
		}
		routingContext.response().putHeader("content-type", "application/json")
				.end(Json.encodePrettily(Map.of("id", "" + id, "name", name)));
	}

	private void addJSonDocuments(RoutingContext routingContext) {
		JsonObject jsonObject = requireNonNull(routingContext.getBodyAsJson());
		ByteBuffer byteBuffer = ByteBuffer.wrap(Json.encodePrettily(jsonObject).getBytes());
		pushAJsonDocumentWithMap(routingContext, byteBuffer);
		// pushAJsonDocument(routingContext, byteBuffer);
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new Client());
	}
}