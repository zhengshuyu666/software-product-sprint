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
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;


import com.google.sps.data.UserComment;
import com.google.sps.data.CommentResponse;

/** Servlet that returns comment data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;");

        // Get the Blobstore URL
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        String uploadUrl = blobstoreService.createUploadUrl("/add-comment");

        UserService userService = UserServiceFactory.getUserService();

        // If user is not logged in, response CommentResponse json with login status and rediretURL
        if (!userService.isUserLoggedIn()) {
            String loginUrl = userService.createLoginURL("/");
            CommentResponse responseData = new CommentResponse(false, loginUrl, uploadUrl, null);
            String json = convertToJsonUsingGson(responseData);
            response.getWriter().println(json);
            return;
        } 

        // If the user is logged in, response CommentResponse json with login status, rediretURL, comment list and user info
        String logoutUrl = userService.createLogoutURL("/");
        String userEmail = userService.getCurrentUser().getEmail();

        // Load comments from datastore
        Query query = new Query("UserComment").addSort("currentTime", SortDirection.DESCENDING);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);
        ArrayList<UserComment> commentList = new ArrayList<UserComment>();
        for (Entity entity : results.asIterable()) {
            String userName = (String) entity.getProperty("userName");
            Date currentTime = (Date) entity.getProperty("currentTime");
            String commentText = (String) entity.getProperty("commentText");
            String imageURL = (String) entity.getProperty("imageURL");
            List<String> imageLabels = (List<String>) entity.getProperty("imageLabels");
            UserComment newComment = new UserComment(userName, currentTime, commentText, imageURL, imageLabels);
            commentList.add(newComment);
        }

        // Build response json
        CommentResponse responseData = new CommentResponse(true, logoutUrl, uploadUrl, commentList);
        String json = convertToJsonUsingGson(responseData);

        // Send the JSON as the response
        response.getWriter().println(json);
    }

    // @Override
    // public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
    //     UserService userService = UserServiceFactory.getUserService();

    //     // If user is not logged in, response CommentResponse json with login status and rediretURL
    //     if (!userService.isUserLoggedIn()) {
    //         String loginUrl = userService.createLoginURL("/");
    //         response.getWriter().println("<p>Please Login.</p>");
    //         response.getWriter().println("<p>Login <a href=\"" + loginUrl + "\">Here</a>.</p>");
    //         response.getWriter().println("<p><a href=\"/\">Go Back</a></p>");
    //         return;
    //     }      

    //     // Get user email
    //     String userEmail = userService.getCurrentUser().getEmail();

    //     // Receive form data
    //     String userName = getParameter(request, "form-name", "");
    //     Date currentTime = new Date();
    //     String commentText = getParameter(request, "form-comment", "");

    //     // Add comment to datastore
    //     Entity commentEntity = new Entity("UserComment");
    //     commentEntity.setProperty("userName", userEmail);
    //     commentEntity.setProperty("currentTime", currentTime);
    //     commentEntity.setProperty("commentText", commentText);

    //     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    //     datastore.put(commentEntity);

    //     // Redirect back to the HTML page.
    //     response.sendRedirect("/index.html#comment");
    // }

    /**
    * Converts a CommentResponse instance into a JSON string using the Gson library. Note: We first added
    * the Gson library dependency to pom.xml.
    */
    private String convertToJsonUsingGson(CommentResponse responseData) {
        Gson gson = new Gson();
        String json = gson.toJson(responseData);
        return json;
    }

    // /**
    // * @return the request parameter, or the default value if the parameter
    // *         was not specified by the client
    // */
    // private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    //     String value = request.getParameter(name);
    //     if (value == null) {
    //         return defaultValue;
    //     }
    //     return value;
    // }

}
