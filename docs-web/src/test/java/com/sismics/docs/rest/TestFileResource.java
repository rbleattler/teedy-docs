package com.sismics.docs.rest;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sismics.util.mime.MimeType;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * Exhaustive test of the document resource.
 * 
 * @author bgamard
 */
public class TestFileResource extends BaseJerseyTest {
    /**
     * Test the document resource.
     * 
     * @throws Exception
     */
    @Test
    public void testFileResource() throws Exception {
        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Create a document
        WebResource documentResource = resource().path("/document");
        documentResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("title", "File test document 1");
        ClientResponse response = documentResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String document1Id = json.optString("id");
        Assert.assertNotNull(document1Id);
        
        // Add a file
        WebResource fileResource = resource().path("/file");
        fileResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream file = this.getClass().getResourceAsStream("/file/PIA00452.jpg");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(file),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        form.field("id", document1Id);
        response = fileResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String file1Id = json.getString("id");
        
        // Get the file
        fileResource = resource().path("/file/" + file1Id);
        fileResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = fileResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertEquals(MimeType.IMAGE_JPEG, json.getString("mimetype"));
        Assert.assertEquals(file1Id, json.getString("id"));
        
        // Get the file data
        fileResource = resource().path("/file/" + file1Id + "/data");
        fileResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = fileResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        InputStream is = response.getEntityInputStream();
        byte[] fileBytes = ByteStreams.toByteArray(is);
        Assert.assertEquals(163510, fileBytes.length);
        
        // Get all files from a document
        fileResource = resource().path("/file/list");
        fileResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl getParams = new MultivaluedMapImpl();
        getParams.putSingle("id", document1Id);
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONArray files = json.getJSONArray("files");
        Assert.assertEquals(1, files.length());
        
        // Deletes a file
        documentResource = resource().path("/file/" + file1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = documentResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Get all files from a document
        fileResource = resource().path("/file/list");
        fileResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("id", document1Id);
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        files = json.getJSONArray("files");
        Assert.assertEquals(0, files.length());
    }
}