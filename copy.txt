import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class another extends JFrame implements ActionListener {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new another());
    }

    private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/logindb";
    private static final String USER = "root";
    private static final String PASS = "Babji1234";

    private JTextField txtId, txtName;
    private JLabel lblId, lblName, lblTimer, lblQuestion;
    private JButton btnStart, btnAdmin, btnAdminLogin, btnNext, btnPrevious, btnSubmit, btnSaveQuestion;
    private JTextField txtAdminUsername, txtQuestion, txtOption1, txtOption2, txtOption3, txtOption4, txtAnswer;
    private JPasswordField txtAdminPassword;
    private JRadioButton option1, option2, option3, option4;
    private ButtonGroup bg;
    private Timer quizTimer;

    private String[] questions;
    private String[][] options;
    private String[] answers;
    private String[] userAnswers;
    private int totalQuestions = 5;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int remainingTime = 5 * 60;

    public another() {
        setTitle("Interactive Quiz Application");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new CardLayout());
        setResizable(false);

        // User Input Panel
        JPanel userInputPanel = new JPanel(new GridBagLayout());
        userInputPanel.setBackground(new Color(240, 248, 255));
        userInputPanel.setBorder(BorderFactory.createTitledBorder("User Login"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        lblId = new JLabel("ID No:");
        lblName = new JLabel("Name:");
        lblId.setFont(new Font("Arial", Font.BOLD, 14));
        lblName.setFont(new Font("Arial", Font.BOLD, 14));

        txtId = new JTextField(15);
        txtName = new JTextField(15);
        btnStart = new JButton("Start Quiz");
        btnStart.setBackground(new Color(50, 150, 250));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFont(new Font("Arial", Font.BOLD, 14));
        btnStart.setFocusPainted(false);
        btnStart.addActionListener(this);

        btnAdmin = new JButton("Admin");
        btnAdmin.setBackground(new Color(50, 150, 250));
        btnAdmin.setForeground(Color.WHITE);
        btnAdmin.setFont(new Font("Arial", Font.BOLD, 14));
        btnAdmin.setFocusPainted(false);
        btnAdmin.addActionListener(this);

        gbc.gridx = 0;
        gbc.gridy = 0;
        userInputPanel.add(lblId, gbc);
        gbc.gridx = 1;
        userInputPanel.add(txtId, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        userInputPanel.add(lblName, gbc);
        gbc.gridx = 1;
        userInputPanel.add(txtName, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        userInputPanel.add(btnStart, gbc);
        gbc.gridx = 1;
        userInputPanel.add(btnAdmin, gbc);

        // Admin Login Panel
        JPanel adminLoginPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        adminLoginPanel.setBorder(BorderFactory.createTitledBorder("Admin Login"));
        adminLoginPanel.setBackground(new Color(255, 248, 220));
        adminLoginPanel.add(new JLabel("Username:"));
        txtAdminUsername = new JTextField(15);
        adminLoginPanel.add(txtAdminUsername);
        adminLoginPanel.add(new JLabel("Password:"));
        txtAdminPassword = new JPasswordField(15);
        adminLoginPanel.add(txtAdminPassword);

        btnAdminLogin = new JButton("Login");
        btnAdminLogin.setBackground(new Color(50, 150, 250));
        btnAdminLogin.setForeground(Color.WHITE);
        btnAdminLogin.addActionListener(this);
        adminLoginPanel.add(btnAdminLogin);

        // Quiz Panel
        JPanel quizPanel = new JPanel(new GridLayout(8, 1, 5, 5));
        quizPanel.setBorder(BorderFactory.createTitledBorder("Quiz"));
        quizPanel.setBackground(new Color(224, 255, 255));

        lblQuestion = new JLabel();
        lblTimer = new JLabel("Time remaining: 5:00", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Arial", Font.BOLD, 16));

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

        btnNext.setBackground(new Color(50, 150, 250));
        btnPrevious.setBackground(new Color(50, 150, 250));
        btnSubmit.setBackground(new Color(50, 150, 250));
        btnNext.setForeground(Color.WHITE);
        btnPrevious.setForeground(Color.WHITE);
        btnSubmit.setForeground(Color.WHITE);
        btnNext.setFont(new Font("Arial", Font.BOLD, 14));
        btnPrevious.setFont(new Font("Arial", Font.BOLD, 14));
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 14));

        btnNext.addActionListener(this);
        btnPrevious.addActionListener(this);
        btnSubmit.addActionListener(this);

        quizPanel.add(lblTimer);
        quizPanel.add(lblQuestion);
        quizPanel.add(option1);
        quizPanel.add(option2);
        quizPanel.add(option3);
        quizPanel.add(option4);

        JPanel navPanel = new JPanel();
        navPanel.add(btnPrevious);
        navPanel.add(btnNext);
        navPanel.add(btnSubmit);
        btnSubmit.setVisible(false);

        quizPanel.add(navPanel);

        // Admin Question Save Panel
        JPanel adminPanel = new JPanel(new GridLayout(10, 1, 5, 5));
        adminPanel.setBorder(BorderFactory.createTitledBorder("Add Questions"));
        adminPanel.setBackground(new Color(255, 228, 225));

        txtQuestion = new JTextField(30);
        txtOption1 = new JTextField(20);
        txtOption2 = new JTextField(20);
        txtOption3 = new JTextField(20);
        txtOption4 = new JTextField(20);
        txtAnswer = new JTextField(20);
        btnSaveQuestion = new JButton("Save Question");
        btnSaveQuestion.setBackground(new Color(50, 150, 250));
        btnSaveQuestion.setForeground(Color.WHITE);
        btnSaveQuestion.setFont(new Font("Arial", Font.BOLD, 14));
        btnSaveQuestion.addActionListener(this);

        adminPanel.add(new JLabel("Question:"));
        adminPanel.add(txtQuestion);
        adminPanel.add(new JLabel("Option 1:"));
        adminPanel.add(txtOption1);
        adminPanel.add(new JLabel("Option 2:"));
        adminPanel.add(txtOption2);
        adminPanel.add(new JLabel("Option 3:"));
        adminPanel.add(txtOption3);
        adminPanel.add(new JLabel("Option 4:"));
        adminPanel.add(txtOption4);
        adminPanel.add(new JLabel("Answer:"));
        adminPanel.add(txtAnswer);
        adminPanel.add(btnSaveQuestion);

        add(userInputPanel, "userInput");
        add(quizPanel, "quiz");
        add(adminLoginPanel, "adminLogin");
        add(adminPanel, "admin");

        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "userInput");
        setVisible(true);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnStart) {
            fetchQuestionsFromDatabase();
            ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "quiz");
            startTimer();
            showQuestion();
        } else if (e.getSource() == btnAdmin) {
            ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "adminLogin");
        } else if (e.getSource() == btnAdminLogin) {
            if (authenticateAdmin(txtAdminUsername.getText(), String.valueOf(txtAdminPassword.getPassword()))) {
                ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "admin");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid admin credentials. Please try again.");
            }
        } else if (e.getSource() == btnSaveQuestion) {
            saveQuestionToDatabase();
            JOptionPane.showMessageDialog(this, "Question Saved!");
        } else if (e.getSource() == btnNext) {
            saveAnswer();
            if (currentQuestionIndex < totalQuestions - 1) {
                currentQuestionIndex++;
                showQuestion();
            }
        } else if (e.getSource() == btnPrevious) {
            saveAnswer();
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                showQuestion();
            }
        } else if (e.getSource() == btnSubmit) {
            saveAnswer();
            submitQuiz();
        }
    }

    private boolean authenticateAdmin(String username, String password) {
        return "admin".equals(username) && "babji".equals(password);
    }



