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
            initializeFullStackLessons();
            initializeLogicalReasoningLessons();
            initializeInterviewPrepLessons();
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

        // Java Lesson 3: Control Structures - If Statements
        Lesson javaIfStatements = createLesson(
            "Control Structures - If Statements",
            "Java",
            3,
            createJavaIfStatementsContent(),
            "Learn how to make decisions in your programs using if statements",
            Lesson.Difficulty.BEGINNER,
            55,
            List.of(javaVariables.getId())
        );
        
        CheckpointQuestion checkpoint3 = new CheckpointQuestion(
            javaIfStatements,
            "What happens if the condition in an if statement is false?",
            "The code inside the if block is skipped",
            "When an if condition evaluates to false, the program skips the code inside the if block and continues with the next statement after the if block."
        );
        checkpoint3.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint3.setSequenceOrder(1);
        javaIfStatements.getCheckpointQuestions().add(checkpoint3);
        
        PracticeQuestion practice4 = new PracticeQuestion(
            javaIfStatements,
            "Write a program that checks if a number is positive, negative, or zero.",
            "int number = 5;\nif (number > 0) {\n    System.out.println(\"Positive\");\n} else if (number < 0) {\n    System.out.println(\"Negative\");\n} else {\n    System.out.println(\"Zero\");\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice4.setHints("Use if-else if-else structure to check all three conditions");
        practice4.setSequenceOrder(1);
        javaIfStatements.getPracticeQuestions().add(practice4);
        
        lessonRepository.save(javaIfStatements);

        // Java Lesson 4: Loops - For and While
        Lesson javaLoops = createLesson(
            "Loops - For and While",
            "Java",
            4,
            createJavaLoopsContent(),
            "Learn how to repeat code using for and while loops",
            Lesson.Difficulty.BEGINNER,
            65,
            List.of(javaIfStatements.getId())
        );
        
        CheckpointQuestion checkpoint4 = new CheckpointQuestion(
            javaLoops,
            "What is the difference between a for loop and a while loop?",
            "For loops are best when you know how many times to repeat; while loops are best when you repeat until a condition changes",
            "For loops are typically used when you know the exact number of iterations, while while loops are used when you need to repeat until a certain condition becomes false."
        );
        checkpoint4.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint4.setSequenceOrder(1);
        javaLoops.getCheckpointQuestions().add(checkpoint4);
        
        PracticeQuestion practice5 = new PracticeQuestion(
            javaLoops,
            "Write a program that prints numbers from 1 to 10 using a for loop.",
            "for (int i = 1; i <= 10; i++) {\n    System.out.println(i);\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice5.setHints("Use i <= 10 as the condition and i++ to increment");
        practice5.setSequenceOrder(1);
        javaLoops.getPracticeQuestions().add(practice5);
        
        lessonRepository.save(javaLoops);

        // Java Lesson 5: Methods and Functions
        Lesson javaMethods = createLesson(
            "Methods and Functions",
            "Java",
            5,
            createJavaMethodsContent(),
            "Learn how to create reusable code blocks with methods",
            Lesson.Difficulty.INTERMEDIATE,
            70,
            List.of(javaLoops.getId())
        );
        
        CheckpointQuestion checkpoint5 = new CheckpointQuestion(
            javaMethods,
            "What is the purpose of the 'return' keyword in a method?",
            "To send a value back to the code that called the method",
            "The return keyword is used to send a value back to the caller and immediately exit the method. If a method has a return type other than void, it must return a value of that type."
        );
        checkpoint5.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint5.setSequenceOrder(1);
        javaMethods.getCheckpointQuestions().add(checkpoint5);
        
        PracticeQuestion practice6 = new PracticeQuestion(
            javaMethods,
            "Create a method that takes two integers and returns their sum.",
            "public static int addNumbers(int a, int b) {\n    return a + b;\n}\n\npublic static void main(String[] args) {\n    int result = addNumbers(5, 3);\n    System.out.println(\"Sum: \" + result);\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice6.setHints("Use 'public static int' for the method signature and 'return a + b;'");
        practice6.setSequenceOrder(1);
        javaMethods.getPracticeQuestions().add(practice6);
        
        lessonRepository.save(javaMethods);

        // Java Lesson 6: Object-Oriented Programming Basics
        Lesson javaOOP = createLesson(
            "Object-Oriented Programming Basics",
            "Java",
            6,
            createJavaOOPContent(),
            "Introduction to classes, objects, and encapsulation",
            Lesson.Difficulty.INTERMEDIATE,
            80,
            List.of(javaMethods.getId())
        );
        
        CheckpointQuestion checkpoint6 = new CheckpointQuestion(
            javaOOP,
            "What is the difference between a class and an object?",
            "A class is a blueprint or template; an object is an instance created from that class",
            "A class defines the structure and behavior (attributes and methods), while an object is a specific instance of that class with actual values for the attributes."
        );
        checkpoint6.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint6.setSequenceOrder(1);
        javaOOP.getCheckpointQuestions().add(checkpoint6);
        
        PracticeQuestion practice7 = new PracticeQuestion(
            javaOOP,
            "Create a simple Person class with name and age attributes, and a method to introduce themselves.",
            "public class Person {\n    private String name;\n    private int age;\n    \n    public Person(String name, int age) {\n        this.name = name;\n        this.age = age;\n    }\n    \n    public void introduce() {\n        System.out.println(\"Hi, I'm \" + name + \" and I'm \" + age + \" years old.\");\n    }\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice7.setHints("Use private attributes, a constructor, and a public method");
        practice7.setSequenceOrder(1);
        javaOOP.getPracticeQuestions().add(practice7);
        
        lessonRepository.save(javaOOP);
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

        // DSA Lesson 2: Array Searching
        Lesson arraySearching = createLesson(
            "Array Searching - Linear Search",
            "DSA",
            2,
            createArraySearchingContent(),
            "Learn how to search for elements in an array",
            Lesson.Difficulty.BEGINNER,
            55,
            List.of(arrayIntro.getId())
        );
        
        CheckpointQuestion checkpoint7 = new CheckpointQuestion(
            arraySearching,
            "What is the time complexity of linear search in the worst case?",
            "O(n)",
            "In the worst case, linear search must check every element in the array, so it takes O(n) time where n is the number of elements."
        );
        checkpoint7.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint7.setSequenceOrder(1);
        arraySearching.getCheckpointQuestions().add(checkpoint7);
        
        PracticeQuestion practice8 = new PracticeQuestion(
            arraySearching,
            "Write a method that searches for a target value in an array and returns its index, or -1 if not found.",
            "public static int linearSearch(int[] arr, int target) {\n    for (int i = 0; i < arr.length; i++) {\n        if (arr[i] == target) {\n            return i;\n        }\n    }\n    return -1;\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice8.setHints("Loop through the array and compare each element with the target");
        practice8.setSequenceOrder(1);
        arraySearching.getPracticeQuestions().add(practice8);
        
        lessonRepository.save(arraySearching);

        // DSA Lesson 3: Introduction to Linked Lists
        Lesson linkedListIntro = createLesson(
            "Introduction to Linked Lists",
            "DSA",
            3,
            createLinkedListContent(),
            "Understand linked lists and their advantages over arrays",
            Lesson.Difficulty.INTERMEDIATE,
            60,
            List.of(arraySearching.getId())
        );
        
        CheckpointQuestion checkpoint8 = new CheckpointQuestion(
            linkedListIntro,
            "What is the main advantage of a linked list over an array?",
            "Dynamic size and efficient insertion/deletion",
            "Linked lists can grow or shrink dynamically, and inserting or deleting elements doesn't require shifting other elements like in arrays."
        );
        checkpoint8.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint8.setSequenceOrder(1);
        linkedListIntro.getCheckpointQuestions().add(checkpoint8);
        
        PracticeQuestion practice9 = new PracticeQuestion(
            linkedListIntro,
            "Create a simple Node class for a singly linked list with integer data.",
            "class Node {\n    int data;\n    Node next;\n    \n    Node(int data) {\n        this.data = data;\n        this.next = null;\n    }\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice9.setHints("A node needs data and a reference to the next node");
        practice9.setSequenceOrder(1);
        linkedListIntro.getPracticeQuestions().add(practice9);
        
        lessonRepository.save(linkedListIntro);

        // DSA Lesson 4: Stacks and Queues
        Lesson stacksQueues = createLesson(
            "Stacks and Queues",
            "DSA",
            4,
            createStacksQueuesContent(),
            "Learn about stack and queue data structures",
            Lesson.Difficulty.INTERMEDIATE,
            65,
            List.of(linkedListIntro.getId())
        );
        
        CheckpointQuestion checkpoint9 = new CheckpointQuestion(
            stacksQueues,
            "What is the difference between LIFO and FIFO?",
            "LIFO (Last In First Out) is for stacks; FIFO (First In First Out) is for queues",
            "LIFO means the last element added is the first one removed (like a stack of plates). FIFO means the first element added is the first one removed (like a line of people)."
        );
        checkpoint9.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint9.setSequenceOrder(1);
        stacksQueues.getCheckpointQuestions().add(checkpoint9);
        
        PracticeQuestion practice10 = new PracticeQuestion(
            stacksQueues,
            "Implement a simple stack using an ArrayList with push and pop operations.",
            "import java.util.ArrayList;\n\nclass Stack {\n    private ArrayList<Integer> items = new ArrayList<>();\n    \n    public void push(int item) {\n        items.add(item);\n    }\n    \n    public int pop() {\n        if (items.isEmpty()) throw new RuntimeException(\"Stack is empty\");\n        return items.remove(items.size() - 1);\n    }\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice10.setHints("Use ArrayList's add() for push and remove(size-1) for pop");
        practice10.setSequenceOrder(1);
        stacksQueues.getPracticeQuestions().add(practice10);
        
        lessonRepository.save(stacksQueues);

        // DSA Lesson 5: Binary Search Trees
        Lesson binarySearchTrees = createLesson(
            "Binary Search Trees",
            "DSA",
            5,
            createBinarySearchTreeContent(),
            "Introduction to tree data structures and BST operations",
            Lesson.Difficulty.ADVANCED,
            75,
            List.of(stacksQueues.getId())
        );
        
        CheckpointQuestion checkpoint10 = new CheckpointQuestion(
            binarySearchTrees,
            "In a binary search tree, where are smaller values located relative to a node?",
            "In the left subtree",
            "In a BST, all values in the left subtree are smaller than the node's value, and all values in the right subtree are larger."
        );
        checkpoint10.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint10.setSequenceOrder(1);
        binarySearchTrees.getCheckpointQuestions().add(checkpoint10);
        
        PracticeQuestion practice11 = new PracticeQuestion(
            binarySearchTrees,
            "Create a TreeNode class for a binary search tree.",
            "class TreeNode {\n    int value;\n    TreeNode left;\n    TreeNode right;\n    \n    TreeNode(int value) {\n        this.value = value;\n        this.left = null;\n        this.right = null;\n    }\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice11.setHints("A tree node needs a value and references to left and right children");
        practice11.setSequenceOrder(1);
        binarySearchTrees.getPracticeQuestions().add(practice11);
        
        lessonRepository.save(binarySearchTrees);
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

    // Additional content creation methods for Java
    private String createJavaIfStatementsContent() {
        return """
            Simple explanation: If statements help your program make decisions. They're like asking "Is this true?" and then doing different things based on the answer.
            
            Real-life example: Think of if statements like traffic lights. If the light is green, you go. If it's red, you stop. If it's yellow, you slow down. Your program can make similar decisions based on different conditions.
            
            Technical example: If statements use boolean expressions (true/false conditions) to control program flow. They can be combined with else if and else clauses to handle multiple conditions.
            
            Coding demonstration: Here's how to use if statements:
            ```java
            public class IfExample {
                public static void main(String[] args) {
                    int temperature = 75;
                    
                    if (temperature > 80) {
                        System.out.println("It's hot outside!");
                    } else if (temperature > 60) {
                        System.out.println("Nice weather!");
                    } else {
                        System.out.println("It's cold outside!");
                    }
                }
            }
            ```
            """;
    }

    private String createJavaLoopsContent() {
        return """
            Simple explanation: Loops help you repeat the same action multiple times without writing the same code over and over again.
            
            Real-life example: Think of loops like doing jumping jacks. Instead of saying "jump 1, jump 2, jump 3..." up to 20, you can say "do jumping jacks 20 times." Loops work the same way in programming.
            
            Technical example: For loops are ideal when you know exactly how many times to repeat. While loops are better when you need to repeat until some condition changes.
            
            Coding demonstration: Here are both types of loops:
            ```java
            public class LoopExample {
                public static void main(String[] args) {
                    // For loop - count from 1 to 5
                    for (int i = 1; i <= 5; i++) {
                        System.out.println("Count: " + i);
                    }
                    
                    // While loop - keep going until condition is false
                    int number = 1;
                    while (number <= 3) {
                        System.out.println("Number: " + number);
                        number++;
                    }
                }
            }
            ```
            """;
    }

    private String createJavaMethodsContent() {
        return """
            Simple explanation: Methods are like mini-programs inside your main program. They help you organize your code and avoid repeating yourself.
            
            Real-life example: Think of methods like recipes. Instead of writing out all the steps to make a sandwich every time you're hungry, you can just say "make sandwich" and follow the recipe. Methods work the same way - you define them once and use them many times.
            
            Technical example: Methods encapsulate functionality and can accept parameters (inputs) and return values (outputs). They promote code reusability and better organization.
            
            Coding demonstration: Here's how to create and use methods:
            ```java
            public class MethodExample {
                public static void main(String[] args) {
                    greetUser("Alice");
                    int result = addNumbers(5, 3);
                    System.out.println("Sum: " + result);
                }
                
                public static void greetUser(String name) {
                    System.out.println("Hello, " + name + "!");
                }
                
                public static int addNumbers(int a, int b) {
                    return a + b;
                }
            }
            ```
            """;
    }

    private String createJavaOOPContent() {
        return """
            Simple explanation: Object-Oriented Programming (OOP) is like creating blueprints for things and then making actual things from those blueprints.
            
            Real-life example: Think of a car blueprint. The blueprint shows what every car should have (wheels, engine, doors), but it's not a real car. When you build actual cars from this blueprint, each car is an object with its own color, model, and features.
            
            Technical example: Classes are templates that define attributes (data) and methods (behavior). Objects are instances of classes with specific values for the attributes.
            
            Coding demonstration: Here's a simple class and object:
            ```java
            public class Car {
                private String color;
                private String model;
                
                public Car(String color, String model) {
                    this.color = color;
                    this.model = model;
                }
                
                public void start() {
                    System.out.println("The " + color + " " + model + " is starting!");
                }
                
                public static void main(String[] args) {
                    Car myCar = new Car("red", "Toyota");
                    myCar.start();
                }
            }
            ```
            """;
    }

    // Content creation methods for DSA
    private String createArraySearchingContent() {
        return """
            Simple explanation: Searching in an array means looking through all the elements to find a specific value, like looking through a list of names to find your friend.
            
            Real-life example: Imagine you have a row of books and you're looking for a specific title. You start from the first book and check each one until you find the book you want. That's exactly how linear search works.
            
            Technical example: Linear search examines each element sequentially until the target is found or all elements are checked. It has O(n) time complexity in the worst case.
            
            Coding demonstration: Here's how to implement linear search:
            ```java
            public class SearchExample {
                public static void main(String[] args) {
                    int[] numbers = {10, 25, 30, 45, 50};
                    int target = 30;
                    int result = linearSearch(numbers, target);
                    
                    if (result != -1) {
                        System.out.println("Found " + target + " at index " + result);
                    } else {
                        System.out.println(target + " not found");
                    }
                }
                
                public static int linearSearch(int[] arr, int target) {
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i] == target) {
                            return i;
                        }
                    }
                    return -1;
                }
            }
            ```
            """;
    }

    private String createLinkedListContent() {
        return """
            Simple explanation: A linked list is like a chain where each link points to the next one. Unlike arrays where elements are next to each other, linked list elements can be anywhere in memory.
            
            Real-life example: Think of a treasure hunt where each clue tells you where to find the next clue. You start with the first clue, which leads to the second, which leads to the third, and so on. That's how a linked list works.
            
            Technical example: Linked lists consist of nodes, where each node contains data and a reference to the next node. They allow dynamic size and efficient insertion/deletion but require sequential access.
            
            Coding demonstration: Here's a simple linked list implementation:
            ```java
            class Node {
                int data;
                Node next;
                
                Node(int data) {
                    this.data = data;
                    this.next = null;
                }
            }
            
            public class LinkedListExample {
                public static void main(String[] args) {
                    Node first = new Node(10);
                    Node second = new Node(20);
                    Node third = new Node(30);
                    
                    first.next = second;
                    second.next = third;
                    
                    // Print the list
                    Node current = first;
                    while (current != null) {
                        System.out.println(current.data);
                        current = current.next;
                    }
                }
            }
            ```
            """;
    }

    private String createStacksQueuesContent() {
        return """
            Simple explanation: Stacks and queues are ways to organize data. A stack is like a pile of plates (last one on, first one off), and a queue is like a line of people (first one in, first one out).
            
            Real-life example: Stack: Think of a stack of books. You can only add or remove books from the top. Queue: Think of a line at a coffee shop. The first person in line is the first person served.
            
            Technical example: Stacks follow LIFO (Last In, First Out) principle with push and pop operations. Queues follow FIFO (First In, First Out) principle with enqueue and dequeue operations.
            
            Coding demonstration: Here's how to implement both:
            ```java
            import java.util.*;
            
            public class StackQueueExample {
                public static void main(String[] args) {
                    // Stack example
                    Stack<Integer> stack = new Stack<>();
                    stack.push(1);
                    stack.push(2);
                    stack.push(3);
                    System.out.println("Stack pop: " + stack.pop()); // Prints 3
                    
                    // Queue example
                    Queue<Integer> queue = new LinkedList<>();
                    queue.offer(1);
                    queue.offer(2);
                    queue.offer(3);
                    System.out.println("Queue poll: " + queue.poll()); // Prints 1
                }
            }
            ```
            """;
    }

    private String createBinarySearchTreeContent() {
        return """
            Simple explanation: A binary search tree is like a family tree, but organized so that smaller values go to the left and larger values go to the right.
            
            Real-life example: Think of organizing a library. You put books with titles starting with A-M on the left side and N-Z on the right side. Then you do the same thing for each section, creating a tree-like organization.
            
            Technical example: BSTs maintain the property that for any node, all values in the left subtree are smaller and all values in the right subtree are larger. This enables efficient searching, insertion, and deletion.
            
            Coding demonstration: Here's a simple BST implementation:
            ```java
            class TreeNode {
                int value;
                TreeNode left, right;
                
                TreeNode(int value) {
                    this.value = value;
                    left = right = null;
                }
            }
            
            public class BSTExample {
                public static void main(String[] args) {
                    TreeNode root = new TreeNode(50);
                    root.left = new TreeNode(30);
                    root.right = new TreeNode(70);
                    root.left.left = new TreeNode(20);
                    root.left.right = new TreeNode(40);
                    
                    inorderTraversal(root);
                }
                
                public static void inorderTraversal(TreeNode node) {
                    if (node != null) {
                        inorderTraversal(node.left);
                        System.out.println(node.value);
                        inorderTraversal(node.right);
                    }
                }
            }
            ```
            """;
    }

    // Full Stack Development curriculum
    private void initializeFullStackLessons() {
        // Full Stack Lesson 1: HTML Fundamentals
        Lesson htmlFundamentals = createLesson(
            "HTML Fundamentals",
            "Full Stack",
            1,
            createHTMLFundamentalsContent(),
            "Learn the basics of HTML structure and common elements",
            Lesson.Difficulty.BEGINNER,
            50,
            null
        );
        
        CheckpointQuestion checkpoint11 = new CheckpointQuestion(
            htmlFundamentals,
            "What does HTML stand for?",
            "HyperText Markup Language",
            "HTML stands for HyperText Markup Language. It's the standard markup language used to create web pages."
        );
        checkpoint11.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint11.setSequenceOrder(1);
        htmlFundamentals.getCheckpointQuestions().add(checkpoint11);
        
        PracticeQuestion practice12 = new PracticeQuestion(
            htmlFundamentals,
            "Create a basic HTML page with a title, heading, and paragraph.",
            "<!DOCTYPE html>\n<html>\n<head>\n    <title>My First Page</title>\n</head>\n<body>\n    <h1>Welcome to My Website</h1>\n    <p>This is my first HTML page!</p>\n</body>\n</html>",
            PracticeQuestion.QuestionType.CODING
        );
        practice12.setHints("Use <!DOCTYPE html>, <html>, <head>, <title>, <body>, <h1>, and <p> tags");
        practice12.setSequenceOrder(1);
        htmlFundamentals.getPracticeQuestions().add(practice12);
        
        lessonRepository.save(htmlFundamentals);

        // Full Stack Lesson 2: CSS Styling
        Lesson cssStyling = createLesson(
            "CSS Styling Basics",
            "Full Stack",
            2,
            createCSSBasicsContent(),
            "Learn how to style HTML elements with CSS",
            Lesson.Difficulty.BEGINNER,
            60,
            List.of(htmlFundamentals.getId())
        );
        
        CheckpointQuestion checkpoint12 = new CheckpointQuestion(
            cssStyling,
            "What are the three ways to add CSS to an HTML page?",
            "Inline, internal, and external",
            "CSS can be added inline (style attribute), internal (style tag in head), or external (separate CSS file linked with link tag)."
        );
        checkpoint12.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint12.setSequenceOrder(1);
        cssStyling.getCheckpointQuestions().add(checkpoint12);
        
        PracticeQuestion practice13 = new PracticeQuestion(
            cssStyling,
            "Style a heading to be blue and center-aligned using CSS.",
            "<style>\nh1 {\n    color: blue;\n    text-align: center;\n}\n</style>\n<h1>Styled Heading</h1>",
            PracticeQuestion.QuestionType.CODING
        );
        practice13.setHints("Use color property for text color and text-align for alignment");
        practice13.setSequenceOrder(1);
        cssStyling.getPracticeQuestions().add(practice13);
        
        lessonRepository.save(cssStyling);

        // Full Stack Lesson 3: JavaScript Basics
        Lesson javaScriptBasics = createLesson(
            "JavaScript Fundamentals",
            "Full Stack",
            3,
            createJavaScriptBasicsContent(),
            "Introduction to JavaScript programming for web development",
            Lesson.Difficulty.BEGINNER,
            70,
            List.of(cssStyling.getId())
        );
        
        CheckpointQuestion checkpoint13 = new CheckpointQuestion(
            javaScriptBasics,
            "How do you declare a variable in JavaScript?",
            "Using var, let, or const keywords",
            "JavaScript variables can be declared using var (function-scoped), let (block-scoped), or const (block-scoped, immutable)."
        );
        checkpoint13.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint13.setSequenceOrder(1);
        javaScriptBasics.getCheckpointQuestions().add(checkpoint13);
        
        PracticeQuestion practice14 = new PracticeQuestion(
            javaScriptBasics,
            "Create a JavaScript function that adds two numbers and displays the result.",
            "function addNumbers(a, b) {\n    let result = a + b;\n    console.log('Sum: ' + result);\n    return result;\n}\n\naddNumbers(5, 3);",
            PracticeQuestion.QuestionType.CODING
        );
        practice14.setHints("Use function keyword, parameters, and console.log for output");
        practice14.setSequenceOrder(1);
        javaScriptBasics.getPracticeQuestions().add(practice14);
        
        lessonRepository.save(javaScriptBasics);

        // Full Stack Lesson 4: DOM Manipulation
        Lesson domManipulation = createLesson(
            "DOM Manipulation with JavaScript",
            "Full Stack",
            4,
            createDOMManipulationContent(),
            "Learn how to interact with HTML elements using JavaScript",
            Lesson.Difficulty.INTERMEDIATE,
            65,
            List.of(javaScriptBasics.getId())
        );
        
        CheckpointQuestion checkpoint14 = new CheckpointQuestion(
            domManipulation,
            "What method is used to select an element by its ID in JavaScript?",
            "document.getElementById()",
            "The document.getElementById() method returns the element with the specified ID attribute."
        );
        checkpoint14.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint14.setSequenceOrder(1);
        domManipulation.getCheckpointQuestions().add(checkpoint14);
        
        PracticeQuestion practice15 = new PracticeQuestion(
            domManipulation,
            "Create a button that changes the text of a paragraph when clicked.",
            "<p id=\"myParagraph\">Original text</p>\n<button onclick=\"changeText()\">Click me</button>\n\n<script>\nfunction changeText() {\n    document.getElementById('myParagraph').innerHTML = 'Text changed!';\n}\n</script>",
            PracticeQuestion.QuestionType.CODING
        );
        practice15.setHints("Use getElementById() and innerHTML property");
        practice15.setSequenceOrder(1);
        domManipulation.getPracticeQuestions().add(practice15);
        
        lessonRepository.save(domManipulation);

        // Full Stack Lesson 5: Introduction to Backend Development
        Lesson backendIntro = createLesson(
            "Introduction to Backend Development",
            "Full Stack",
            5,
            createBackendIntroContent(),
            "Understanding server-side development and APIs",
            Lesson.Difficulty.INTERMEDIATE,
            75,
            List.of(domManipulation.getId())
        );
        
        CheckpointQuestion checkpoint15 = new CheckpointQuestion(
            backendIntro,
            "What is the difference between frontend and backend development?",
            "Frontend is what users see and interact with; backend is server-side logic and data management",
            "Frontend handles the user interface and user experience, while backend manages server logic, databases, and API endpoints."
        );
        checkpoint15.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint15.setSequenceOrder(1);
        backendIntro.getCheckpointQuestions().add(checkpoint15);
        
        PracticeQuestion practice16 = new PracticeQuestion(
            backendIntro,
            "Explain what an API is and give an example of how it might be used.",
            "An API (Application Programming Interface) is a way for different software applications to communicate. Example: A weather app uses a weather API to get current temperature data from a weather service.",
            PracticeQuestion.QuestionType.SHORT_ANSWER
        );
        practice16.setHints("Think about how apps get data from external services");
        practice16.setSequenceOrder(1);
        backendIntro.getPracticeQuestions().add(practice16);
        
        lessonRepository.save(backendIntro);
    }

    // Logical Reasoning & Aptitude curriculum
    private void initializeLogicalReasoningLessons() {
        // Logical Reasoning Lesson 1: Pattern Recognition
        Lesson patternRecognition = createLesson(
            "Pattern Recognition",
            "Logical Reasoning",
            1,
            createPatternRecognitionContent(),
            "Learn to identify and continue patterns in sequences",
            Lesson.Difficulty.BEGINNER,
            45,
            null
        );
        
        CheckpointQuestion checkpoint16 = new CheckpointQuestion(
            patternRecognition,
            "What comes next in the sequence: 2, 4, 6, 8, ?",
            "10",
            "This is an arithmetic sequence where each number increases by 2. So 8 + 2 = 10."
        );
        checkpoint16.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint16.setSequenceOrder(1);
        patternRecognition.getCheckpointQuestions().add(checkpoint16);
        
        PracticeQuestion practice17 = new PracticeQuestion(
            patternRecognition,
            "Find the next number in the sequence: 1, 4, 9, 16, ?",
            "25",
            PracticeQuestion.QuestionType.SHORT_ANSWER
        );
        practice17.setHints("Look at the relationship between consecutive numbers. These are perfect squares: 1², 2², 3², 4², ?");
        practice17.setSequenceOrder(1);
        patternRecognition.getPracticeQuestions().add(practice17);
        
        lessonRepository.save(patternRecognition);

        // Logical Reasoning Lesson 2: Basic Logic Problems
        Lesson basicLogic = createLesson(
            "Basic Logic Problems",
            "Logical Reasoning",
            2,
            createBasicLogicContent(),
            "Solve simple logic puzzles using deductive reasoning",
            Lesson.Difficulty.BEGINNER,
            50,
            List.of(patternRecognition.getId())
        );
        
        CheckpointQuestion checkpoint17 = new CheckpointQuestion(
            basicLogic,
            "If all cats are animals, and Fluffy is a cat, what can we conclude about Fluffy?",
            "Fluffy is an animal",
            "This is a basic syllogism. Since all cats are animals and Fluffy is a cat, we can logically conclude that Fluffy is an animal."
        );
        checkpoint17.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint17.setSequenceOrder(1);
        basicLogic.getCheckpointQuestions().add(checkpoint17);
        
        PracticeQuestion practice18 = new PracticeQuestion(
            basicLogic,
            "Three friends - Alice, Bob, and Carol - have different favorite colors: red, blue, and green. Alice doesn't like red. Bob doesn't like blue. Carol likes green. What color does each person like?",
            "Alice likes blue, Bob likes red, Carol likes green",
            PracticeQuestion.QuestionType.SHORT_ANSWER
        );
        practice18.setHints("Use process of elimination. Start with what you know for certain and work from there.");
        practice18.setSequenceOrder(1);
        basicLogic.getPracticeQuestions().add(practice18);
        
        lessonRepository.save(basicLogic);

        // Logical Reasoning Lesson 3: Mathematical Reasoning
        Lesson mathReasoning = createLesson(
            "Mathematical Reasoning",
            "Logical Reasoning",
            3,
            createMathReasoningContent(),
            "Apply logical thinking to mathematical problems",
            Lesson.Difficulty.INTERMEDIATE,
            55,
            List.of(basicLogic.getId())
        );
        
        CheckpointQuestion checkpoint18 = new CheckpointQuestion(
            mathReasoning,
            "If a train travels 60 miles in 1 hour, how far will it travel in 2.5 hours at the same speed?",
            "150 miles",
            "Speed = 60 miles/hour. Distance = Speed × Time = 60 × 2.5 = 150 miles."
        );
        checkpoint18.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint18.setSequenceOrder(1);
        mathReasoning.getCheckpointQuestions().add(checkpoint18);
        
        PracticeQuestion practice19 = new PracticeQuestion(
            mathReasoning,
            "A store offers a 20% discount on an item that originally costs $50. What is the final price after the discount?",
            "$40",
            PracticeQuestion.QuestionType.SHORT_ANSWER
        );
        practice19.setHints("Calculate 20% of $50, then subtract that from the original price");
        practice19.setSequenceOrder(1);
        mathReasoning.getPracticeQuestions().add(practice19);
        
        lessonRepository.save(mathReasoning);
    }

    // Interview Preparation curriculum
    private void initializeInterviewPrepLessons() {
        // Interview Prep Lesson 1: Technical Interview Basics
        Lesson techInterviewBasics = createLesson(
            "Technical Interview Fundamentals",
            "Interview Prep",
            1,
            createTechInterviewBasicsContent(),
            "Understanding what to expect in technical interviews",
            Lesson.Difficulty.BEGINNER,
            60,
            null
        );
        
        CheckpointQuestion checkpoint19 = new CheckpointQuestion(
            techInterviewBasics,
            "What are the main types of questions asked in technical interviews?",
            "Coding problems, system design, behavioral questions, and technical knowledge questions",
            "Technical interviews typically include coding challenges, system design discussions, behavioral questions about past experiences, and questions about technical concepts."
        );
        checkpoint19.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint19.setSequenceOrder(1);
        techInterviewBasics.getCheckpointQuestions().add(checkpoint19);
        
        PracticeQuestion practice20 = new PracticeQuestion(
            techInterviewBasics,
            "Describe your approach to solving a coding problem you've never seen before.",
            "1. Understand the problem completely 2. Ask clarifying questions 3. Think of examples 4. Consider edge cases 5. Choose an approach 6. Code the solution 7. Test with examples",
            PracticeQuestion.QuestionType.SHORT_ANSWER
        );
        practice20.setHints("Think about the systematic approach: understand, plan, implement, test");
        practice20.setSequenceOrder(1);
        techInterviewBasics.getPracticeQuestions().add(practice20);
        
        lessonRepository.save(techInterviewBasics);

        // Interview Prep Lesson 2: Common Coding Interview Problems
        Lesson commonCodingProblems = createLesson(
            "Common Coding Interview Problems",
            "Interview Prep",
            2,
            createCommonCodingProblemsContent(),
            "Practice solving frequently asked coding interview questions",
            Lesson.Difficulty.INTERMEDIATE,
            75,
            List.of(techInterviewBasics.getId())
        );
        
        CheckpointQuestion checkpoint20 = new CheckpointQuestion(
            commonCodingProblems,
            "What is the time complexity of checking if an array contains duplicate elements using a nested loop approach?",
            "O(n²)",
            "Using nested loops to compare each element with every other element results in O(n²) time complexity, where n is the number of elements."
        );
        checkpoint20.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint20.setSequenceOrder(1);
        commonCodingProblems.getCheckpointQuestions().add(checkpoint20);
        
        PracticeQuestion practice21 = new PracticeQuestion(
            commonCodingProblems,
            "Write a function to reverse a string without using built-in reverse methods.",
            "public static String reverseString(String str) {\n    char[] chars = str.toCharArray();\n    int left = 0, right = chars.length - 1;\n    while (left < right) {\n        char temp = chars[left];\n        chars[left] = chars[right];\n        chars[right] = temp;\n        left++;\n        right--;\n    }\n    return new String(chars);\n}",
            PracticeQuestion.QuestionType.CODING
        );
        practice21.setHints("Use two pointers approach - one from start, one from end, and swap characters");
        practice21.setSequenceOrder(1);
        commonCodingProblems.getPracticeQuestions().add(practice21);
        
        lessonRepository.save(commonCodingProblems);

        // Interview Prep Lesson 3: Behavioral Interview Questions
        Lesson behavioralQuestions = createLesson(
            "Behavioral Interview Questions",
            "Interview Prep",
            3,
            createBehavioralQuestionsContent(),
            "Learn how to answer behavioral questions using the STAR method",
            Lesson.Difficulty.BEGINNER,
            50,
            List.of(commonCodingProblems.getId())
        );
        
        CheckpointQuestion checkpoint21 = new CheckpointQuestion(
            behavioralQuestions,
            "What does the STAR method stand for in behavioral interviews?",
            "Situation, Task, Action, Result",
            "STAR is a framework for answering behavioral questions: describe the Situation, explain the Task, detail the Action you took, and share the Result."
        );
        checkpoint21.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint21.setSequenceOrder(1);
        behavioralQuestions.getCheckpointQuestions().add(checkpoint21);
        
        PracticeQuestion practice22 = new PracticeQuestion(
            behavioralQuestions,
            "Using the STAR method, describe a time when you had to learn something new quickly.",
            "Situation: I needed to learn React for a project with a tight deadline. Task: Master React basics in one week. Action: I dedicated 2 hours daily to tutorials, built small projects, and asked experienced developers for guidance. Result: Successfully delivered the project on time and gained valuable React skills.",
            PracticeQuestion.QuestionType.SHORT_ANSWER
        );
        practice22.setHints("Think of a real example from your experience and structure it using Situation, Task, Action, Result");
        practice22.setSequenceOrder(1);
        behavioralQuestions.getPracticeQuestions().add(practice22);
        
        lessonRepository.save(behavioralQuestions);

        // Interview Prep Lesson 4: System Design Basics
        Lesson systemDesignBasics = createLesson(
            "System Design Interview Basics",
            "Interview Prep",
            4,
            createSystemDesignBasicsContent(),
            "Introduction to system design concepts for interviews",
            Lesson.Difficulty.ADVANCED,
            80,
            List.of(behavioralQuestions.getId())
        );
        
        CheckpointQuestion checkpoint22 = new CheckpointQuestion(
            systemDesignBasics,
            "What are the key components to consider when designing a scalable web application?",
            "Load balancers, databases, caching, microservices, and monitoring",
            "Key components include load balancers for distributing traffic, databases for data storage, caching for performance, microservices for modularity, and monitoring for system health."
        );
        checkpoint22.setQuestionType(CheckpointQuestion.QuestionType.SHORT_ANSWER);
        checkpoint22.setSequenceOrder(1);
        systemDesignBasics.getCheckpointQuestions().add(checkpoint22);
        
        PracticeQuestion practice23 = new PracticeQuestion(
            systemDesignBasics,
            "Explain how you would design a simple URL shortener service like bit.ly.",
            "1. Use a database to store original URLs and short codes 2. Generate unique short codes (base62 encoding) 3. Implement redirect service 4. Add caching for popular URLs 5. Use load balancers for scalability 6. Monitor usage and performance",
            PracticeQuestion.QuestionType.SHORT_ANSWER
        );
        practice23.setHints("Think about the core functionality first, then consider scalability, performance, and reliability");
        practice23.setSequenceOrder(1);
        systemDesignBasics.getPracticeQuestions().add(practice23);
        
        lessonRepository.save(systemDesignBasics);
    }

    // Content creation methods for Full Stack Development
    private String createHTMLFundamentalsContent() {
        return """
            Simple explanation: HTML is like the skeleton of a webpage. It provides the basic structure and tells the browser what content to display and how to organize it.
            
            Real-life example: Think of HTML like the frame of a house. Just as a house frame defines where the rooms, doors, and windows go, HTML defines where the headings, paragraphs, images, and links go on a webpage.
            
            Technical example: HTML uses tags (markup) to define elements. Tags are enclosed in angle brackets and usually come in pairs - an opening tag and a closing tag.
            
            Coding demonstration: Here's a basic HTML structure:
            ```html
            <!DOCTYPE html>
            <html>
            <head>
                <title>My First Webpage</title>
            </head>
            <body>
                <h1>Welcome to My Site</h1>
                <p>This is a paragraph of text.</p>
                <a href="https://example.com">This is a link</a>
                <img src="image.jpg" alt="Description of image">
            </body>
            </html>
            ```
            """;
    }

    private String createCSSBasicsContent() {
        return """
            Simple explanation: CSS is like the paint and decorations for your HTML house. While HTML provides the structure, CSS makes it look beautiful with colors, fonts, spacing, and layout.
            
            Real-life example: If HTML is like a black and white newspaper, CSS is like a colorful magazine. CSS adds the visual appeal - colors, fonts, spacing, and layout that makes websites attractive and easy to read.
            
            Technical example: CSS uses selectors to target HTML elements and properties to define how they should look. You can apply styles inline, internally, or through external stylesheets.
            
            Coding demonstration: Here's how to style HTML elements:
            ```css
            /* External CSS file */
            body {
                font-family: Arial, sans-serif;
                background-color: #f0f0f0;
                margin: 0;
                padding: 20px;
            }
            
            h1 {
                color: #333;
                text-align: center;
                border-bottom: 2px solid #007bff;
            }
            
            p {
                line-height: 1.6;
                color: #666;
            }
            ```
            """;
    }

    private String createJavaScriptBasicsContent() {
        return """
            Simple explanation: JavaScript is like the brain of your webpage. While HTML provides structure and CSS provides style, JavaScript adds behavior and interactivity.
            
            Real-life example: Think of a remote control for your TV. The TV (HTML) has all the components, the design (CSS) makes it look good, but the remote control (JavaScript) lets you actually interact with it - change channels, adjust volume, etc.
            
            Technical example: JavaScript is a programming language that runs in web browsers. It can manipulate HTML elements, respond to user events, and communicate with servers.
            
            Coding demonstration: Here's basic JavaScript functionality:
            ```javascript
            // Variables and functions
            let userName = "Alice";
            const greeting = "Hello";
            
            function greetUser(name) {
                return greeting + ", " + name + "!";
            }
            
            // DOM manipulation
            document.getElementById("myButton").addEventListener("click", function() {
                document.getElementById("output").innerHTML = greetUser(userName);
            });
            
            // Conditional logic
            if (userName.length > 0) {
                console.log("User name is valid");
            }
            ```
            """;
    }

    private String createDOMManipulationContent() {
        return """
            Simple explanation: DOM manipulation is how JavaScript talks to and changes the HTML on your webpage. It's like having a remote control that can rearrange furniture in your room.
            
            Real-life example: Imagine you have a smart home system that can turn lights on/off, change TV channels, and adjust temperature. DOM manipulation works similarly - it lets JavaScript control and change elements on your webpage.
            
            Technical example: The DOM (Document Object Model) represents the HTML document as a tree of objects that JavaScript can access and modify. You can select elements, change their content, add event listeners, and create new elements.
            
            Coding demonstration: Here's how to manipulate the DOM:
            ```javascript
            // Select elements
            const button = document.getElementById("myButton");
            const paragraph = document.querySelector(".my-paragraph");
            
            // Change content
            paragraph.innerHTML = "New content!";
            paragraph.style.color = "blue";
            
            // Add event listeners
            button.addEventListener("click", function() {
                paragraph.classList.toggle("highlight");
            });
            
            // Create new elements
            const newDiv = document.createElement("div");
            newDiv.textContent = "I'm a new element!";
            document.body.appendChild(newDiv);
            ```
            """;
    }

    private String createBackendIntroContent() {
        return """
            Simple explanation: Backend development is like the kitchen of a restaurant. Customers (frontend users) don't see it, but it's where all the food preparation (data processing) happens before being served.
            
            Real-life example: When you order food through an app, the frontend shows you the menu and takes your order. The backend processes your payment, sends the order to the restaurant, tracks delivery, and updates your order status.
            
            Technical example: Backend development involves server-side programming, databases, APIs, and server management. It handles business logic, data storage, authentication, and communication between different parts of an application.
            
            Coding demonstration: Here's a simple backend concept:
            ```javascript
            // Simple API endpoint (Node.js/Express example)
            app.get('/api/users/:id', (req, res) => {
                const userId = req.params.id;
                
                // Get user from database
                const user = database.findUser(userId);
                
                if (user) {
                    res.json({
                        success: true,
                        data: user
                    });
                } else {
                    res.status(404).json({
                        success: false,
                        message: "User not found"
                    });
                }
            });
            ```
            """;
    }

    // Content creation methods for Logical Reasoning
    private String createPatternRecognitionContent() {
        return """
            Simple explanation: Pattern recognition is like being a detective who notices clues that repeat. You look for what stays the same or changes in a predictable way.
            
            Real-life example: Think about your daily routine. You might notice patterns like: wake up, brush teeth, eat breakfast, go to work. Or seasonal patterns: spring brings flowers, summer brings heat, fall brings falling leaves.
            
            Technical example: Patterns can be arithmetic (adding/subtracting the same number), geometric (multiplying/dividing by the same number), or based on position, shape, or other properties.
            
            Coding demonstration: Here's how to identify patterns programmatically:
            ```java
            public class PatternRecognition {
                public static void main(String[] args) {
                    int[] sequence = {2, 4, 6, 8, 10};
                    
                    // Check if it's an arithmetic sequence
                    int difference = sequence[1] - sequence[0];
                    boolean isArithmetic = true;
                    
                    for (int i = 2; i < sequence.length; i++) {
                        if (sequence[i] - sequence[i-1] != difference) {
                            isArithmetic = false;
                            break;
                        }
                    }
                    
                    if (isArithmetic) {
                        int nextNumber = sequence[sequence.length - 1] + difference;
                        System.out.println("Next number: " + nextNumber);
                    }
                }
            }
            ```
            """;
    }

    private String createBasicLogicContent() {
        return """
            Simple explanation: Logic problems are like puzzles where you use clues to figure out the answer. You eliminate impossible options and use what you know to find what you don't know.
            
            Real-life example: Imagine you're trying to figure out who ate the last cookie. You know: Mom was at work, Dad doesn't like cookies, and your sister was in her room studying. By eliminating the impossible options, you can deduce who the cookie thief was!
            
            Technical example: Logic problems often use deductive reasoning (if A is true and B is true, then C must be true) and process of elimination to reach conclusions.
            
            Coding demonstration: Here's a simple logic solver:
            ```java
            public class LogicSolver {
                public static void main(String[] args) {
                    // Three people: Alice, Bob, Carol
                    // Three colors: Red, Blue, Green
                    // Clues: Alice doesn't like Red, Bob doesn't like Blue, Carol likes Green
                    
                    String[] people = {"Alice", "Bob", "Carol"};
                    String[] colors = {"Red", "Blue", "Green"};
                    
                    // Carol likes Green (given)
                    System.out.println("Carol likes Green");
                    
                    // Alice doesn't like Red, so Alice likes Blue (only option left)
                    System.out.println("Alice likes Blue");
                    
                    // Bob doesn't like Blue, Carol has Green, so Bob likes Red
                    System.out.println("Bob likes Red");
                }
            }
            ```
            """;
    }

    private String createMathReasoningContent() {
        return """
            Simple explanation: Mathematical reasoning is using logic to solve number problems. It's like being a math detective who uses clues (given information) to find the answer.
            
            Real-life example: If you're planning a pizza party and each pizza feeds 4 people, and you have 12 guests, you can reason that you need 3 pizzas (12 ÷ 4 = 3).
            
            Technical example: Mathematical reasoning involves identifying what you know, what you need to find, choosing the right operations, and checking if your answer makes sense.
            
            Coding demonstration: Here's a mathematical reasoning approach:
            ```java
            public class MathReasoning {
                public static void main(String[] args) {
                    // Problem: A car travels 60 mph for 2.5 hours. How far does it go?
                    
                    double speed = 60; // miles per hour
                    double time = 2.5; // hours
                    
                    // Formula: Distance = Speed × Time
                    double distance = speed * time;
                    
                    System.out.println("Speed: " + speed + " mph");
                    System.out.println("Time: " + time + " hours");
                    System.out.println("Distance: " + distance + " miles");
                    
                    // Check: Does this make sense?
                    // 60 mph for 2.5 hours should be more than 60 but less than 180
                    if (distance > 60 && distance < 180) {
                        System.out.println("Answer seems reasonable!");
                    }
                }
            }
            ```
            """;
    }

    // Content creation methods for Interview Preparation
    private String createTechInterviewBasicsContent() {
        return """
            Simple explanation: Technical interviews are like practical exams where companies test your programming skills and problem-solving abilities to see if you can do the job.
            
            Real-life example: Think of it like a driving test. You've learned the rules and practiced driving, but now you need to demonstrate your skills to an examiner who will decide if you're ready to drive independently.
            
            Technical example: Technical interviews typically include coding challenges (solve problems on a whiteboard or computer), system design questions (how would you build a large application), and behavioral questions (tell me about a time when...).
            
            Coding demonstration: Here's a typical interview problem-solving approach:
            ```java
            // Problem: Find the maximum number in an array
            public class InterviewExample {
                public static int findMaximum(int[] arr) {
                    // Step 1: Handle edge cases
                    if (arr == null || arr.length == 0) {
                        throw new IllegalArgumentException("Array cannot be empty");
                    }
                    
                    // Step 2: Initialize with first element
                    int max = arr[0];
                    
                    // Step 3: Compare with remaining elements
                    for (int i = 1; i < arr.length; i++) {
                        if (arr[i] > max) {
                            max = arr[i];
                        }
                    }
                    
                    // Step 4: Return result
                    return max;
                }
                
                // Step 5: Test with examples
                public static void main(String[] args) {
                    int[] test = {3, 7, 2, 9, 1};
                    System.out.println("Maximum: " + findMaximum(test)); // Should print 9
                }
            }
            ```
            """;
    }

    private String createCommonCodingProblemsContent() {
        return """
            Simple explanation: Common coding problems are like popular recipes that many chefs know how to make. Interviewers ask these because they test fundamental programming skills.
            
            Real-life example: Just like learning to cook starts with basic recipes (scrambled eggs, pasta), programming interviews start with basic problems (reverse a string, find duplicates in an array).
            
            Technical example: Common problems include string manipulation, array operations, linked list traversal, tree operations, and basic algorithms like sorting and searching.
            
            Coding demonstration: Here are some classic problems:
            ```java
            public class CommonProblems {
                // Problem 1: Check if a string is a palindrome
                public static boolean isPalindrome(String str) {
                    str = str.toLowerCase().replaceAll("[^a-z0-9]", "");
                    int left = 0, right = str.length() - 1;
                    
                    while (left < right) {
                        if (str.charAt(left) != str.charAt(right)) {
                            return false;
                        }
                        left++;
                        right--;
                    }
                    return true;
                }
                
                // Problem 2: Find two numbers that sum to target
                public static int[] twoSum(int[] nums, int target) {
                    Map<Integer, Integer> map = new HashMap<>();
                    
                    for (int i = 0; i < nums.length; i++) {
                        int complement = target - nums[i];
                        if (map.containsKey(complement)) {
                            return new int[]{map.get(complement), i};
                        }
                        map.put(nums[i], i);
                    }
                    return new int[]{};
                }
            }
            ```
            """;
    }

    private String createBehavioralQuestionsContent() {
        return """
            Simple explanation: Behavioral questions are like asking for stories about your past experiences to predict how you'll behave in future situations.
            
            Real-life example: It's like when a friend asks "Tell me about a time you helped someone" to understand what kind of person you are. Employers ask similar questions to understand how you work with others and handle challenges.
            
            Technical example: The STAR method helps structure your answers: Situation (context), Task (what needed to be done), Action (what you did), Result (what happened).
            
            Coding demonstration: Here's how to structure a STAR response:
            ```
            Question: "Tell me about a time you had to debug a difficult problem."
            
            Situation: "In my last project, our web application was crashing randomly in production, but we couldn't reproduce the issue in our development environment."
            
            Task: "I needed to identify the root cause and fix the issue quickly since it was affecting our users."
            
            Action: "I analyzed the server logs, added more detailed logging to track user actions, and discovered the crash happened when users uploaded files larger than 10MB. I implemented proper file size validation and error handling."
            
            Result: "The crashes stopped completely, and we prevented similar issues by adding file size limits. I also created a monitoring dashboard to catch such issues earlier in the future."
            ```
            """;
    }

    private String createSystemDesignBasicsContent() {
        return """
            Simple explanation: System design is like being an architect who plans how to build a large building. You need to think about the foundation, structure, utilities, and how everything works together.
            
            Real-life example: Designing a system is like planning a city. You need roads (network), power plants (servers), water systems (databases), traffic lights (load balancers), and emergency services (monitoring) all working together.
            
            Technical example: System design involves understanding requirements, estimating scale, choosing appropriate technologies, and designing for reliability, scalability, and performance.
            
            Coding demonstration: Here's a high-level system design approach:
            ```
            Problem: Design a URL shortener like bit.ly
            
            1. Requirements:
               - Shorten long URLs
               - Redirect short URLs to original
               - Handle millions of URLs
               - Fast response times
            
            2. High-level design:
               [Client] → [Load Balancer] → [Web Servers] → [Database]
                                        ↓
                                    [Cache Layer]
            
            3. Database schema:
               URLs table:
               - id (primary key)
               - original_url (text)
               - short_code (varchar, indexed)
               - created_at (timestamp)
               - click_count (integer)
            
            4. Algorithm for short code:
               - Use base62 encoding (a-z, A-Z, 0-9)
               - Convert database ID to base62 string
               - Example: ID 12345 → short code "dnh"
            
            5. Scalability considerations:
               - Use caching for popular URLs
               - Database sharding for large datasets
               - CDN for global distribution
               - Rate limiting to prevent abuse
            ```
            """;
    }
}