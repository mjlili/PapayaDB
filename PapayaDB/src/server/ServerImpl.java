package server;

import static java.util.Objects.requireNonNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import dataBase.DataBaseCrud;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class ServerImpl extends AbstractVerticle implements Server, DataBaseCrud {
	@Override
	public void start() {
		Router router = Router.router(vertx);
		// A BodyHandler to use the body of our requests
		router.route("/*").handler(BodyHandler.create());

		// route to JSON REST APIs
		router.get("/").handler(this::displayIndexWebPage);
		router.get("/").handler(this::getAllDocuments);

		// route to POST REST Method
		router.post("/add_documents").handler(this::createDocument);
		router.post("/:name/:id").handler(this::createDocumentSimple);
		router.post("/test").handler(this::createDocumentSimple);

		// otherwise serve static pages
		router.route().handler(StaticHandler.create());

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept).listen(8080);
		System.out.println("listen on port 8080");
	}

	public void displayIndexWebPage(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "text/html").sendFile("index.html");
	}

	private void getAllDocuments(RoutingContext routingContext) {
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile("./DataBase/dataBase.json", "r");
			FileChannel fileChannel = randomAccessFile.getChannel();
			MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
			StringBuilder str = new StringBuilder();
			for (int i = 0; i < fileChannel.size(); i++) {
				str.append((char) mappedByteBuffer.get());
			}
			routingContext.response().putHeader("content-type", "application/json").end(str.toString());
			fileChannel.close();
			randomAccessFile.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			routingContext.response().putHeader("Content-Type", "text/html")
					.end("Fichier de base de données introuvable\n");
		} catch (IOException e) {
			e.printStackTrace();
			routingContext.response().putHeader("Content-Type", "text/html")
					.end("Erreur d'ouverture de la base de données\n");
		}

	}

	private void pushAJsonDocumentSimple(RoutingContext routingContext, ByteBuffer byteBuffer) {
		HttpServerResponse response = routingContext.response();
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
			// On peut utiliser la taille du mappedByteBuffer pour l'indice du
			// document
			fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileChannel.size());
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
		response.putHeader("Content-type", "text/html").end("Le document est bien ajoute\n");
	}

	public void createDocument(RoutingContext routingContext) {
		JsonObject jsonObject = requireNonNull(routingContext.getBodyAsJson());
		ByteBuffer byteBuffer = ByteBuffer.wrap(Json.encodePrettily(jsonObject).getBytes());
		pushAJsonDocumentWithMap(routingContext, byteBuffer);
	}

	public void createDocumentSimple(RoutingContext routingContext) {
		// HttpServerResponse response = routingContext.response();
		// HttpServerRequest request = routingContext.request();
		// // String name = requireNonNull(request.getParam("name"));
		// int id = Integer.parseInt(request.getParam("id"));
		// if (name.isEmpty() || id < 0) {
		// response.setStatusCode(404).end();
		// return;
		// }
		// ByteBuffer byteBuffer =
		// ByteBuffer.wrap(Json.encodePrettily(Map.of("id", "" + id, "name",
		// name)).getBytes());
		String[] paramSplits = routingContext.getBodyAsString().split("&");
		String[] valueSplits;
		Map<String, String> params = new HashMap<String, String>();
		if (paramSplits.length > 1) {
			for (String param : paramSplits) {
				valueSplits = param.split("=");
				if (valueSplits.length > 1) {
					params.put(requireNonNull(valueSplits[0]), requireNonNull(valueSplits[1]));
				}
			}
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(Json.encodePrettily(params).getBytes());
		pushAJsonDocumentWithMap(routingContext, byteBuffer);
		// Il faut trouver une solution pour le message d'erreur ISE Response
		// already written !!!
		try {
			routingContext.response().putHeader("Content-Type", "application/json").end("Document added successfully");
			routingContext.response().close();
		} catch (IllegalStateException e) {
			System.out.println("Une erreur est survenue");
		}
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new ServerImpl());
	}
}
