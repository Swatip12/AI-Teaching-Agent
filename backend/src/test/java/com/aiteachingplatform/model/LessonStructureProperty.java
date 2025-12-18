package com.aiteachingplatform.model;

import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.util.PropertyTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static net.java.quickcheck.generator.PrimitiveGenerators.*;

/**
 * **Feature: ai-teaching-platform, Property 4: Consistent lesson structure**
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**
 * 
 * Property-based test for consistent lesson structure.
 * Tests that any lesson follows the required structure: simple explanation → real-life example → 
 * technical example → exactly one checkpoint question → 2-3 practice questions.
 */
@DataJpaTest
@ActiveProfiles("test")
public class LessonStructureProperty extends PropertyTestBase {
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Test
    void consistentLessonStructure() {
        assertProperty(new AbstractCharacteristic<LessonTestData>() {
            @Override
            protected void doSpecify(LessonTestData testData) throws Throwable {
                // Create lesson with structured content
                Lesson lesson = new Lesson(testData.title, testData.subject, testData.sequenceOrder, testData.content);
                lesson.setObjectives(testData.objectives);
                lesson.setDifficulty(testData.difficulty);
                lesson.setEstimatedDurationMinutes(testData.estimatedDurationMinutes);
                
                // Add checkpoint questions (should be exactly 1)
                for (CheckpointQuestion checkpointQuestion : testData.checkpointQuestions) {
                    checkpointQuestion.setLesson(lesson);
                    lesson.getCheckpointQuestions().add(checkpointQuestion);
                }
                
                // Add practice questions (should be 2-3)
                for (PracticeQuestion practiceQuestion : testData.practiceQuestions) {
                    practiceQuestion.setLesson(lesson);
                    lesson.getPracticeQuestions().add(practiceQuestion);
                }
                
                // Save lesson
                Lesson savedLesson = lessonRepository.save(lesson);
                
                // Verify lesson structure requirements
                
                // Requirement 2.1: Content should explain concepts in simple words first
                assert savedLesson.getContent() != null : "Lesson content should not be null";
                assert savedLesson.getContent().contains("simple explanation") : "Lesson should contain simple explanation section";
                
                // Requirement 2.2: Should provide real-life examples before technical examples
                String content = savedLesson.getContent().toLowerCase();
                int realLifeIndex = content.indexOf("real-life example");
                int technicalIndex = content.indexOf("technical example");
                assert realLifeIndex >= 0 : "Lesson should contain real-life example section";
                assert technicalIndex >= 0 : "Lesson should contain technical example section";
                assert realLifeIndex < technicalIndex : "Real-life example should come before technical example";
                
                // Requirement 2.3: Should include simple coding demonstrations
                assert content.contains("coding demonstration") : "Lesson should contain coding demonstration";
                
                // Requirement 2.4: Should ask exactly one checkpoint question
                assert savedLesson.getCheckpointQuestions().size() == 1 : 
                    "Lesson should have exactly one checkpoint question, but had " + savedLesson.getCheckpointQuestions().size();
                
                // Requirement 2.5: Should offer 2-3 practice questions per concept
                int practiceCount = savedLesson.getPracticeQuestions().size();
                assert practiceCount >= 2 && practiceCount <= 3 : 
                    "Lesson should have 2-3 practice questions, but had " + practiceCount;
                
                // Verify checkpoint question properties
                CheckpointQuestion checkpoint = savedLesson.getCheckpointQuestions().get(0);
                assert checkpoint.getQuestion() != null && !checkpoint.getQuestion().trim().isEmpty() : 
                    "Checkpoint question should have non-empty question text";
                assert checkpoint.getCorrectAnswer() != null && !checkpoint.getCorrectAnswer().trim().isEmpty() : 
                    "Checkpoint question should have non-empty correct answer";
                
                // Verify practice question properties
                for (PracticeQuestion practice : savedLesson.getPracticeQuestions()) {
                    assert practice.getQuestion() != null && !practice.getQuestion().trim().isEmpty() : 
                        "Practice question should have non-empty question text";
                    assert practice.getQuestionType() != null : 
                        "Practice question should have a question type";
                }
                
                // Verify lesson has proper sequencing
                assert savedLesson.getSequenceOrder() != null && savedLesson.getSequenceOrder() > 0 : 
                    "Lesson should have positive sequence order";
                
                // Verify lesson has subject classification
                assert savedLesson.getSubject() != null && !savedLesson.getSubject().trim().isEmpty() : 
                    "Lesson should have non-empty subject";
            }
        });
    }
    
