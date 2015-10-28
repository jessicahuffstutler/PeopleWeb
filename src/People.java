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
                    String offset = request.queryParams("offset");

                    int namesStart;

                    if (offset == null) {
                        namesStart = 0;
                    } else {
                        namesStart = Integer.valueOf(offset);
                    }

                    if(!(namesStart<people.size())) {
                        Spark.halt(403);
                    }

                    ArrayList<Person> peeps = new ArrayList<Person>(people.subList(namesStart, namesStart + 20));

                    HashMap m = new HashMap();
                    m.put("people", peeps);
                    m.put("offset", namesStart + 20);

                    return new ModelAndView(m, "people.html");
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
