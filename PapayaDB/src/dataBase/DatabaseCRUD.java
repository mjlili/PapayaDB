package dataBase;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public interface DatabaseCRUD {
	public void createNewDataBase(RoutingContext routingContext);

	public void selectDatabase(RoutingContext routingContext);

	public void deleteExistingDataBase(RoutingContext routingContext);

	public boolean associateDocumentToDatabase(JsonObject requestAsJson);

	public void deleteDocumentFromDatabase(RoutingContext routingContext);

	public void selectDocumentFromDatabase(RoutingContext routingContext);
}