    private static class LessonTestData {
        final String title;
        final String subject;
        final Integer sequenceOrder;
        final String content;
        final String objectives;
        final Lesson.Difficulty difficulty;
        final Integer estimatedDurationMinutes;
        final List<CheckpointQuestion> checkpointQuestions;
        final List<PracticeQuestion> practiceQuestions;
        
        LessonTestData(String title, String subject, Integer sequenceOrder, String content,
                      String objectives, Lesson.Difficulty difficulty, Integer estimatedDurationMinutes,
                      List<CheckpointQuestion> checkpointQuestions, List<PracticeQuestion> practiceQuestions) {
            this.title = title;
            this.subject = subject;
            this.sequenceOrder = sequenceOrder;
            this.content = content;
            this.objectives = objectives;
            this.difficulty = difficulty;
            this.estimatedDurationMinutes = estimatedDurationMinutes;
            this.checkpointQuestions = checkpointQuestions;
            this.practiceQuestions = practiceQuestions;
        }
    }
    
    private static final Generator<LessonTestData> lessonTestDataGenerator = new Generator<LessonTestData>() {
        @Override
        public LessonTestData next() {
            String title = "Lesson: " + strings(10, 50).next();
            String subject = arrays(new String[]{"Java", "DSA", "FullStack", "Logic", "Interview"}).next();
            Integer sequenceOrder = integers(1, 100).next();
            
            // Create structured content that follows the required format
            String content = "Simple explanation: " + strings(50, 100).next() + 
                           "\n\nReal-life example: " + strings(50, 100).next() +
                           "\n\nTechnical example: " + strings(50, 100).next() +
                           "\n\nCoding demonstration: " + strings(50, 100).next();
            
            String objectives = "Learning objectives: " + strings(20, 100).next();
            Lesson.Difficulty difficulty = arrays(Lesson.Difficulty.values()).next();
            Integer estimatedDurationMinutes = integers(15, 120).next();
            
            // Create exactly 1 checkpoint question
            List<CheckpointQuestion> checkpointQuestions = new ArrayList<>();
            CheckpointQuestion checkpoint = new CheckpointQuestion();
            checkpoint.setQuestion("Checkpoint: " + strings(20, 100).next());
            checkpoint.setCorrectAnswer("Answer: " + strings(10, 50).next());
            checkpoint.setExplanation("Explanation: " + strings(20, 100).next());
            checkpoint.setQuestionType(arrays(CheckpointQuestion.QuestionType.values()).next());
            checkpoint.setSequenceOrder(1);
            checkpointQuestions.add(checkpoint);
            
            // Create 2-3 practice questions
            List<PracticeQuestion> practiceQuestions = new ArrayList<>();
            int practiceCount = integers(2, 3).next();
            for (int i = 0; i < practiceCount; i++) {
                PracticeQuestion practice = new PracticeQuestion();
                practice.setQuestion("Practice " + (i + 1) + ": " + strings(20, 100).next());
                practice.setExpectedSolution("Solution: " + strings(20, 100).next());
                practice.setHints("Hint: " + strings(10, 50).next());
                practice.setQuestionType(arrays(PracticeQuestion.QuestionType.values()).next());
                practice.setDifficulty(arrays(PracticeQuestion.Difficulty.values()).next());
                practice.setSequenceOrder(i + 1);
                practiceQuestions.add(practice);
            }
            
            return new LessonTestData(title, subject, sequenceOrder, content, objectives, 
                                    difficulty, estimatedDurationMinutes, checkpointQuestions, practiceQuestions);
        }
    };
    
    {
        // Register the generator
        Generator.register(LessonTestData.class, lessonTestDataGenerator);
    }
}