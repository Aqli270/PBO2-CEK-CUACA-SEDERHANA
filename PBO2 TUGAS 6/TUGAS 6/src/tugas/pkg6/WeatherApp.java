package tugas.pkg6;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.json.JSONObject;

public class WeatherApp extends JFrame {
    private JComboBox<String> cityComboBox;
    private JButton checkWeatherButton, saveDataButton;
    private JLabel weatherImageLabel;
    private JTextField temperatureField;
    private JTable weatherDataTable;
    private DefaultTableModel tableModel;
    private List<String> favoriteCities = new ArrayList<>();
    private static final String API_KEY = "0356412955e94511aa473036240411"; // Replace with your WeatherAPI API key

    public WeatherApp() {
        setTitle("Weather Checking Application");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        cityComboBox = new JComboBox<>(new String[]{"New York", "London", "Tokyo"});
        checkWeatherButton = new JButton("Check Weather");
        saveDataButton = new JButton("Save Data");
        weatherImageLabel = new JLabel();
        temperatureField = new JTextField(10);
        temperatureField.setEditable(false);

        // Table to display weather data
        tableModel = new DefaultTableModel(new Object[]{"City", "Temperature", "Condition"}, 0);
        weatherDataTable = new JTable(tableModel);

        // Add components to the frame
        add(cityComboBox);
        add(checkWeatherButton);
        add(weatherImageLabel);
        add(temperatureField);
        add(new JScrollPane(weatherDataTable));
        add(saveDataButton);

        // Action listener for checking weather
        checkWeatherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedCity = (String) cityComboBox.getSelectedItem();
                fetchWeatherData(selectedCity);
            }
        });

        // Item listener for city selection
        cityComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // Optionally handle city selection change if needed
            }
        });

        // Action listener for saving data
        saveDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveWeatherData();
            }
        });

        // Load weather data from CSV on startup
        loadWeatherData();

        setVisible(true);
    }

    // Method to fetch weather data from the API
    private void fetchWeatherData(String city) {
        try {
            String urlString = "http://api.weatherapi.com/v1/current.json?q=" + city + "&key=" + API_KEY;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse the weather data and update UI
            parseWeatherData(response.toString(), city);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Method to parse weather data from the API response
    private void parseWeatherData(String jsonResponse, String city) {
        try {
            // Parse the JSON response
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject current = jsonObject.getJSONObject("current");
            double temp = current.getDouble("temp_c"); // Get temperature in Celsius
            JSONObject condition = current.getJSONObject("condition");
            String weatherCondition = condition.getString("text").toLowerCase(); // Get weather condition text

            // Update temperature field
            temperatureField.setText(temp + "°C");

            // Update the weather image based on condition
            updateWeatherImage(weatherCondition);

            // Add weather data to the table
            tableModel.addRow(new Object[]{city, temp + "°C", weatherCondition});

            // Add the city to the favorites list if not already added
            if (!favoriteCities.contains(city)) {
                favoriteCities.add(city);
                cityComboBox.addItem(city); // Add city to combo box if new
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to update the weather image based on weather condition
    private void updateWeatherImage(String condition) {
        // Update the weather image based on condition
        if (condition.contains("clear")) {
            weatherImageLabel.setIcon(new ImageIcon("images/sunny_image.png"));
        } else if (condition.contains("cloud")) {
            weatherImageLabel.setIcon(new ImageIcon("images/cloudy_image.png"));
        } else if (condition.contains("rain")) {
            weatherImageLabel.setIcon(new ImageIcon("images/rainy_image.png"));
        } else if (condition.contains("snow")) {
            weatherImageLabel.setIcon(new ImageIcon("images/snowy_image.png"));
        }
    }

    // Method to save weather data to a CSV file
    // Method to save weather data to a CSV file in the Downloads folder
private void saveWeatherData() {
    try {
        // Get the user's home directory
        String userHome = System.getProperty("user.home");

        // Determine the path to the Downloads folder (works on Windows and macOS)
        File downloadFolder = new File(userHome, "Downloads");

        // Create the CSV file path
        File csvFile = new File(downloadFolder, "weather_data.csv");

        // Create a BufferedWriter to write to the CSV file
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
        writer.write("City,Temperature,Condition\n");

        // Write each row of weather data from the table
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String city = (String) tableModel.getValueAt(i, 0);
            String temp = (String) tableModel.getValueAt(i, 1);
            String condition = (String) tableModel.getValueAt(i, 2);
            writer.write(city + "," + temp + "," + condition + "\n");
        }
        writer.close();

        // Inform the user that the data has been saved
        JOptionPane.showMessageDialog(this, "Weather data saved to " + csvFile.getAbsolutePath());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    // Method to load weather data from CSV file into the table
    private void loadWeatherData() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("weather_data.csv"));
            String line;
            reader.readLine(); // Skip header row
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                tableModel.addRow(new Object[]{data[0], data[1], data[2]});
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main method to run the application
    public static void main(String[] args) {
        new WeatherApp();
    }
}
