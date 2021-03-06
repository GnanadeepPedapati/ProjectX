package com.example.projectx.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseOverview {

    private String entityName;
    private String newMessageCount;
    private String lastReceivedTime;
    private String lastMessage;
    private String otherUser;


}
