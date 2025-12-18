package com.aiteachingplatform.service;

import com.aiteachingplatform.model.CheckpointQuestion;
import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.PracticeQuestion;
import com.aiteachingplatform.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to initialize sample lesson data for demonstration
 * This creates structured lessons following the requirements
 */
@Service
public class LessonDataInitializationService implements CommandLineRunner {
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Only initialize if no lessons exist
        if (lessonRepository.count() == 0) {
            initializeJavaLessons();
            initializeDSALessons();
        }
    }
    
    private void initializeJavaLessons() {
        // Java Lesson 1: Introduction to Java
        Lesson javaIntro = createLesson(
            "Introduction to Java Programming",
            "Java",
            1,
            createJavaIntroContent(),
            "Understand what Java is and write your first program",
            Lesson.Difficulty.BEGINNER,
            45,
            null // No prerequisites
        );
        
        // Add checkpoint question
        CheckpointQuestion checkpoint1 = new CheckpointQuestion(
            javaIntro,
            "What is the main method signature in Java?",
            "public static void main(String[] args)",
            "The main method is the entry point of any Java application. It must be public (accessible), static (can be called without creating an object), void (returns nothing), and takes a String array as parameter."
        );
        checkpoint1.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint1.setSequenceOrder(1);
        javaIntro.getCheckpointQuestions().add(checkpoint1);
        
        // Add practice questions
        PracticeQuestion practice1 = new PracticeQuestion(
            javaIntro,
            "Write a Java program that prints 'Hello, World!' to the console.",
            "public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice1.setHints("Use System.out.println() to print to console");
        practice1.setSequenceOrder(1);
        practice1.setStarterCode("public class HelloWorld {\n    public static void main(String[] args) {\n        // Write your code here\n    }\n}");
        javaIntro.getPracticeQuestions().add(practice1);
        
        PracticeQuestion practice2 = new PracticeQuestion(
            javaIntro,
            "Create a Java program that prints your name and age.",
            "public class PersonInfo {\n    public static void main(String[] args) {\n        System.out.println(\"Name: John Doe\");\n        System.out.println(\"Age: 25\");\n    }\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice2.setHints("Use multiple System.out.println() statements");
        practice2.setSequenceOrder(2);
        javaIntro.getPracticeQuestions().add(practice2);
        
        lessonRepository.save(javaIntro);
        
        // Java Lesson 2: Variables and Data Types
        Lesson javaVariables = createLesson(
            "Variables and Data Types in Java",
            "Java",
            2,
            createJavaVariablesContent(),
            "Learn about different data types and how to declare variables",
            Lesson.Difficulty.BEGINNER,
            60,
            List.of(javaIntro.getId()) // Requires Java intro
        );
        
        CheckpointQuestion checkpoint2 = new CheckpointQuestion(
            javaVariables,
            "Which data type would you use to store a person's age?",
            "int",
            "Age is a whole number, so 'int' is the appropriate data type. It can store values from -2,147,483,648 to 2,147,483,647."
        );
        checkpoint2.setQuestionType(CheckpointQuestion.QuestionType.MULTIPLE_CHOICE);
        checkpoint2.setSequenceOrder(1);
        javaVariables.getCheckpointQuestions().add(checkpoint2);
        
        PracticeQuestion practice3 = new PracticeQuestion(
            javaVariables,
            "Declare variables for a student's name, age, and GPA, then print them.",
            "String name = \"Alice\";\nint age = 20;\ndouble gpa = 3.85;\nSystem.out.println(\"Name: \" + name);\nSystem.out.println(\"Age: \" + age);\nSystem.out.println(\"GPA: \" + gpa);",
            PracticeQuestion.QuestionType.CODING
        );
        practice3.setHints("Use String for name, int for age, and double for GPA");
        practice3.setSequenceOrder(1);
        javaVariables.getPracticeQuestions().add(practice3);
        
        lessonRepository.save(javaVariables);
    }
    
    private void initializeDSALessons() {
        // DSA Lesson 1: Introduction to Arrays
        Lesson arrayIntro = createLesson(
            "Introduction to Arrays",
            "DSA",
            1,
            createArrayIntroContent(),
            "Understand arrays and basic array operations",
            Lesson.Difficulty.BEGINNER,
            50,
            null // No prerequisites
        );
        
        CheckpointQuestion checkpoint3 = new CheckpointQuestion(
            arrayIntro,
            "What is the index of the first element in an array?",
            "0",
            "Arrays in most programming languages use zero-based indexing, meaning the first element is at index 0."
        );
        checkpoint3.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint3.setSequenceOrder(1);
        arrayIntro.getCheckpointQuestions().add(checkpoint3);
        
        PracticeQuestion practice4 = new PracticeQuestion(
            arrayIntro,
            "Create an array of 5 integers and print all elements.",
            "int[] numbers = {1, 2, 3, 4, 5};\nfor (int i = 0; i < numbers.length; i++) {\n    System.out.println(numbers[i]);\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice4.setHints("Use a for loop to iterate through the array");
        practice4.setSequenceOrder(1);
        arrayIntro.getPracticeQuestions().add(practice4);
        
        lessonRepository.save(arrayIntro);
    }
    
    private Lesson createLesson(String title, String subject, Integer sequenceOrder, String content,
                               String objectives, Lesson.Difficulty difficulty, Integer estimatedDurationMinutes,
                               List<Long> prerequisiteLessonIds) {
        Lesson lesson = new Lesson(title, subject, sequenceOrder, content);
        lesson.setObjectives(objectives);
        lesson.setDifficulty(difficulty);
        lesson.setEstimatedDurationMinutes(estimatedDurationMinutes);
        if (prerequisiteLessonIds != null) {
            lesson.setPrerequisiteLessonIds(new ArrayList<>(prerequisiteLessonIds));
        }
        return lesson;
    }
    
    private String createJavaIntroContent() {
        return """
            Simple explanation: Java is a popular programming language that helps us create computer programs. Think of it like learning a new language to communicate with computers.
            
            Real-life example: Just like you need to learn English or Spanish to talk to people from different countries, you need to learn Java to tell computers what to do. For example, when you use apps on your phone or browse websites, Java might be working behind the scenes.
            
            Technical example: Java is an object-oriented programming language developed by Sun Microsystems (now Oracle). It follows the principle of "write once, run anywhere" because Java code is compiled into bytecode that can run on any system with a Java Virtual Machine (JVM).
            
            Coding demonstration: Here's your first Java program:
            ```java
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            ```
            This program prints "Hello, World!" to the screen. Every Java program needs a main method as its starting point.
            """;
    }
    
    private String createJavaVariablesContent() {
        return """
            Simple explanation: Variables are like containers that store information in your program. Just like you might have different boxes to store different things at home.
            
            Real-life example: Think of variables like labeled boxes in your room. You might have a box labeled "Books" for your textbooks, another labeled "Clothes" for your shirts, and one labeled "Money" for your cash. In programming, variables work the same way - they're labeled containers for different types of information.
            
            Technical example: In Java, variables have specific data types that determine what kind of information they can store. Common data types include int (whole numbers), double (decimal numbers), String (text), and boolean (true/false values).
            
            Coding demonstration: Here's how to declare and use variables:
            ```java
            public class Variables {
                public static void main(String[] args) {
                    int age = 25;           // Whole number
                    double height = 5.9;    // Decimal number
                    String name = "Alice";  // Text
                    boolean isStudent = true; // True or false
                    
                    System.out.println("Name: " + name);
                    System.out.println("Age: " + age);
                }
            }
            ```
            """;
    }
    
    private String createArrayIntroContent() {
        return """
            Simple explanation: An array is like a row of boxes, all connected together, where each box can hold one piece of information. All boxes in the row must hold the same type of information.
            
            Real-life example: Think of an array like a parking lot with numbered spaces. Each parking space (array element) can hold one car (data), and you can find any car by knowing its parking space number (index). Just like parking spaces are numbered 1, 2, 3, arrays are numbered 0, 1, 2.
            
            Technical example: Arrays are data structures that store multiple elements of the same data type in contiguous memory locations. They provide constant-time access to elements using an index, making them efficient for storing and retrieving data.
            
            Coding demonstration: Here's how to create and use arrays:
            ```java
            public class ArrayExample {
                public static void main(String[] args) {
                    // Create an array of 5 integers
                    int[] numbers = {10, 20, 30, 40, 50};
                    
                    // Access elements by index
                    System.out.println("First element: " + numbers[0]);
                    System.out.println("Third element: " + numbers[2]);
                    
                    // Print all elements
                    for (int i = 0; i < numbers.length; i++) {
                        System.out.println("Element " + i + ": " + numbers[i]);
                    }
                }
            }
            ```
            """;
    }
}