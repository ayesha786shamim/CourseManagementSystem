import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class printController {

    @FXML
    private TableView<Course> courseTable;
    @FXML
    private TableColumn<Course, String> courseIdColumn;
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    @FXML
    private TableColumn<Course, Integer> creditsColumn;
    @FXML
    private TableColumn<Course, String> semesterColumn;
    @FXML
    private TableColumn<Course, Double> gpaColumn;

    @FXML
    private Button printButton;
    @FXML
    private Label messageLabel;
    @FXML
    private TextField cgpaTextField;

    private ObservableList<Course> courseList;
    private int loggedInUserId;


    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        loadDataFromDatabase(loggedInUserId); // Load data when the user ID is set
        calculateAndSetCGPA();
    }

    @FXML
    public void initialize() {
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        gpaColumn.setCellValueFactory(new PropertyValueFactory<>("gpa"));


//        calculateAndSetCGPA();

    }



    private void loadDataFromDatabase(int loggedInUserId) {
        System.out.println("Attempting to load data from the database...");
        courseList = FXCollections.observableArrayList();
        String sql = "SELECT COURSE.CourseId, COURSE.Course_name, COURSE.Credits, COURSE.Semester, MARKS.Marks " +
                "FROM COURSE " +
                "JOIN MARKS ON COURSE.CourseId = MARKS.CourseId " +
                "WHERE MARKS.UserId = ?";

        // No need to call setLoggedInUserId() here since loggedInUserId is already set
        System.out.println("Logged-in User ID: " + loggedInUserId); // Output the logged-in user ID

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, loggedInUserId); // Set the user ID parameter in the SQL query
            System.out.println("Connected to database");

            ResultSet rs = pstmt.executeQuery();
            System.out.println("SQL query executed");

            boolean dataFound = false; // Flag to track if any data is found

            while (rs.next()) {
                dataFound = true; // Data is found
                int courseId = rs.getInt("CourseId");
                String courseName = rs.getString("Course_name");
                int credits = rs.getInt("Credits");
                String semester = rs.getString("Semester");
                int marks = rs.getInt("Marks");
                double gpa = getGpa(marks); // Calculate GPA based on marks obtained
                Course course = new Course(courseId, courseName, credits, semester, gpa);
                courseList.add(course);
                System.out.println("Added course to courseList: " + course);
            }

            if (!dataFound) {
                System.out.println("No data found for the logged-in user in the database.");
            } else {
                System.out.println("Data loaded successfully.");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for SQL exceptions
        }

        courseTable.setItems(courseList);
    }

    private void calculateAndSetCGPA() {
        int totalCredits = courseList.stream().mapToInt(Course::getCredits).sum();
        double totalGPA = courseList.stream().mapToDouble(course -> course.getGpa() * course.getCredits()).sum();

        double cgpa = totalCredits > 0 ? totalGPA / totalCredits : 0.0;
        cgpaTextField.setText(String.format("%.2f", cgpa));
    }


    public double getGpa(int marks) {
        // Calculate GPA based on the marks obtained
        double gpa = 0.0;
        if (marks >= 90) {
            gpa = 4.0; // Grade A
        } else if (marks >= 80) {
            gpa = 3.0; // Grade B
        } else if (marks >= 70) {
            gpa = 2.0; // Grade C
        } else if (marks >= 60) {
            gpa = 1.0; // Grade D
        } else {
            gpa = 0.0; // Grade F (Fail)
        }
        return gpa;
    }


    @FXML
    public void handlePrint() {
        int totalCredits = courseList.stream().mapToInt(Course::getCredits).sum();

        if (totalCredits < 140) {
            messageLabel.setText("You cannot download the transcript. Total credit hours are less than 140.");
            return;
        }

        // Open a file chooser to save the PDF file
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try {
                generatePDF(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void generatePDF(File file) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();

                // Load the font from file
                PDType0Font font = PDType0Font.load(doc, new File("C:\\Users\\HP\\Downloads\\Helvetica-Font\\Helvetica.ttf")); // Adjust the file path as necessary

                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Transcript");
                contentStream.endText();

                int y = 650;
                int x = 50;

                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(x, y);
                contentStream.showText("Course ID");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("Course Name");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText("Credits");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("Semester");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("GPA");
                contentStream.endText();

                y -= 20;

                for (Course course : courseList) {
                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(x, y);
                    contentStream.showText(String.valueOf(course.getCourseId())); // Corrected
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(course.getCourseName());
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(String.valueOf(course.getCredits())); // Corrected
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(course.getSemester());
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(String.valueOf(course.getGpa())); // Corrected
                    contentStream.endText();
                    y -= 20;
                }

                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(x, y - 30);
                contentStream.showText("CGPA: " + cgpaTextField.getText());
                contentStream.endText();
            }

            doc.save(file);
        }
    }

    public static class Course {
        private int courseId;
        private String courseName;
        private int credits;
        private String semester;
        private double gpa; // Adding gpa as an instance variable

        public Course(int courseId, String courseName, int credits, String semester, double gpa) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.credits = credits;
            this.semester = semester;
            this.gpa = gpa; // Setting the gpa value
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public int getCredits() {
            return credits;
        }

        public String getSemester() {
            return semester;
        }

        public double getGpa() {
            // No need to calculate GPA here, just return the already calculated GPA
            return gpa;
        }
    }

}

