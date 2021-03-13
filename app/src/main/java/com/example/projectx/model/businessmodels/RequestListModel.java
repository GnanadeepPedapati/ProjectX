package com.example.projectx.model.businessmodels;

import com.google.firebase.Timestamp;

import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class RequestListModel {
    String RequestId;
    String Request;
    String ImageUrl;
    String CreatedBy;
    String CreatedAt;
    int responsesCount;

}