package com.example.projectx.model;

import com.google.firebase.Timestamp;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageModel {
    private String message;
    private boolean seen;
    private String sender;


}
