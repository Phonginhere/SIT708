package com.example.llm61.data

// Hardcoded question pool keyed by topic.
// In a real app, this would come from a backend or database.
object QuestionBank {

    private val questions: Map<String, List<Question>> = mapOf(
        "Algorithms" to listOf(
            Question(1, "What is the time complexity of binary search?",
                listOf("O(n)", "O(log n)", "O(n²)", "O(1)"), 1),
            Question(2, "Which sorting algorithm has the best average case time complexity?",
                listOf("Bubble Sort", "Quick Sort", "Selection Sort", "Insertion Sort"), 1),
            Question(3, "What does BFS stand for?",
                listOf("Best First Search", "Breadth First Search", "Binary First Search", "Backward First Search"), 1)
        ),
        "Data Structures" to listOf(
            Question(1, "Which data structure uses LIFO order?",
                listOf("Queue", "Stack", "Linked List", "Tree"), 1),
            Question(2, "What is the worst-case time to insert into a hash table?",
                listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"), 2),
            Question(3, "A binary tree node has at most how many children?",
                listOf("1", "2", "3", "Unlimited"), 1)
        ),
        "Web Development" to listOf(
            Question(1, "What does HTML stand for?",
                listOf("Hyper Text Markup Language", "Home Tool Markup Language", "Hyperlink Text Markup Language", "Hyper Tool Modern Language"), 0),
            Question(2, "Which HTTP status code means 'Not Found'?",
                listOf("200", "301", "404", "500"), 2),
            Question(3, "Which of these is a JavaScript framework?",
                listOf("Django", "React", "Laravel", "Flask"), 1)
        ),
        "Testing" to listOf(
            Question(1, "What does 'TDD' stand for?",
                listOf("Test Driven Development", "Type Driven Design", "Total Defect Discovery", "Tested Deployment Design"), 0),
            Question(2, "Which of these is a unit testing framework for Java?",
                listOf("Selenium", "JUnit", "Postman", "Cypress"), 1),
            Question(3, "What is the purpose of mocking?",
                listOf("To slow down tests", "To replace dependencies with controlled fakes", "To compile code", "To deploy apps"), 1)
        ),
        "Machine Learning" to listOf(
            Question(1, "Which is a supervised learning algorithm?",
                listOf("K-Means", "Linear Regression", "PCA", "DBSCAN"), 1),
            Question(2, "What does 'overfitting' mean?",
                listOf("Model performs poorly on training data", "Model memorizes training data and generalizes poorly", "Model is too small", "Model is too fast"), 1),
            Question(3, "What activation function outputs values between 0 and 1?",
                listOf("ReLU", "Sigmoid", "Tanh", "Linear"), 1)
        ),
        "Databases" to listOf(
            Question(1, "What does SQL stand for?",
                listOf("Structured Query Language", "Simple Query Language", "Sequential Query Language", "Standard Query Logic"), 0),
            Question(2, "Which is NOT a NoSQL database?",
                listOf("MongoDB", "Cassandra", "PostgreSQL", "Redis"), 2),
            Question(3, "A primary key must be:",
                listOf("Nullable", "Unique and not null", "A string only", "Auto-incremented"), 1)
        ),
        "Operating Systems" to listOf(
            Question(1, "What is a deadlock?",
                listOf("A fast process", "Two or more processes waiting on each other indefinitely", "A type of memory", "A scheduling algorithm"), 1),
            Question(2, "Which scheduling algorithm gives equal CPU time to all processes?",
                listOf("FCFS", "Round Robin", "SJF", "Priority"), 1),
            Question(3, "What does 'kernel' refer to?",
                listOf("A type of file", "The core part of an OS that manages hardware", "A user app", "A network protocol"), 1)
        ),
        "Networking" to listOf(
            Question(1, "What does TCP stand for?",
                listOf("Transmission Control Protocol", "Transfer Control Protocol", "Type Control Protocol", "Total Connection Protocol"), 0),
            Question(2, "Which port does HTTPS use by default?",
                listOf("80", "21", "443", "22"), 2),
            Question(3, "What is the purpose of DNS?",
                listOf("Encrypt data", "Translate domain names to IP addresses", "Send emails", "Compress files"), 1)
        ),
        "Mobile Development" to listOf(
            Question(1, "Which language is primarily used for modern Android development?",
                listOf("Swift", "Kotlin", "C#", "Ruby"), 1),
            Question(2, "What is Jetpack Compose?",
                listOf("A build tool", "A modern UI toolkit for Android", "A database", "An emulator"), 1),
            Question(3, "What file declares Android app permissions?",
                listOf("build.gradle", "AndroidManifest.xml", "MainActivity.kt", "settings.xml"), 1)
        ),
        "Cloud Computing" to listOf(
            Question(1, "What does IaaS stand for?",
                listOf("Internet as a Service", "Infrastructure as a Service", "Integration as a Service", "Index as a Service"), 1),
            Question(2, "Which is a cloud provider?",
                listOf("Photoshop", "AWS", "Excel", "VS Code"), 1),
            Question(3, "What is 'serverless'?",
                listOf("Code that runs without you managing servers", "An app with no server", "A type of database", "Frontend only"), 0)
        ),
        "Security" to listOf(
            Question(1, "What is phishing?",
                listOf("A type of firewall", "A social engineering attack to steal credentials", "A virus scanner", "A backup method"), 1),
            Question(2, "Which is a strong password practice?",
                listOf("Reuse passwords", "Use a password manager with unique passwords", "Use only your name", "Share with friends"), 1),
            Question(3, "What does HTTPS provide that HTTP doesn't?",
                listOf("Faster loading", "Encryption in transit", "Better SEO", "Lower cost"), 1)
        ),
        "DevOps" to listOf(
            Question(1, "What does CI/CD stand for?",
                listOf("Code Inspection / Code Deployment", "Continuous Integration / Continuous Deployment", "Central Index / Central Database", "Critical Issue / Critical Defect"), 1),
            Question(2, "Which tool is commonly used for containers?",
                listOf("Docker", "Photoshop", "Excel", "Chrome"), 0),
            Question(3, "What does 'infrastructure as code' mean?",
                listOf("Writing code without infrastructure", "Defining infrastructure using code/config files", "Code that runs on infrastructure", "Code reviews for infra teams"), 1)
        ),
        "Game Development" to listOf(
            Question(1, "Which is a popular game engine?",
                listOf("Unity", "Tableau", "Slack", "Notion"), 0),
            Question(2, "What is a 'sprite'?",
                listOf("A 2D image used in games", "A bug in code", "A sound effect", "A physics formula"), 0),
            Question(3, "What does FPS stand for in gaming?",
                listOf("Fast Player Speed", "Frames Per Second", "First Person Shooter only", "Frame Pixel Size"), 1)
        ),
        "AI" to listOf(
            Question(1, "What does LLM stand for?",
                listOf("Large Language Model", "Long Logic Machine", "Linear Learning Method", "Layered Logic Module"), 0),
            Question(2, "Which company created GPT?",
                listOf("Google", "OpenAI", "Meta", "Apple"), 1),
            Question(3, "What is a 'prompt' in the context of LLMs?",
                listOf("A bug report", "The input text given to the model", "A type of GPU", "A loss function"), 1)
        ),
        "UI/UX Design" to listOf(
            Question(1, "What does UX stand for?",
                listOf("User Experience", "Universal Extension", "Unified Examples", "User Examples"), 0),
            Question(2, "Which is a popular UI design tool?",
                listOf("Figma", "MySQL", "Git", "Linux"), 0),
            Question(3, "What is a 'wireframe'?",
                listOf("A network cable", "A low-fidelity sketch of a UI layout", "A type of font", "A CSS framework"), 1)
        )
    )

    // Get a Task object for a specific topic
    fun getTaskForTopic(topic: String): Task {
        // Trim whitespace and do a case-insensitive lookup so small mismatches don't fall through
        val cleanTopic = topic.trim()

        val topicQuestions = questions.entries
            .firstOrNull { it.key.equals(cleanTopic, ignoreCase = true) }
            ?.value
            ?: questions.values.first()    // fallback only if topic genuinely not found

        return Task(
            topic = cleanTopic,
            description = "A short quiz on $cleanTopic to test your knowledge.",
            questions = topicQuestions
        )
    }
}