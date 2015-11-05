import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by jessicahuffstutler on 11/4/15.
 */
public class PeopleTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./test");
        People.createTables(conn);
        return conn;
    }

    public void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn. createStatement();
        stmt.execute("DROP TABLE people");
        conn.close();
    }

    @Test
    public void testPerson() throws SQLException {
        Connection conn = startConnection();
        People.insertPerson(conn, "First", "Last", "fl@gmail.com", "Murrica", "87654321");
        ArrayList<Person> people = People.selectPerson(conn, 1);
        endConnection(conn);

        assertTrue(people.size() == 1);
    }

    @Test
    public void testPeople() throws SQLException {
        Connection conn = startConnection();
        People.insertPerson(conn, "First", "Last", "fl@gmail.com", "Murrica", "87654321");
        People.insertPerson(conn, "HI", "BYE", "fl@gmail.com", "CountryB", "87654321");
        People.insertPerson(conn, "First", "Last", "fl@gmail.com", "CountryC", "87654321");
        ArrayList<Person> people = People.selectPeople(conn, 0);
        endConnection(conn);

        assertTrue(people.size() == 3);
    }
}