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
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.sps.data.UserComment;


/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private final ArrayList<UserComment> commentList = new ArrayList<UserComment>(); 
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Convert the comment list to JSON
        String json = convertToJsonUsingGson(commentList);

        // Send the JSON as the response
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Receive form data
        String userName = getParameter(request, "form-name", "");
        Date currentTime = new Date();
        String commentText = getParameter(request, "form-comment", "");

        // Add comment to comment list
        UserComment newComment = new UserComment(userName, currentTime, commentText);
        commentList.add(newComment);

        // Redirect back to the HTML page.
        response.sendRedirect("/index.html#comment");
    }

    /**
    * Converts a UserComment list instance into a JSON string using the Gson library. Note: We first added
    * the Gson library dependency to pom.xml.
    */
    private String convertToJsonUsingGson(ArrayList<UserComment> comments) {
        Gson gson = new Gson();
        String json = gson.toJson(comments);
        return json;
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

}
