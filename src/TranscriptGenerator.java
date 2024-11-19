import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.Desktop;

public class TranscriptGenerator {

    private static final String TRANSCRIPT_FILE_PATH = "C:\\Users\\HP\\OneDrive\\Desktop\\transcript.html";

    public static void generateTranscriptHTML(int userId) throws IOException, SQLException {
        System.out.println("Generating transcript for user ID: " + userId); // Debug statement

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        BufferedWriter writer = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement("SELECT u.First_name, u.Last_name, c.Course_name, c.Credits, m.Marks, c.Semester " +
                    "FROM [USER] u " +
                    "JOIN STUDENT_COURSE sc ON u.userId = sc.UserId " +
                    "JOIN COURSE c ON sc.CourseId = c.CourseId " +
                    "LEFT JOIN MARKS m ON u.userId = m.UserId AND c.CourseId = m.CourseId " +
                    "WHERE u.userId = ?");
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            File transcriptFile = new File(TRANSCRIPT_FILE_PATH);
            writer = new BufferedWriter(new FileWriter(transcriptFile));

            writer.write("<html><head><title>Student Transcript</title>");
            writer.write("<style>");
            writer.write("table {border-collapse: collapse; width: 100%;}");
            writer.write("th, td {border: 1px solid #dddddd; text-align: left; padding: 8px;}");
            writer.write("th {background-color: #f2f2f2;}");
            writer.write("</style>");
            writer.write("</head><body>");
            writer.write("<h1 style='text-align:center;'>Student Transcript</h1>");

            if (rs.next()) {
                String firstName = rs.getString("First_name");
                String lastName = rs.getString("Last_name");
                String name = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                writer.write("<p>Name: " + name + "</p>");

                writer.write("<table><tr><th>Course Name</th><th>Credits</th><th>Grade</th><th>Semester</th></tr>");

                do {
                    String courseName = rs.getString("Course_name");
                    int credits = rs.getInt("Credits");
                    int marks = rs.getInt("Marks");
                    String semester = rs.getString("Semester");
                    writer.write("<tr><td>" + courseName + "</td><td>" + credits + "</td><td>" + marks + "</td><td>" + semester + "</td></tr>");
                } while (rs.next());

                writer.write("</table>");
            } else {
                writer.write("<p>No transcript data found for the given user ID.</p>");
            }

            writer.write("</body></html>");
            writer.flush();

            System.out.println("Transcript HTML generated and saved to desktop.");

            if (transcriptFile.exists()) {
                try {
                    Desktop.getDesktop().browse(transcriptFile.toURI());
                    System.out.println("Transcript HTML opened in default browser.");
                } catch (IOException e) {
                    System.err.println("Error opening HTML file: " + e.getMessage());
                }
            } else {
                System.err.println("Transcript HTML file was not created.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
