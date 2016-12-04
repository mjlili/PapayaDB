package fr.umlv.papayadbapplication.dataBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
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
		router.get("/select").handler(this::disptachGetRequest);

		// route to POST REST Methods
		router.post("/insert").handler(this::disptachPostRequest);

		// route to DELETE REST Methods
		router.delete("/delete").handler(this::disptachDeleteRequest);

		// otherwise serve static pages
		router.route().handler(StaticHandler.create());

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept).listen(3306);
		System.out.println("listen on port 3306");
	}

	public void disptachGetRequest(RoutingContext routingContext) {
		if (routingContext.getBodyAsJson().size() == 1) {
			if (routingContext.getBodyAsJson().containsKey("uri")) {
				getAllDatabases(routingContext);
				return;
			}
			selectAllFromDatabase(routingContext);
		}
	}

	public void disptachPostRequest(RoutingContext routingContext) {
		if (routingContext.getBodyAsJson().containsKey("username")) {
			System.out.println("ADDING NEW DATABASE");
			createNewDataBase(routingContext);
			return;
		}
		System.out.println("NOT ADDING NEW DATABASE");
		insertDocumentIntoDatabase(routingContext);
	}

	public void disptachDeleteRequest(RoutingContext routingContext) {
		deleteDatabase(routingContext);
	}

	public boolean databaseExists(String databaseName) {
		File databaseDirectory = new File("./Database");
		File[] files = databaseDirectory.listFiles();
		if (Arrays.stream(files).filter(database -> database.getName().equals(databaseName + ".json")).count() == 0) {
			return false;
		}
		return true;
	}

	public void getAllDatabases(RoutingContext routingContext) {
		File databaseDirectory = new File("./Database");
		File[] files = databaseDirectory.listFiles();
		if (files.length == 1) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry but there are no database files created");
			return;
		}
		StringBuilder databases = new StringBuilder();
		Arrays.stream(files).filter(database -> !database.getName().equals("database_index.json"))
				.map(database -> database.getName().substring(0, database.getName().length() - 5))
				.forEach(databaseName -> databases.append(databaseName).append("\n"));
		routingContext.response().putHeader("Content-Type", "application/json").end(databases.toString());
	}

	public void selectAllFromDatabase(RoutingContext routingContext) {
		String databaseName = routingContext.getBodyAsJson().getString("databasename");
		if (!databaseExists(databaseName)) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry but there are no databases named : " + databaseName);
			return;
		}
		String databaseContent = getAllDocumentContentAsString(databaseName);
		if (databaseContent == null) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("The database content cannot be extracted");
			return;
		}
		routingContext.response().putHeader("Content-Type", "application/json")
				.end("The database " + databaseName + " contains :\n" + databaseContent);
	}

	private String getAllDocumentContentAsString(String databaseName) {
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile("./Database/" + databaseName + ".json", "r");
			FileChannel fileChannel = randomAccessFile.getChannel();
			MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
			StringBuilder content = new StringBuilder();
			for (int i = 0; i < fileChannel.size(); i++) {
				content.append((char) mappedByteBuffer.get());
			}
			fileChannel.close();
			randomAccessFile.close();
			return content.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void createNewDataBase(RoutingContext routingContext) {
		String databaseName = (String) Objects.requireNonNull(routingContext.getBodyAsJson().getValue("databasename"));
		if (databaseExists(databaseName)) {
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

	public void deleteDatabase(RoutingContext routingContext) {
		String databaseName = (String) Objects.requireNonNull(routingContext.getBodyAsJson().getValue("databasename"));
		if (!databaseExists(databaseName)) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("Sorry but the database " + databaseName + " is not found");
			return;
		}
		File databaseDirectory = new File("./Database");
		File[] files = databaseDirectory.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(databaseName + ".json")) {
				if (files[i].delete()) {
					routingContext.response().putHeader("Content-Type", "application/json")
							.end("The database " + databaseName + " was deleted successfully");
					return;
				}
			}
		}
		routingContext.response().putHeader("Content-Type", "application/json")
				.end("Sorry but we were not able to delete the database " + databaseName);
	}

	public void insertDocumentIntoDatabase(RoutingContext routingContext) {
		JsonObject requestAsJson = routingContext.getBodyAsJson();
		if (pushAJsonDocumentWithMap(requestAsJson)) {
			routingContext.response().putHeader("Content-Type", "application/json")
					.end("The document has been created successfully");
			return;
		}
		routingContext.response().putHeader("Content-Type", "application/json")
				.end("Server Internal error on creating the new document");
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
	 * Associates a document to its database with a Json format into the
	 * database_index.json file which is the global index of our application
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

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new Database());
	}
}
