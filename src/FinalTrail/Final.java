package FinalTrail;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.apache.logging.log4j.*;






import static java.lang.Character.isAlphabetic;

public class Final implements ActionListener {
	private static org.apache.logging.log4j.Logger demoLogger=LogManager.getLogger(Final.class.getName());
    JFrame frame;
    JTextField textField;
    JButton searchButton;

    CardLayout cardLayout;
    
    JPanel cards;
    // JButton nextButton;
    GridLayout grid;

    JPanel panel;
    JLabel label;

    ActionListener searchListener;

    List<Country> countryList;

    JLabel searchResultLabel;
    JPanel searchResultPanel;
    GridLayout searchGridLayout;

    public Final() {
        frame = new JFrame("UNData");

        textField = new JTextField(20);
        searchButton = new JButton("Search");

        // Add a card layout manager to the frame
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // nextButton = new JButton("Next");

        // pool = Executors.newFixedThreadPool(5);

        grid = new GridLayout(1, 3);
        panel = new JPanel(grid);

        label = new JLabel("Enter country name:");

        searchButton.addActionListener(this);

        countryList = new ArrayList<Country>();

        panel.add(label);
        panel.add(textField);
        panel.add(searchButton);

        frame.getContentPane().add(BorderLayout.NORTH, panel);
        frame.getContentPane().add(cards, BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(600, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void everything() throws Exception {
        // url to download from the main html page
        String url = "https://data.un.org/";
        String indexUrl = url + "en/index.html";
        // download info of all countries and create Country objects
        try {
            Document doc = Jsoup.connect(indexUrl).timeout(60).get();
            Element countryResults = doc.getElementById("myUL");
            Elements countries = countryResults.getElementsByTag("li");

            int index = 1;

            for(Element country : countries) {
                String link = url + "en/" + country.getElementsByTag("a").get(0).attr("href");
                String flag = country.getElementsByTag("td").get(1).getElementsByTag("img").attr("src");
                flag = url + flag.substring(3);

                String names = country.getElementsByTag("td").get(3).text();

                // separating countryName and continent
                String continent = country.getElementsByTag("td").get(3).getElementsByTag("small").text();
                String countryName = names.substring(0, names.length() - continent.length());

                try {
                    Document docDetailed = Jsoup.connect(link).get();
                    Elements detailElements = docDetailed.getElementsByClass("pure-menu-list");
                    Element details = detailElements.get(0);
                    Elements detailTr = details.getElementsByTag("tr");

                    String region = detailTr.get(0).getElementsByTag("td").get(2).text();

                    String population = detailTr.get(1).getElementsByTag("td").get(2).text();
                    population = removeLastLetter(population) + " 000";

                    String popDensity = detailTr.get(2).getElementsByTag("td").get(2).text();
                    popDensity = removeLastLetter(popDensity);

                    String capitalCity = detailTr.get(3).getElementsByTag("td").get(2).text();

                    String capCityPop = detailTr.get(4).getElementsByTag("td").get(2).text();
                    capCityPop = removeLastLetter(capCityPop);

                    String membershipDate = detailTr.get(5).getElementsByTag("td").get(2).text();

                    String surfaceArea = detailTr.get(6).getElementsByTag("td").get(2).text();
                    surfaceArea = removeLastLetter(surfaceArea);

                    String sexRatio = detailTr.get(7).getElementsByTag("td").get(2).text();
                    sexRatio = removeLastLetter(sexRatio);

                    String currency = detailTr.get(8).getElementsByTag("td").get(2).text();
                    currency = removeLastLetter(currency);

                    String exchangeRate = detailTr.get(9).getElementsByTag("td").get(2).text();
                    exchangeRate = removeLastLetter(exchangeRate);

                    Country newCountry = new Country(link, flag, countryName, continent, region, population,
                            popDensity, capitalCity, capCityPop, membershipDate,
                            surfaceArea, sexRatio, currency, exchangeRate);
                    countryList.add(newCountry);
                } catch (HttpStatusException ex) {
                    System.out.println("Error: " + ex.getMessage());
                    // Quit
                    System.exit(0);
                }

                System.out.println(index + "/" + countries.size());
                index++;
            }
        } catch (HttpStatusException ex) {
            System.out.println("Error: " + ex.getMessage());
            demoLogger.error("An error has been occured");
            // Quit
            System.exit(0);
        }

        //System.out.println(countryList.get(0).getFlag());
    }

    public static String removeLastLetter(String s) {
        if(isAlphabetic(s.charAt(s.length() - 1)) && !isAlphabetic(s.charAt(s.length() - 2)))
            return s.substring(0, s.length() - 1);
        return s;
    }

    public static Country search(ArrayList<Country> countryList, String s) {
        for(Country country : countryList) {
            if(country.getCountryName().toLowerCase(Locale.ROOT).trim().contains(s.toLowerCase(Locale.ROOT).trim()))
                return country;
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Button pressed");
        demoLogger.info("Button pressed");
        int rows = 10;
        searchButton.setText("Searching...");

        Country country = search((ArrayList<Country>) countryList, textField.getText());

        // if country is not found
        if(country == null) {
            searchButton.setText("not found");
            demoLogger.error("Country not found");
            searchResultLabel = new JLabel("country not found");
            searchResultPanel.add(searchResultLabel);
            rows = 0;
        }


        // if country is found
        else {
            searchButton.setText("found");
            List<String> properties = Arrays.asList("Region", "Population",
                    "Pop Density (per km2)", "Capital City", "Capital city population",
                    "UN Membership Date", "Surface area (km2)", "sex ratio", "currency",
                    "exchange rate");


            GridLayout propertiesLayout = new GridLayout(rows, 1);
            JPanel propertiesPanel = new JPanel(propertiesLayout);

            searchGridLayout = new GridLayout(rows, 1);
            searchResultPanel = new JPanel(searchGridLayout);

            for(String element : properties) {
                JLabel label = new JLabel(element);
                propertiesPanel.add(label);
            }

            // get the properties of the countries
            JLabel label = new JLabel(country.getRegion());
            searchResultPanel.add(label);

            label = new JLabel(country.getPopulation());
            searchResultPanel.add(label);

            label = new JLabel(country.getPopDensity());
            searchResultPanel.add(label);

            label = new JLabel(country.getCapitalCity());
            searchResultPanel.add(label);

            label = new JLabel(country.getCapCityPop());
            searchResultPanel.add(label);

            label = new JLabel(country.getMembershipDate());
            searchResultPanel.add(label);

            label = new JLabel(country.getSurfaceArea());
            searchResultPanel.add(label);

            label = new JLabel(country.getSexRatio());
            searchResultPanel.add(label);

            label = new JLabel(country.getCurrency());
            searchResultPanel.add(label);

            label = new JLabel(country.getExchangeRate());
            searchResultPanel.add(label);

            JPanel imagePanel = new JPanel();
            try {
                URL flagURL = new URL(country.getFlag());
                BufferedImage flagImage = ImageIO.read(flagURL);

                System.out.println(country.getFlag());

                if(flagImage != null)
                    label = new JLabel(new ImageIcon(flagImage));
                else
                    label = new JLabel("No image");

                imagePanel.add(label);
            } catch (MalformedURLException ex) {
                System.out.println("Malformed URL");
            } catch (IOException iox) {
                System.out.println("Can not load file");
            }



            // frame.add(searchResultPanel);
            frame.getContentPane().add(BorderLayout.WEST, propertiesPanel);
            frame.getContentPane().add(BorderLayout.EAST, searchResultPanel);
            frame.getContentPane().add(BorderLayout.CENTER, imagePanel);
//            frame.setResizable(false);


        }

    }
}


class Country {
    private String link;
    private String flag;
    private String countryName;
    private String continent;

    private String region;
    private String population;
    private String popDensity;
    private String capitalCity;
    private String capCityPop;

    private String membershipDate;
    private String surfaceArea;
    private String sexRatio;
    private String currency;
    private String exchangeRate;

    public Country(String link, String flag, String countryName, String continent,
                   String region, String population, String popDensity, String capitalCity,
                   String capCityPop, String membershipDate, String surfaceArea,
                   String sexRatio, String currency, String exchangeRate) {
        this.link = link;
        this.flag = flag;
        this.countryName = countryName;
        this.continent = continent;
        this.region = region;
        this.population = population;
        this.popDensity = popDensity;
        this.capitalCity = capitalCity;
        this.capCityPop = capCityPop;
        this.membershipDate = membershipDate;
        this.surfaceArea = surfaceArea;
        this.sexRatio = sexRatio;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
    }

    public Country(String link, String flag, String countryName, String continent) {
        this.link = link;
        this.flag = flag;
        this.countryName = countryName;
        this.continent = continent;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPopulation() {
        return population;
    }

    public void setPopulation(String population) {
        this.population = population;
    }

    public String getPopDensity() {
        return popDensity;
    }

    public void setPopDensity(String popDensity) {
        this.popDensity = popDensity;
    }

    public String getCapitalCity() {
        return capitalCity;
    }

    public void setCapitalCity(String capitalCity) {
        this.capitalCity = capitalCity;
    }

    public String getCapCityPop() {
        return capCityPop;
    }

    public void setCapCityPop(String capCityPop) {
        this.capCityPop = capCityPop;
    }

    public String getMembershipDate() {
        return membershipDate;
    }

    public void setMembershipDate(String membershipDate) {
        this.membershipDate = membershipDate;
    }

    public String getSurfaceArea() {
        return surfaceArea;
    }

    public void setSurfaceArea(String surfaceArea) {
        this.surfaceArea = surfaceArea;
    }

    public String getSexRatio() {
        return sexRatio;
    }

    public void setSexRatio(String sexRatio) {
        this.sexRatio = sexRatio;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String toString() {
        return link + "\n" + flag + "\n" + countryName + "\n" + region +
                "\n" + population + "\n" + popDensity + "\n" +
                capitalCity + "\n" + capCityPop + "\n" + membershipDate +
                "\n" + surfaceArea + "\n" + sexRatio + "\n" + currency
                + "\n" + exchangeRate;
    }
}