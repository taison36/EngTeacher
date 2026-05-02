package EngTeacher.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Session {
    private String id;
    @Builder.Default
    private List<Exercise> exercises = new ArrayList<>();
}
