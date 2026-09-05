import java.sql.*;
public class CheckJdbc {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/tadka?user=tadka&password=tadka_local&ssl=false";
        try (Connection c = DriverManager.getConnection(url)) {
            DatabaseMetaData meta = c.getMetaData();
            System.out.println("Product name: " + meta.getDatabaseProductName());
            System.out.println("Product version: " + meta.getDatabaseProductVersion());
            System.out.println("Driver name: " + meta.getDriverName());
            System.out.println("Driver version: " + meta.getDriverVersion());
        }
    }
}
