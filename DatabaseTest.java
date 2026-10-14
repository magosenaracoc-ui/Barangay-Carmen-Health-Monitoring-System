package barangaycarmenhealthmonitoringsystem;

import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        Connection connection =
                DatabaseConnection.getConnection();

        if (connection != null) {

            System.out.println(
                    "SUCCESS: Connected to barangay_carmen_db!"
            );

            try {

                connection.close();

                System.out.println(
                        "Connection closed."
                );

            } catch (Exception e) {

                e.printStackTrace();
            }

        } else {

            System.out.println(
                    "FAILED: Could not connect to database."
            );
        }
    }
}

