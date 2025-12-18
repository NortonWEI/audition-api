package com.audition.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditionComment {

    private int postId;
    private int id;
    private String name;
    private String email;
    private String body;
}
