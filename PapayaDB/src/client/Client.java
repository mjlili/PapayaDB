package client;

import java.io.IOException;

public interface Client {
	public void createDocumentUri(String uri) throws IOException, InterruptedException;

	public void createDocumentUrl(String uri) throws IOException;

	public void getAllDocuments();
}
