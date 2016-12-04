package fr.umlv.papayadbapplication.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class ClientImpl implements Client {

	/**
	 * @author jlilimk
	 */
	@Override
	public void displayApplicationMenu() {
		System.out.println("*** Welcome on the PapayaDB Application ***");
		System.out.println("We present to you our features :");
		System.out.println("#####################################################");
		System.out.println("#  Note that your request will be case sensitive !  #");
		System.out.println("#####################################################");
		System.out.println("To create a new database please enter : createNewDataBase /:databasename");
		System.out.println("To select another database please enter : selectDatabase /");
		System.out.println("To delete a specific database please enter : deleteExistingDataBase /:databasename");
		System.out.println(
				"To insert a new document into a database please enter : insertDocumentIntoDatabase /:databasename/:documentname/key1=value1&key2=value2...");
		System.out.println(
				"To select a specific document please enter : selectDocumentFromDatabase /:databasename?filter1=value1&filter2=value2...");
		System.out.println(
				"To delete a specific document please enter : deleteDocumentFromDatabase /:databasename/:documentname");
	}

	/**
	 * @author jlilimk
	 */
	@Override
	public void dispatchRequest(String clientRequest) {
		String[] splitClientRequest = clientRequest.split(" ");
		StringBuilder uriAsString = new StringBuilder();
		Arrays.stream(splitClientRequest).skip(1).forEach(element -> uriAsString.append(element));
		String finalClientRequestAsString = uriAsString.toString();
		switch (splitClientRequest[0]) {
		case "createNewDataBase":
			this.sendPostRequest(finalClientRequestAsString);
			break;
		case "selectDatabase":
			this.sendGetRequest(finalClientRequestAsString);
			break;
		case "deleteExistingDataBase":
			this.sendDeleteRequest(finalClientRequestAsString);
			break;
		case "insertDocumentIntoDatabase":
			this.sendPostRequest(finalClientRequestAsString);
			break;
		case "selectDocumentFromDatabase":
			this.sendGetRequest(finalClientRequestAsString);
			break;
		case "deleteDocumentFromDatabase":
			this.sendDeleteRequest(finalClientRequestAsString);
			break;
		default:
			System.out.println("Sorry we cannot answer your request");
			displayApplicationMenu();
			break;
		}
	}

	public static void main(String[] args) throws IOException {
		Client papayaClient = new ClientImpl();
		papayaClient.displayApplicationMenu();
		Scanner requestScanner = new Scanner(System.in);
		System.out.println("Enter a choice please :");
		String request = requestScanner.nextLine();
		while (!request.contentEquals("quit")) {
			papayaClient.dispatchRequest(request);
			System.out.println("Enter a choice please :");
			request = requestScanner.nextLine();
			if (request.contentEquals("menu")) {
				Runtime.getRuntime().exec("clear");
				papayaClient.displayApplicationMenu();
				System.out.println("Enter a choice please :");
				request = requestScanner.nextLine();
			}
		}
		requestScanner.close();
		System.out.println("The job is done!");
	}
}