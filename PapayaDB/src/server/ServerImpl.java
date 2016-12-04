package server;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
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
	 * @author jlilimk
	 * @param routingContext
	 */
	private void getDocumentWithFilters(RoutingContext routingContext) {
		
	}

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
		pushAJsonDocumentWithMap(requestAsJson);
		routingContext.response().putHeader("Content-Type", "application/json")
				.end("Document has been created successfully");
	}

	/**
	 * Insert a document into a specified database using the filechannel.map()
	 * method
	 * 
	 * @author jlilimk
	 * @param routingContext
	 */
	private boolean associateDocumentToDatabase(JsonObject requestAsJson) {
		try {
			RandomAccessFile randomAccessDatabaseIndexFile = new RandomAccessFile("./Database/database_index.json",
					"rw");
			FileChannel databaseIndexFileChannel = randomAccessDatabaseIndexFile.getChannel();
			databaseIndexFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, databaseIndexFileChannel.size());
			databaseIndexFileChannel.position(databaseIndexFileChannel.size());
			databaseIndexFileChannel.write(
					ByteBuffer.wrap(Json.encodePrettily(Map.of("databasename", requestAsJson.getString("databasename"),
							"documentname", requestAsJson.getString("documentname"))).getBytes()));
			databaseIndexFileChannel.write(ByteBuffer.wrap("\n".getBytes()));
			databaseIndexFileChannel.close();
			randomAccessDatabaseIndexFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Display the list of all the existing databases
	 * 
	 * @author jlilimk
	 * @param routingContext
	 */
	private void getAllDatabases(RoutingContext routingContext) {

		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile("./Database/database_index.json", "r");
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

	/**
	 * @author jlilimk
	 * @param routingContext
	 * @param byteBuffer
	 */
	private boolean pushAJsonDocumentWithMap(JsonObject requestAsJson) {
		try {
			RandomAccessFile randomAccessDatabaseFile = new RandomAccessFile(
					"./Database/" + requestAsJson.getString("databasename") + ".json", "rw");
			FileChannel databaseFileChannel = randomAccessDatabaseFile.getChannel();
			databaseFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, databaseFileChannel.size());
			databaseFileChannel.position(databaseFileChannel.size());
			databaseFileChannel.write(ByteBuffer.wrap(Json.encodePrettily(requestAsJson).getBytes()));
			databaseFileChannel.write(ByteBuffer.wrap("\n".getBytes()));
			databaseFileChannel.close();
			randomAccessDatabaseFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (associateDocumentToDatabase(requestAsJson)) {
			return true;
		}
		return false;
	}

	/**
	 * @author jlilimk
	 * @param routingContext
	 */
	private void createNewDataBase(RoutingContext routingContext) {
		String databaseName = requireNonNull(routingContext.request().getParam("databasename"));
		File databaseDirectory = new File("./Database/");
		File[] filesList = requireNonNull(databaseDirectory.listFiles());
		if (Arrays.stream(filesList).filter(file -> file.getName().equals(databaseName + ".json")).count() != 0) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry but this database name is already in use");
			return;
		}
		File newDatabase = new File("./Database/" + databaseName + ".json");
		try {
			newDatabase.createNewFile();
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("New database file haw been created successfully : " + newDatabase.getName());
		} catch (IOException e) {
			e.printStackTrace();
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Server Internal error on creating the new database file");
		}

	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new ServerImpl());
	}

}
