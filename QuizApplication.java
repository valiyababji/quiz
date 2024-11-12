import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class QuizApplication extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    // Database
    private static final String DB_URL = "jdbc:mysql://localhost:3306/logindb";
    private static final String USER = "root";
    private static final String PASS = "Babji1234";
//hello ra

    JTextField txtId, txtName;
    JLabel lblId, lblName;
    JButton btnStart;

    // Quiz components
    JLabel lblQuestion;
    JRadioButton option1, option2, option3, option4;
    JButton btnNext, btnPrevious, btnSubmit;
    ButtonGroup bg;

    // Quiz data
    String[] questions = {
            "What is the 5 + 6?",
            "what is 5 * 6?",
            "What is 5 - 4?",
            "What is 20 / 4?",
            "What is java ?"
    };
    String[][] options = {
            {"11", "30", "1", "5"},
            {"11", "30", "1", "5"},
            {"11", "30", "1", "5"},
            {"11", "30", "1", "5"},
            {"High level language","object oriented language","medium level language","none of the above"}
    };
    String[] answers = {"11", "30", "1", "5", "object oriented language"};

    // Tracking answers and current question
    String[] userAnswers = new String[5];
    int currentQuestionIndex = 0;
    int score = 0;

    // Constructor medthod edi
    public QuizApplication() {
        setTitle("Quiz Application");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new CardLayout());


        JPanel userInputPanel = new JPanel();
        lblId = new JLabel("ID No:");
        lblName = new JLabel("Name:");
        txtId = new JTextField(15);
        txtName = new JTextField(15);
        btnStart = new JButton("Start Quiz");
        btnStart.addActionListener(this);

        userInputPanel.add(lblId);
        userInputPanel.add(txtId);
        userInputPanel.add(lblName);
        userInputPanel.add(txtName);
        userInputPanel.add(btnStart);

        // Create the quiz panel
        JPanel quizPanel = new JPanel();
        quizPanel.setLayout(new GridLayout(6, 1));
        lblQuestion = new JLabel(questions[currentQuestionIndex]);
        option1 = new JRadioButton();
        option2 = new JRadioButton();
        option3 = new JRadioButton();
        option4 = new JRadioButton();

        bg = new ButtonGroup();
        bg.add(option1);
        bg.add(option2);
        bg.add(option3);
        bg.add(option4);

        btnNext = new JButton("Next");
        btnPrevious = new JButton("Previous");
        btnSubmit = new JButton("Submit");

        btnNext.addActionListener(this);
        btnPrevious.addActionListener(this);
        btnSubmit.addActionListener(this);

        quizPanel.add(lblQuestion);
        quizPanel.add(option1);
        quizPanel.add(option2);
        quizPanel.add(option3);
        quizPanel.add(option4);

        JPanel navPanel = new JPanel();
        navPanel.add(btnPrevious);
        navPanel.add(btnNext);
        navPanel.add(btnSubmit);
        btnSubmit.setVisible(false); //hide cheskuna

        quizPanel.add(navPanel);

        // panels ni add cheskuna
        add(userInputPanel, "userInput");
        add(quizPanel, "quiz");

        //  visibility at starting
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "userInput");

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnStart) {
            // Switch to quiz panel and show the first question
            ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "quiz");
            showQuestion();
        } else if (e.getSource() == btnNext) {
            // Save current answer and go to the next question
            saveAnswer();
            if (currentQuestionIndex < questions.length - 1) {
                currentQuestionIndex++;
                showQuestion();
            }
        } else if (e.getSource() == btnPrevious) {
            // Save current answer and go to the previous question
            saveAnswer();
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                showQuestion();
            }
        } else if (e.getSource() == btnSubmit) {
            // Save the final answer and submit the quiz
            saveAnswer();
            calculateScore();
            saveResults();
            JOptionPane.showMessageDialog(this, "Quiz Submitted! Your score is: " + score);
        }
    }

    // Display the current question and options
    private void showQuestion() {
        lblQuestion.setText(questions[currentQuestionIndex]);
        option1.setText(options[currentQuestionIndex][0]);
        option2.setText(options[currentQuestionIndex][1]);
        option3.setText(options[currentQuestionIndex][2]);
        option4.setText(options[currentQuestionIndex][3]);
        bg.clearSelection();

        // Enable/disable navigation buttons
        btnPrevious.setEnabled(currentQuestionIndex > 0);
        btnNext.setVisible(currentQuestionIndex < questions.length - 1);
        btnSubmit.setVisible(currentQuestionIndex == questions.length - 1);
    }

    // Save the selected answer for the current question
    private void saveAnswer() {
        if (option1.isSelected()) {
            userAnswers[currentQuestionIndex] = option1.getText();
        } else if (option2.isSelected()) {
            userAnswers[currentQuestionIndex] = option2.getText();
        } else if (option3.isSelected()) {
            userAnswers[currentQuestionIndex] = option3.getText();
        } else if (option4.isSelected()) {
            userAnswers[currentQuestionIndex] = option4.getText();
        }
    }

    // Calculate the score based on correct answers
    private void calculateScore() {
        score = 0;
        for (int i = 0; i < questions.length; i++) {
            if (answers[i].equals(userAnswers[i])) {
                score++;
            }
        }
    }

    // Save quiz results to the database and a file
    private void saveResults() {
        String idStr = txtId.getText();
        String name = txtName.getText();

        // Parse ID as a long
        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid ID number. Please enter a numeric value.");
            return;
        }

        // Save to file as quiz_results.txt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("quiz_results.txt", true))) {
            writer.write("ID: " + id + ", Name: " + name + ", Score: " + score + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save to database
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO quiz_results (id, name, score) VALUES (?, ?, ?)")) {
            pstmt.setLong(1, id); // Use setLong for long data type
            pstmt.setString(2, name);
            pstmt.setInt(3, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Main method to start the application
    public static void main(String[] args) {
        new QuizApplication();
    }
}
