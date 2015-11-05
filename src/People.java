import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;


import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zach on 10/19/15.
 */
public class People {

    static final int SHOW_COUNT = 20;

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS people");
        stmt.execute("CREATE TABLE people (id IDENTITY, firstName VARCHAR, lastName VARCHAR, email VARCHAR, country VARCHAR, ip VARCHAR)");
    }

    public static void insertPerson(Connection conn, String firstName, String lastName, String email, String country, String ip) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO people VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setString(3, email);
        stmt.setString(4, country);
        stmt.setString(5, ip);
        stmt.execute();
    }

    public static ArrayList<Person> selectPerson(Connection conn, int id) throws SQLException {
        ArrayList<Person> people = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            Person person = new Person();
            person.id = results.getInt("id");
            person.firstName = results.getString("firstName");
            person.lastName = results.getString("lastName");
            person.email = results.getString("email");
            person.country = results.getString("country");
            person.ip = results.getString("ip");
            people.add(person);
        }
        return people;
    }

    public static ArrayList<Person> selectPeople(Connection conn, int offset) throws SQLException {
        ArrayList<Person> people = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people LIMIT ? OFFSET ?");
        stmt.setInt(1, SHOW_COUNT);
        stmt.setInt(2, offset);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            Person person = new Person();
            person.id = results.getInt("id");
            person.firstName = results.getString("firstName");
            person.lastName = results.getString("lastName");
            person.email = results.getString("email");
            person.country = results.getString("country");
            person.ip = results.getString("ip");
            people.add(person);
        }
        return people;
    }

    public static void populateDatabase(Connection conn) throws SQLException {
        createTables(conn);
        String fileContent = readFile("people.csv");
        String[] lines = fileContent.split("\n");


        for (String line : lines) {
            if (line == lines[0]) { //skip header row
                continue;
            }

            String[] columns = line.split(",");
            String firstName = columns[1];
            String lastName = columns[2];
            String email = columns[3];
            String country = columns[4];
            String ip = columns[5];
            insertPerson(conn, firstName, lastName, email, country, ip);
//            Person person = new Person();
//            people.add(person);
        }
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

//        ArrayList<Person> people = new ArrayList();

//        addTestPeople(people);

        populateDatabase(conn);


        Spark.get(
                "/",
                ((request, response) -> {
                    String offsetStr = request.queryParams("offset"); //getting a query parameter equal to 20, first time we go to the site it will be null
                    int offset = 0;
                    try {
                        offset = Integer.valueOf(offsetStr);
                    } catch (Exception e) {

                    }

//                    String idStr = request.queryParams("id");
//                    int id = Integer.valueOf(idStr);

//                    int namesStart;
//
//                    if (offset == null) {
//                        namesStart = 0; //if it's null, like we know it will be from above, give it a default offset number of 0
//                    } else {
//                        namesStart = Integer.valueOf(offset);
//                    }

//                    if(!(namesStart<people.size())) {
//                        Spark.halt(403);
//                    }
//                    ArrayList<Person> peeps = new ArrayList<Person>(people.subList(namesStart, namesStart + SHOW_COUNT));

//                    ArrayList<Person> peeps = new ArrayList<Person>(people.subList(
//                            Math.max(0, Math.min(people.size(), namesStart)),
//                            Math.max(0, Math.min(people.size(), namesStart + SHOW_COUNT))
//                    ));

                    //^this will eliminate an error if "http://localhost:4567/?offset=990" this is entered into the URL or if we go over people.size();
                    //this replaces the:
                    // ArrayList<Person> peeps = new ArrayList<Person>(people.subList(namesStart, namesStart + SHOW_COUNT));
                    // if(!(namesStart<people.size())) {Spark.halt(403);} above.
                    //this will say first, make sure it's not too big and then make sure it's not too small.

                    HashMap m = new HashMap();
                    m.put("people", selectPeople(conn, offset));
                    m.put("oldOffset", offset - SHOW_COUNT); //"oldOffset" calls {{oldOffset}} from people.html
                    m.put("newOffset", offset + SHOW_COUNT); //"newOffset" calls {{newOffset}} from people.html

                    boolean showNext = selectPeople(conn, offset).size() == SHOW_COUNT; //shows one extra Next on last page
                    m.put("showNext", showNext); //will stop showing the "next" button when there will be no more names to display on another page

                    boolean showPrev = offset > 0 ;
                    m.put("showPrev", showPrev);

                    return new ModelAndView(m, "people.html");
                }),
                new MustacheTemplateEngine()
        );

        Spark.get(
                "/person",
                ((request, response) -> {
                    HashMap m = new HashMap();

                    String id = request.queryParams("id");
                    int idNum = Integer.valueOf(id);
                    Person person = selectPerson(conn, idNum).get(0); //0 is getting the first index of the arraylist
                    m.put("person", person);

                    return new ModelAndView(m, "person.html");
                }),
                new MustacheTemplateEngine()
        );
    }

    static String readFile(String fileName) {
        File f = new File(fileName);
        try {
            FileReader fr = new FileReader(f);
            int fileSize = (int) f.length();
            char[] fileContent = new char[fileSize];
            fr.read(fileContent);
            return new String(fileContent);
        } catch (Exception e) {
            return null;
        }
    }

    static void addTestPeople(ArrayList<Person> people) {
        people.add(new Person("First", "Last", "fl@gmail.com", "Merica", "87654321"));
    }
}
