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
	 * this methods start the data base server on port 3306 and routes the
	 * applicant server request to the right method
	 * 
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

	/**
	 * this method dispatches the get request to the right method. it analyzes
	 * the request and redirect it. whether it's a get base's names or get
	 * document's content from a base
	 * 
	 * @param routingContext
	 */
	public void disptachGetRequest(RoutingContext routingContext) {
		if (routingContext.getBodyAsJson().size() == 1) {
			if (routingContext.getBodyAsJson().containsKey("uri")) {
				getAllDatabases(routingContext);
				return;
			}
			selectAllFromDatabase(routingContext);
		}
	}

	/**
	 * this method dispatches the post request to the right method. it analyzes
	 * the request and redirect it. whether it's a create a new data base or
	 * insert a document in a data base
	 * 
	 * @param routingContext
	 */
	public void disptachPostRequest(RoutingContext routingContext) {
		if (routingContext.getBodyAsJson().containsKey("username")) {
			createNewDataBase(routingContext);
			return;
		}
		insertDocumentIntoDatabase(routingContext);
	}

	/**
	 * not implemented yet
	 * 
	 * @param routingContext
	 */
	public void disptachDeleteRequest(RoutingContext routingContext) {
		deleteDatabase(routingContext);
	}

	/**
	 * returns true if the data base already exists
	 * 
	 * @param databasename
	 *            a string containing the data base name
	 * @return boolean exists or not
	 */
	public boolean databaseExists(String databaseName) {
		File databaseDirectory = new File("./Database");
		File[] files = databaseDirectory.listFiles();
		if (Arrays.stream(files).filter(database -> database.getName().equals(databaseName + ".json")).count() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * the list of all data bases
	 * 
	 * @param routingContext
	 */
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

	/**
	 * extracts the content of a data base
	 * 
	 * @param routingContext
	 */
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

	/**
	 * get a map of the data base file and parse it
	 * 
	 * @param String-
	 *            data base name
	 * @return String - data base content
	 * @throws FileNotFoundException
	 *             - if the the file doesn't exist
	 * @throws IOException
	 */
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

	/**
	 * creates a new data base - a new json file
	 * 
	 * @param routingContext
	 */
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

	/**
	 * delete an existing data base
	 * 
	 * @param routingContext
	 */
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

	/**
	 * inserts a given document un the data base
	 * 
	 * @param routingContext
	 */
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
	 * add a document to the data base using a filechannel map and associate it
	 * with a data base
	 * 
	 * @author jlilimk
	 * @param Json
	 *            request
	 * @return boolean true if the document was add and the association
	 *         succeeded
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
	 * @return boolean true if the association was successfully made
	 * @throws FileNotFoundException
	 *             if the file doesn't exist
	 * @throws IOException
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