private void startTimer() {
        quizTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingTime > 0) {
                    remainingTime--;
                    int minutes = remainingTime / 60;
                    int seconds = remainingTime % 60;
                    lblTimer.setText(String.format("Time remaining: %02d:%02d", minutes, seconds));

                    if (remainingTime > 3 * 60) { // More than 3 minutes left
                        getContentPane().setBackground(Color.GREEN);
                    } else if (remainingTime > 2 * 60) { // Between 2 and 3 minutes left
                        getContentPane().setBackground(Color.YELLOW);
                    } else { // Less than 1 minute left
                        getContentPane().setBackground(Color.RED);
                    }
                } else {
                    quizTimer.stop();
                    submitQuiz();
                }
            }
        });
        quizTimer.start();
    }


    private void submitQuiz() {
        saveAnswer();
        calculateScore();
        saveResults();
        JOptionPane.showMessageDialog(this, "Time's up! Quiz Submitted! Your score is: " + score);
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "userInput");
    }

    private void fetchQuestionsFromDatabase() {
        List<QuestionData> questionDataList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT question, option1, option2, option3, option4, answer FROM quiz_questions")) {

            while (rs.next()) {
                String question = rs.getString("question");
                String[] options = {
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4")
                };
                String answer = rs.getString("answer");
                questionDataList.add(new QuestionData(question, options, answer));
            }

            Collections.shuffle(questionDataList);
            totalQuestions = Math.min(questionDataList.size(), 5);
            questions = new String[totalQuestions];
            options = new String[totalQuestions][4];
            answers = new String[totalQuestions];
            userAnswers = new String[totalQuestions];

            for (int i = 0; i < totalQuestions; i++) {
                QuestionData data = questionDataList.get(i);
                questions[i] = data.question;
                options[i] = data.options;
                answers[i] = data.answer;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculateScore() {
        score = 0;
        for (int i = 0; i < totalQuestions; i++) {
            if (answers[i].equals(userAnswers[i])) {
                score++;
            }
        }
    }

    private void showQuestion() {
        lblQuestion.setText(questions[currentQuestionIndex]);
        option1.setText(options[currentQuestionIndex][0]);
        option2.setText(options[currentQuestionIndex][1]);
        option3.setText(options[currentQuestionIndex][2]);
        option4.setText(options[currentQuestionIndex][3]);
        bg.clearSelection();

        btnPrevious.setEnabled(currentQuestionIndex > 0);
        btnNext.setVisible(currentQuestionIndex < totalQuestions - 1);
        btnSubmit.setVisible(currentQuestionIndex == totalQuestions - 1);
    }

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

    private void saveResults() {
        String idStr = txtId.getText();
        String name = txtName.getText();

        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid ID number. Please enter a numeric value.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("quiz_results.txt", true))) {
            writer.write("ID: " + id + ", Name: " + name + ", Score: " + score + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO quiz_results (id, name, score) VALUES (?, ?, ?)")) {
            pstmt.setLong(1, id);
            pstmt.setString(2, name);
            pstmt.setInt(3, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveQuestionToDatabase() {
        String question = txtQuestion.getText();
        String option1 = txtOption1.getText();
        String option2 = txtOption2.getText();
        String option3 = txtOption3.getText();
        String option4 = txtOption4.getText();
        String answer = txtAnswer.getText();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO quiz_questions (question, option1, option2, option3, option4, answer) VALUES (?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, question);
            pstmt.setString(2, option1);
            pstmt.setString(3, option2);
            pstmt.setString(4, option3);
            pstmt.setString(5, option4);
            pstmt.setString(6, answer);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class QuestionData {
    String question;
    String[] options;
    String answer;

    QuestionData(String question, String[] options, String answer) {
        this.question = question;
        this.options = options;
        this.answer = answer;
    }
}
