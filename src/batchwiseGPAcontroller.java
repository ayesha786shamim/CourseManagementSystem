import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class batchwiseGPAcontroller implements Initializable {

    @FXML
    private ChoiceBox<String> choiceBoxBatch;
    @FXML
    private ChoiceBox<String> choiceBoxGPA;

    @FXML
    private TableView<StudentInfo> tableViewCredithour;

    @FXML
    private TableColumn<StudentInfo, Integer> columnUserId;

    @FXML
    private TableColumn<StudentInfo, String> columnStudentName;

    @FXML
    private TableColumn<StudentInfo, Double> columnGPA;

    @FXML
    private TableColumn<StudentInfo, Double> columnCGPA;

    private ObservableList<StudentInfo> studentInfoList = FXCollections.observableArrayList();
    private int loggedInUserId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize choice box items for batches
        choiceBoxBatch.setItems(FXCollections.observableArrayList("2020", "2021", "2022", "2023", "2024"));

        // Initialize choice box items for GPA filter
        choiceBoxGPA.setItems(FXCollections.observableArrayList("4", "<4", "<3", "<2", "<1"));

        // Initialize table columns
        columnUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        columnStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        columnGPA.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        columnCGPA.setCellValueFactory(new PropertyValueFactory<>("cgpa"));
    }

    @FXML
    private void onDisplayButtonClicked() {
        String batch = choiceBoxBatch.getValue();
        if (batch != null) {
            System.out.println("Selected batch: " + batch);
            fetchStudentInfoList(batch);
            tableViewCredithour.setItems(studentInfoList);
        } else {
            System.out.println("No batch selected");
        }
    }

    private void fetchStudentInfoList(String batch) {
        studentInfoList.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Fetch the department ID of the logged-in coordinator
            String getDepartmentQuery = "SELECT departmentId FROM [USER] WHERE userId = ?";
            PreparedStatement getDepartmentStmt = conn.prepareStatement(getDepartmentQuery);
            getDepartmentStmt.setInt(1, loggedInUserId);

            ResultSet departmentRs = getDepartmentStmt.executeQuery();
            int departmentId = -1;
            if (departmentRs.next()) {
                departmentId = departmentRs.getInt("departmentId");
            }

            if (departmentId != -1) {
                // Fetch students' information from the same department and batch
                String query = "SELECT u.userId, CONCAT(u.First_name, ' ', u.Last_name) AS studentName "
                        + "FROM [USER] u "
                        + "JOIN BATCH b ON u.userId = b.userId "
                        + "WHERE u.departmentId = ? AND b.Batch = ?";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setInt(1, departmentId);
                statement.setString(2, batch);

                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    int userId = rs.getInt("userId");
                    String studentName = rs.getString("studentName");
                    double gpa = calculateGPA(conn, userId);
                    double cgpa = calculateCGPA(conn, userId);

                    studentInfoList.add(new StudentInfo(userId, studentName, gpa, cgpa));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double convertToScale(int marks) {
        if (marks >= 85) return 4.0;
        else if (marks >= 75) return 3.5;
        else if (marks >= 65) return 3.0;
        else if (marks >= 55) return 2.5;
        else if (marks >= 45) return 2.0;
        else if (marks >= 35) return 1.5;
        else if (marks >= 25) return 1.0;
        else return 0.0;
    }

    private double calculateGPA(Connection conn, int userId) {
        double gpa = 0.0;
        try {
            // Find the latest semester for the user in the MARKS table
            String latestSemesterQuery = "SELECT MAX(CAST(Semester AS INT)) AS latestSemester "
                    + "FROM MARKS "
                    + "WHERE UserId = ?";
            PreparedStatement latestSemesterStmt = conn.prepareStatement(latestSemesterQuery);
            latestSemesterStmt.setInt(1, userId);
            ResultSet latestSemesterRs = latestSemesterStmt.executeQuery();
            int latestSemester = -1;
            if (latestSemesterRs.next()) {
                latestSemester = latestSemesterRs.getInt("latestSemester");
            }

            if (latestSemester != -1) {
                // Fetch the marks and credits for the latest semester
                String marksQuery = "SELECT m.Marks, c.Credits "
                        + "FROM MARKS m "
                        + "JOIN COURSE c ON m.CourseId = c.CourseId "
                        + "WHERE m.UserId = ? AND m.Semester = ?";
                PreparedStatement marksStmt = conn.prepareStatement(marksQuery);
                marksStmt.setInt(1, userId);
                marksStmt.setInt(2, latestSemester);
                ResultSet marksRs = marksStmt.executeQuery();

                double totalWeightedMarks = 0.0;
                double totalCredits = 0.0;

                while (marksRs.next()) {
                    int marks = marksRs.getInt("Marks");
                    double credits = marksRs.getDouble("Credits");

                    totalWeightedMarks += convertToScale(marks) * credits;
                    totalCredits += credits;
                }

                // Calculate GPA
                if (totalCredits != 0.0) {
                    gpa = totalWeightedMarks / totalCredits;
                    gpa = Math.min(Math.max(gpa, 0.0), 4.0); // Ensure GPA is within 0 to 4 range
                    gpa = Double.parseDouble(String.format("%.2f", gpa));
                    System.out.println("Calculated GPA for userId " + userId + " in semester " + latestSemester + ": " + gpa);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gpa;
    }



    private double calculateCGPA(Connection conn, int userId) {
        double cgpa = 0.0;
        try {
            String cgpaQuery = "SELECT Marks, Credits " +
                    "FROM MARKS m " +
                    "JOIN COURSE c ON m.CourseId = c.CourseId " +
                    "WHERE m.userId = ?";

            PreparedStatement cgpaStmt = conn.prepareStatement(cgpaQuery);
            cgpaStmt.setInt(1, userId);

            ResultSet cgpaRs = cgpaStmt.executeQuery();
            if (cgpaRs.next()) {
                double sumCredits = 0.0;
                double totalWeightedMarks = 0.0;

                // Calculate total weighted marks and sum of credits for all semesters
                do {
                    int marks = cgpaRs.getInt("Marks");
                    double credits = cgpaRs.getDouble("Credits");

                    totalWeightedMarks += convertToScale(marks) * credits;
                    sumCredits += credits;
                } while (cgpaRs.next());

                cgpa = totalWeightedMarks / sumCredits;
                cgpa = Double.parseDouble(String.format("%.2f", cgpa));
                System.out.println("Calculated CGPA for userId " + userId + ": " + cgpa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cgpa;
    }



    @FXML
    private void onFilterButtonClicked() {
        String selectedGPA = choiceBoxGPA.getValue();
        if (selectedGPA != null) {
            System.out.println("Selected GPA filter: " + selectedGPA);
            filterStudentInfoList(selectedGPA);
        } else {
            System.out.println("No GPA filter selected");
        }
    }

    private void filterStudentInfoList(String gpaFilter) {
        ObservableList<StudentInfo> filteredList = FXCollections.observableArrayList();

        for (StudentInfo studentInfo : studentInfoList) {
            double gpa = studentInfo.getGpa();
            if (gpaFilter.equals("4") && gpa == 4.0) {
                filteredList.add(studentInfo);
            } else if (gpaFilter.equals("<4") && gpa < 4.0) {
                filteredList.add(studentInfo);
            } else if (gpaFilter.equals("<3") && gpa < 3.0) {
                filteredList.add(studentInfo);
            } else if (gpaFilter.equals("<2") && gpa < 2.0) {
                filteredList.add(studentInfo);
            } else if (gpaFilter.equals("<1") && gpa < 1.0) {
                filteredList.add(studentInfo);
            }
        }

        tableViewCredithour.setItems(filteredList);
    }

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    public static class StudentInfo {
        private final int userId;
        private final String studentName;
        private final double gpa;
        private final double cgpa;

        public StudentInfo(int userId, String studentName, double gpa, double cgpa) {
            this.userId = userId;
            this.studentName = studentName;
            this.gpa = gpa;
            this.cgpa = cgpa;
        }

        public int getUserId() {
            return userId;
        }

        public String getStudentName() {
            return studentName;
        }

        public double getGpa() {
            return gpa;
        }

        public double getCgpa() {
            return cgpa;
        }
    }
}
