package com.example.projectx.model;

import com.google.firebase.Timestamp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class MessageModel {
    private String message;
    private String sender;
    private boolean seen;


  public   MessageModel(){}


}
