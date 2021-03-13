package com.example.projectx.model;


import com.google.firebase.Timestamp;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Requests {
    String RequestId;
    String Request;
    String ImageUrl;
    String CreatedBy;
    Timestamp CreatedAt;
    boolean tagsAssigned;

}