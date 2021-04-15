package com.example.projectx.model;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Requests {
    String RequestId;
    String Request;
    String ImageUrl;
    String CreatedBy;
    double latitute;
    double longitude;
    double distance;
    boolean tagsAssigned;
    private String CreatedAt;

}