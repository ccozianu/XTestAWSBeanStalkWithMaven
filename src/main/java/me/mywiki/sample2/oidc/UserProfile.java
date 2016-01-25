package me.mywiki.sample2.oidc;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TODO: re-org this into a better place,
 * ideally using a type-provider like scenario
 * Depending dynamically on the contract between the client site and 
 * OIDC provider not necessarily all fields will be present, so maybe 
 * we should refactor into a hashmap or Optional based fields
 */
public class UserProfile {
    public final String id;
    public final String name;
    public final String email;
    public final String pictureUrl;
    public final String profileUrl;
    public UserProfile( @JsonProperty("sub") String id_,
                        @JsonProperty("name") String name_,
                        @JsonProperty("email") String email_,
                        @JsonProperty("picture") String pictureUrl_,
                        @JsonProperty("profile") String profileUrl_ ) 
    {
        this.id= id_;
        this.name= name_;
        this.email= email_;
        this.pictureUrl= pictureUrl_;
        this.profileUrl= profileUrl_;
    }
}