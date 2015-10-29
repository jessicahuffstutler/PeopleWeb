import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zach on 10/19/15.
 */
public class People {

    static final int SHOW_COUNT = 20;

    public static void main(String[] args) {
        ArrayList<Person> people = new ArrayList();

        String fileContent = readFile("people.csv");
        String[] lines = fileContent.split("\n");


        for (String line : lines) {
            if (line == lines[0]) {
                continue;
            }

            String[] columns = line.split(",");
            String firstName = columns[1];
            String lastName = columns[2];
            String email = columns[3];
            String country = columns[4];
            String ipAddress = columns[5];
            Person person = new Person(Integer.valueOf(columns[0]), firstName, lastName, email, country, ipAddress);
            people.add(person);
        }

        Spark.get(
                "/",
                ((request, response) -> {
                    String offset = request.queryParams("offset"); //getting a query parameter equal to 20, first time we go to the site it will be null

                    int namesStart;

                    if (offset == null) {
                        namesStart = 0; //if it's null, like we know it will be from above, give it a default offset number of 0
                    } else {
                        namesStart = Integer.valueOf(offset);
                    }

//                    if(!(namesStart<people.size())) {
//                        Spark.halt(403);
//                    }
//                    ArrayList<Person> peeps = new ArrayList<Person>(people.subList(namesStart, namesStart + SHOW_COUNT));

                    ArrayList<Person> peeps = new ArrayList<Person>(people.subList(
                            Math.max(0, Math.min(people.size(), namesStart)),
                            Math.max(0, Math.min(people.size(), namesStart + SHOW_COUNT))
                    ));

                    //^this will eliminate an error if "http://localhost:4567/?offset=990" this is entered into the URL or if we go over people.size();
                    //this replaces the:
                    // ArrayList<Person> peeps = new ArrayList<Person>(people.subList(namesStart, namesStart + SHOW_COUNT));
                    // if(!(namesStart<people.size())) {Spark.halt(403);} above.
                    //this will say first, make sure it's not too big and then make sure it's not too small.

                    HashMap m = new HashMap();
                    m.put("people", peeps); //pass ArrayList: peeps into the hashmap to show in the html where we call "people"
                    m.put("oldOffset", namesStart - SHOW_COUNT); //"oldOffset" calls {{oldOffset}} from people.html
                    m.put("newOffset", namesStart + SHOW_COUNT); //"newOffset" calls {{newOffset}} from people.html

                    boolean showNext = namesStart + SHOW_COUNT < people.size();
                    m.put("showNext", showNext); //will stop showing the "next" button when there will be no more names to display on another page

                    boolean showPrev = namesStart > 0 ;
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
                    Person person = people.get(idNum -1);
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
}
