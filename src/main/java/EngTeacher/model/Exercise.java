package EngTeacher.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Exercise {
    private final String id;
    private String question;
    private Phrase phrase;
    private boolean done;
}
