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

package com.google.sps.data;

import java.util.ArrayList;
import com.google.sps.data.UserComment;

/** Class serves as the response template of user comment. */
public final class CommentResponse {

    private final Boolean isLogegIn;
    private final String redirectURL;
    private final String uploadURL;
    private final ArrayList<UserComment> commentList;

    public CommentResponse(Boolean isLogegIn, String redirectURL, String uploadURL, ArrayList<UserComment> commentList) {
        this.isLogegIn = isLogegIn;
        this.redirectURL = redirectURL;
        this.uploadURL = uploadURL;
        if (isLogegIn) {
            this.commentList = commentList;
        } else {
            this.commentList = new ArrayList<UserComment>();
        }
    }

    public Boolean getIsLogegIn() {
        return isLogegIn;
    }
        
    public String getRedirectURL() {
        return redirectURL;
    }

    public String getUploadURL() {
        return uploadURL;
    }

    public ArrayList<UserComment> getCommentList() {
        return commentList;
    }

}
