// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.protobuf.ByteString;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

import com.google.sps.data.UserComment;
import com.google.sps.data.CommentResponse;

/** Servlet that adds comment. */
@WebServlet("/add-comment")
public class AddCommentServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        UserService userService = UserServiceFactory.getUserService();

        // If user is not logged in, response CommentResponse json with login status and rediretURL
        if (!userService.isUserLoggedIn()) {
            String loginUrl = userService.createLoginURL("/");
            response.getWriter().println("<p>Please Login.</p>");
            response.getWriter().println("<p>Login <a href=\"" + loginUrl + "\">Here</a>.</p>");
            response.getWriter().println("<p><a href=\"/\">Go Back</a></p>");
            return;
        }      

        // Get user email
        String userEmail = userService.getCurrentUser().getEmail();

        // Receive form data
        Date currentTime = new Date();
        String commentText = getParameter(request, "form-comment", "");
        
        // Analyse sentiment of commentText
        Document doc = Document.newBuilder().setContent(commentText).setType(Document.Type.PLAIN_TEXT).build();
        LanguageServiceClient languageService = LanguageServiceClient.create();
        Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
        float score = sentiment.getScore();
        languageService.close();

        // Get the BlobKey that points to the image uploaded by the user.
        BlobKey blobKey = getBlobKey(request, "form-pic");

        String imageUrl = "";
        List<String> imageLabels = new ArrayList<String>();

        if (blobKey != null) {
            // Get the URL of the image that the user uploaded to Blobstore.
            imageUrl = getUploadedFileUrl(blobKey);

            // Get the labels of the image that the user uploaded.
            byte[] blobBytes = getBlobBytes(blobKey);
            List<EntityAnnotation> Labels = getImageLabels(blobBytes);

            for (EntityAnnotation label : Labels) {
                imageLabels.add(label.getDescription());
            }
        }

        // Add comment to datastore
        Entity commentEntity = new Entity("UserComment");
        commentEntity.setProperty("userName", userEmail);
        commentEntity.setProperty("currentTime", currentTime);
        commentEntity.setProperty("commentText", commentText);
        commentEntity.setProperty("imageURL", imageUrl);
        commentEntity.setProperty("imageLabels", imageLabels);
        commentEntity.setProperty("score", score);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

        // Redirect back to the HTML page.
        response.sendRedirect("/index.html#comment");
    }

    /**
    * @return the request parameter, or the default value if the parameter
    *         was not specified by the client
    */
    private String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
    * Returns the BlobKey that points to the file uploaded by the user, or null if the user didn't
    * upload a file.
    */
    private BlobKey getBlobKey(HttpServletRequest request, String formInputElementName) {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
        List<BlobKey> blobKeys = blobs.get(formInputElementName);

        // User submitted form without selecting a file, so we can't get a BlobKey. (dev server)
        if (blobKeys == null || blobKeys.isEmpty()) {
            return null;
        }

        // Our form only contains a single file input, so get the first index.
        BlobKey blobKey = blobKeys.get(0);

        // User submitted form without selecting a file, so the BlobKey is empty. (live server)
        BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
        if (blobInfo == null || blobInfo.getSize() == 0) {
            blobstoreService.delete(blobKey);
            return null;
        }

        return blobKey;
    }

    /**
    * Blobstore stores files as binary data. This function retrieves the binary data stored at the
    * BlobKey parameter.
    */
    private byte[] getBlobBytes(BlobKey blobKey) throws IOException {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

        int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
        long currentByteIndex = 0;
        boolean continueReading = true;
        while (continueReading) {
            // end index is inclusive, so we have to subtract 1 to get fetchSize bytes
            byte[] b = blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
            outputBytes.write(b);

            // if we read fewer bytes than we requested, then we reached the end
            if (b.length < fetchSize) {
                continueReading = false;
            }

            currentByteIndex += fetchSize;
        }

        return outputBytes.toByteArray();
    }

    /**
    * Uses the Google Cloud Vision API to generate a list of labels that apply to the image
    * represented by the binary data stored in imgBytes.
    */
    private List<EntityAnnotation> getImageLabels(byte[] imgBytes) throws IOException {
        ByteString byteString = ByteString.copyFrom(imgBytes);
        Image image = Image.newBuilder().setContent(byteString).build();

        Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        AnnotateImageRequest request =
            AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(request);

        ImageAnnotatorClient client = ImageAnnotatorClient.create();
        BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
        client.close();
        List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
        AnnotateImageResponse imageResponse = imageResponses.get(0);

        if (imageResponse.hasError()) {
            System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
            return null;
        }

        return imageResponse.getLabelAnnotationsList();
    }

    /** Returns a URL that points to the uploaded file. */
    private String getUploadedFileUrl(BlobKey blobKey) {
        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
        String url = imagesService.getServingUrl(options);

        // // GCS's localhost preview is not actually on localhost,
        // // so make the URL relative to the current domain.
        // if(url.startsWith("http://localhost:8080/")){
        // url = url.replace("http://localhost:8080/", "/");
        // }
        return url;
    }

}

