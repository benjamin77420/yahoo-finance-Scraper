import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    //program static variables
    //database variables
    static MongoClient mongoClient = new MongoClient("localhost", 27017);
    static MongoDatabase database = mongoClient.getDatabase("stocks");
    static MongoCollection<org.bson.Document> updatingStatus = database.getCollection("updatingTable");

    public static void main(String[] args) throws InterruptedException {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);

        new Thread(() -> {
            scrapTableData("https://finance.yahoo.com/cryptocurrencies?count=100&offset=", "Cryptocurrencies");
        }).start();

        new Thread(() -> {
            scrapTableData("https://finance.yahoo.com/trending-tickers?count=100&offset=", "Trending Tickers");
        }).start();

        new Thread(() -> {
            scrapTableData("https://finance.yahoo.com/most-active?count=100&offset=", "Most Actives");
        }).start();

        new Thread(() -> {
            scrapTableData("https://finance.yahoo.com/gainers?count=100&offset=", "Gainers");
        }).start();

        new Thread(() -> {
            scrapTableData("https://finance.yahoo.com/losers?count=100&offset=", "Losers");
        }).start();

        new Thread(() -> {
            scrapTableData("https://finance.yahoo.com/commodities?count=100&offset=", "Commodities");
        }).start();

        new Thread(() -> {
            scrapTableData("https://finance.yahoo.com/world-indices?count=100&offset=", "World indices");
        }).start();

        new Thread(() -> {
            scrapTableData("https://finance.yahoo.com/currencies?count=100&offset=", "Currencies");
        }).start();


        new Thread(() -> {
            scrapTableData("https://finance.yahoo.com/mutualfunds?count=100&offset=", "Top Mutual Funds");
        }).start();
    }

    public static void scrapTableData(String mainUrl, String collectionTarget){
        System.out.println("started workin on " + collectionTarget);

        try{
            //getting all the data from the cryprotable at the website
            //scrap the col values
            List<String> coinIndexes = getColumbsName(mainUrl);
            List<org.bson.Document> tableRowsData = getRowsData(mainUrl, coinIndexes);//scrap the raw data of the table//a list that willl store all the coins

            MongoCollection<org.bson.Document> collection = database.getCollection(collectionTarget);
            setToUpdating(collectionTarget);
            clearCollection(collection);
            collection.insertMany(tableRowsData);//update the database with all the found crypto coins
            setToUpdated(collectionTarget);


        }
        catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("end of updating for " + collectionTarget);
    }

    public static List<org.bson.Document> getRowsData(String url, List<String> coinIndexes) throws IOException {
        List<org.bson.Document> tableRowsData = new ArrayList<>();
        int coinOffset = 0;
        int fullTableSize = 0;
        Document mainPage = Jsoup.connect(url + coinOffset).get();
        Optional<Elements> tableIndexesElement = hasTableRange(mainPage);//creating an optional elements object that will handle a case of a one page table

        if(tableIndexesElement.isPresent())//there is a multi page table that needs several url changes, fullTableSize is changes accordingly
            fullTableSize = Integer.parseInt(tableIndexesElement.get().text().split(" ")[2]);//getting the section that index the size of the full table

        do{//while not reached the end of the table
            mainPage = Jsoup.connect(url + coinOffset).get();//changed to the currnt offset of the table row
            Elements tableData = mainPage.getElementsByTag("tbody");//get the body of the main table
            if(tableData == null) return tableRowsData;// basic check if the query to the main page was successful

            for(Element trTagsNodes : tableData)//getting all the 'tr' tags fro the table body
                for(Element tdTagsNodes : trTagsNodes.children()){//getting all the 'td' taags from every individual row('tr' tag)
                    org.bson.Document currntDoc = new org.bson.Document();
                    for(int i=0; i<coinIndexes.size(); ++i)
                        currntDoc.append(coinIndexes.get(i), tdTagsNodes.child(i).text());

                    tableRowsData.add(currntDoc);

                    ++coinOffset;
                }
        }while (coinOffset < fullTableSize);

        return tableRowsData;
    }

    public static List<String> getColumbsName(String url) throws IOException {
        Document mainPage = Jsoup.connect(url + 0).get();
        Elements tableToolBar = mainPage.getElementsByClass("C($tertiaryColor) BdB Bdbc($seperatorColor)");

        List<String> coinIndexes = new ArrayList<>();

        if(tableToolBar == null) return coinIndexes;//basic check for correct paramiter passing, return empty list if fails.

        for(Element colName : tableToolBar.tagName("span"))//adding all the columns names to a list that will be used to construct our stocks database.
            for(Element chiled : colName.children())
                coinIndexes.add(chiled.text());

        return coinIndexes;
    }

    public static Optional<Elements> hasTableRange(Document mainPage){
        Optional<Elements> hasIndexes;

        hasIndexes = Optional.ofNullable(mainPage.getElementsByClass("Mstart(15px) Fw(500) Fz(s)"));//check if there is a class of this nake

        if(hasIndexes.get().size() != 0){//if found class return it
            return hasIndexes;
        }

        return Optional.empty();//return a null option
    }

    public static void setToUpdated(String collectionName){//setting the status table to updated
        updatingStatus.updateOne(Filters.eq("CollectionName", collectionName), new org.bson.Document("$set", new org.bson.Document("Status", "updated")));
    }

    public static void setToUpdating(String collectionName){//setting the status table to updating
        updatingStatus.updateOne(Filters.eq("CollectionName", collectionName), new org.bson.Document("$set", new org.bson.Document("Status", "updated")));
    }

    public static void clearCollection(MongoCollection<org.bson.Document> collection){//clearing a collection for the new valuse
        collection.deleteMany(new org.bson.Document());
    }
    /*
    public static void checkIfUpdating(String collectionName){//this will hold the thread until the table will be in a updated status, will be used as a semaphore.
        org.bson.Document answer;
        do{
            answer = updatingStatus.find(Filters.and(Filters.eq("CollectionName", collectionName), Filters.eq("Status", "updated"))).first();
        }while (answer == null);
    }
     */
}



