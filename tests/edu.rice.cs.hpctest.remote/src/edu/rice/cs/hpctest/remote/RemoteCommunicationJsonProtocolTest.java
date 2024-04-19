package edu.rice.cs.hpctest.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.data.RemoteCommunicationJsonProtocol;

public class RemoteCommunicationJsonProtocolTest 
{
	@Test
	public void sendCommandToOpenDatabaseAndWaitForReplyTest() throws IOException {
		var protocol = createProtocol();

		ISecuredConnection.ISessionRemoteSocket session = Mockito.mock(ISecuredConnection.ISessionRemoteSocket.class);
		when(session.read()).thenReturn(new String[] {"""
				{ status: success,
				  socket: /toto/tutu
				}
				"""});
		
		ArgumentCaptor<String> buffer = ArgumentCaptor.forClass(String.class);

		var status = protocol.sendCommandToGetDirectoryContent(session, "/tmp");
		assertNotNull(status);

		// check the sending message
		verify(session).write(buffer.capture());
		
		var strWriteArg = buffer.getValue();
		assertNotNull(strWriteArg);
		
		// check the receiving
		var response = status.getResponseArgument()[0];
		assertTrue(response.contains("/toto/tutu"));
	}
	
	
	@Test
	public void sendCommandToGetDirectoryContentTest() throws IOException {
		final int NUM_ELEMS = 4;
		final String DIRECTORY = "/toto/tutu";
		
		var protocol = createProtocol();

		ISecuredConnection.ISessionRemoteSocket session = Mockito.mock(ISecuredConnection.ISessionRemoteSocket.class);

		var jsonInput = createDirectoryContent(NUM_ELEMS, DIRECTORY);
		when(session.read()).thenReturn(new String[] {
				jsonInput.toString()
		});
		ArgumentCaptor<String> buffer = ArgumentCaptor.forClass(String.class);

		var status = protocol.sendCommandToGetDirectoryContent(session, "/toto/tutu");
		assertNotNull(status);
		
		// check the sending message	
		verify(session).write(buffer.capture());
		var strWriteArg = buffer.getValue();
		assertNotNull(strWriteArg);
		
		// check the receiving
		assertNotNull(status.getResponseArgument());
		assertTrue(status.getResponseArgument().length == 1);
		
		var directory = protocol.createDirectoryContent(session, status.getResponseArgument());
		
		assertNotNull(directory);
		assertEquals(DIRECTORY, directory.getDirectory());
		
		var contents = directory.getContent();
		assertNotNull(contents);
		assertTrue(contents.length == NUM_ELEMS);
	}

	
	private JSONObject createDirectoryContent(int numElems, String directory) {
		JSONObject json = new JSONObject();
		json.put("status", "success");
		json.put("path", directory);

		JSONArray jsonSetOfFiles = new JSONArray();

		for (var i=0; i<numElems; i++) {
			var file = String.format("file %d", i);
			var jsonFile = createFileObject(file, i%2 == 0 ? "d" : "f");
			
			jsonSetOfFiles.put(jsonFile);
		}
		json.putOnce("contents", jsonSetOfFiles);
		return json;
	}
	
	
	private JSONObject createFileObject(String name, String type) {
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("type", type);
		
		return json;
	}


	private RemoteCommunicationJsonProtocol createProtocol() {
		return new RemoteCommunicationJsonProtocol();
	}
}
