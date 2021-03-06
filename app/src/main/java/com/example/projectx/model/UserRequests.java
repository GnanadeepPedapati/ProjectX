package com.example.projectx.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequests {

    boolean hasReplied;
    String receiver;
    String requestId;
    String sender;

}
