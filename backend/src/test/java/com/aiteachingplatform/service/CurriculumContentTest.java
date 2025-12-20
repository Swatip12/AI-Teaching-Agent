package com.aiteachingplatform.service;

import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.repository.LessonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that curriculum content is properly initialized
 * for all five subjects as specified in task 13
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CurriculumContentTest {

    @Autowired
    private LessonRepository lessonRepository;

    @Test
    public void testAllSubjectsHaveLessons() {
        // Verify that lessons exist for all five required subjects
        String[] expectedSubjects = {
            "Java", 
            "DSA", 
            "Full Stack", 
            "Logical Reasoning", 
            "Interview Prep"
        };

        for (String subject : expectedSubjects) {
            List<Lesson> lessons = lessonRepository.findBySubjectOrderBySequenceOrder(subject);
            assertFalse(lessons.isEmpty(), 
                "Subject '" + subject + "' should have at least one lesson");
            
            // Verify lessons are properly ordered
            for (int i = 0; i < lessons.size(); i++) {
                assertEquals(i + 1, lessons.get(i).getSequenceOrder().intValue(),
                    "Lesson sequence order should be correct for " + subject);
            }
        }
    }

    @Test
    public void testJavaLessonsContent() {
        List<Lesson> javaLessons = lessonRepository.findBySubjectOrderBySequenceOrder("Java");
        assertTrue(javaLessons.size() >= 6, "Java should have at least 6 lessons");
        
        // Check first lesson
        Lesson firstLesson = javaLessons.get(0);
        assertEquals("Introduction to Java Programming", firstLesson.getTitle());
        assertNotNull(firstLesson.getContent());
        assertTrue(firstLesson.getContent().contains("Simple explanation:"));
        assertTrue(firstLesson.getContent().contains("Real-life example:"));
        assertTrue(firstLesson.getContent().contains("Technical example:"));
        assertTrue(firstLesson.getContent().contains("Coding demonstration:"));
    }

    @Test
    public void testDSALessonsContent() {
        List<Lesson> dsaLessons = lessonRepository.findBySubjectOrderBySequenceOrder("DSA");
        assertTrue(dsaLessons.size() >= 5, "DSA should have at least 5 lessons");
        
        // Check first lesson
        Lesson firstLesson = dsaLessons.get(0);
        assertEquals("Introduction to Arrays", firstLesson.getTitle());
        assertNotNull(firstLesson.getContent());
    }

    @Test
    public void testFullStackLessonsContent() {
        List<Lesson> fullStackLessons = lessonRepository.findBySubjectOrderBySequenceOrder("Full Stack");
        assertTrue(fullStackLessons.size() >= 5, "Full Stack should have at least 5 lessons");
        
        // Check first lesson
        Lesson firstLesson = fullStackLessons.get(0);
        assertEquals("HTML Fundamentals", firstLesson.getTitle());
        assertNotNull(firstLesson.getContent());
    }

    @Test
    public void testLogicalReasoningLessonsContent() {
        List<Lesson> logicalLessons = lessonRepository.findBySubjectOrderBySequenceOrder("Logical Reasoning");
        assertTrue(logicalLessons.size() >= 3, "Logical Reasoning should have at least 3 lessons");
        
        // Check first lesson
        Lesson firstLesson = logicalLessons.get(0);
        assertEquals("Pattern Recognition", firstLesson.getTitle());
        assertNotNull(firstLesson.getContent());
    }

    @Test
    public void testInterviewPrepLessonsContent() {
        List<Lesson> interviewLessons = lessonRepository.findBySubjectOrderBySequenceOrder("Interview Prep");
        assertTrue(interviewLessons.size() >= 4, "Interview Prep should have at least 4 lessons");
        
        // Check first lesson
        Lesson firstLesson = interviewLessons.get(0);
        assertEquals("Technical Interview Fundamentals", firstLesson.getTitle());
        assertNotNull(firstLesson.getContent());
    }

    @Test
    public void testLessonStructureCompliance() {
        // Test that lessons follow the required structure from Requirements 2.1-2.5
        List<Lesson> allLessons = lessonRepository.findAll();
        
        for (Lesson lesson : allLessons) {
            // Each lesson should have content following the structure
            String content = lesson.getContent();
            assertNotNull(content, "Lesson content should not be null");
            
            // Should follow the structure: simple explanation → real-life example → technical example → coding demonstration
            assertTrue(content.contains("Simple explanation:"), 
                "Lesson should start with simple explanation: " + lesson.getTitle());
            assertTrue(content.contains("Real-life example:"), 
                "Lesson should have real-life example: " + lesson.getTitle());
            assertTrue(content.contains("Technical example:"), 
                "Lesson should have technical example: " + lesson.getTitle());
            
            // Each lesson should have checkpoint questions
            assertFalse(lesson.getCheckpointQuestions().isEmpty(), 
                "Lesson should have at least one checkpoint question: " + lesson.getTitle());
            
            // Each lesson should have practice questions
            assertFalse(lesson.getPracticeQuestions().isEmpty(), 
                "Lesson should have at least one practice question: " + lesson.getTitle());
        }
    }

    @Test
    public void testPrerequisiteStructure() {
        // Test that lessons have proper prerequisite structure
        List<Lesson> allLessons = lessonRepository.findAll();
        
        for (Lesson lesson : allLessons) {
            if (lesson.getSequenceOrder() == 1) {
                // First lessons should have no prerequisites
                assertTrue(lesson.getPrerequisiteLessonIds().isEmpty(), 
                    "First lesson should have no prerequisites: " + lesson.getTitle());
            } else {
                // Later lessons may have prerequisites (but not required for all)
                // This is subject to the specific curriculum design
            }
        }
    }
}