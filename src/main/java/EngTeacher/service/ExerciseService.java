package EngTeacher.service;

import EngTeacher.dto.agent.ExerciseAttempt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {


    public void markCorrect(List<ExerciseAttempt.Correct> corrects) {
        corrects.forEach(correct -> correct.exerciseId());
    }

    public void markIncorrect(List<ExerciseAttempt.Incorrect> incorrects) {
    }
}
