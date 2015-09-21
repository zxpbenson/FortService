import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLITEConnection {

    private static volatile Connection conn;
    
    public static Connection getConnection(boolean fortEnv){
        if(conn == null){
            synchronized(SQLITEConnection.class){
                if(conn == null){
                    buildConnection(fortEnv);
                }
            }
        }
        return conn;
    }
    
    private static void buildConnection(boolean fortEnv){
        String dbFilePath = "D://FortService//resources//fort.append.s3db";
        if(fortEnv){
            dbFilePath = "/usr/local/fort_append/db/fort.append.s3db";
        }
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+dbFilePath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }        
    }
    
    public static void closeConnection(){
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            conn = null;
        }
    }
    
    public static void main(String[] args) {
        
    }

}
