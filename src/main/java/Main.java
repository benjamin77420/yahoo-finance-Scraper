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
    //check if the file is uploading to git 
    static MongoClient mongoClient = new MongoClient("localhost", 27017);
    static MongoDatabase database = mongoClient.getDatabase("stocks");
    static MongoCollection<org.bson.Document> updatingStatus = database.getCollection("updatingTable");

    public static void main(String[] args) {
        //TODO: collect data from a number of tables in the web page, implament multi threading.
        final String mainUrl = "https://finance.yahoo.com/cryptocurrencies?count=100&offset=";
        scrapCryptoYahoo(mainUrl);
    }

    public static void scrapCryptoYahoo(String mainUrl){

        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);
        try{
            //getting all the data from the cryprotable at the website
            //scrap the col values
            List<String> coinIndexes = getColumbsName(mainUrl);
            List<org.bson.Document> tableRowsData = getRowsData(mainUrl, coinIndexes);//scrap the raw data of the table//a list that willl store all the coins

            String collectionName = "Cryptocurrencies";

            MongoCollection<org.bson.Document> collection = database.getCollection(collectionName);
            setToUpdating(collectionName);
            clearCollection(collection);
            collection.insertMany(tableRowsData);//update the database with all the found crypto coins
            setToUpdated(collectionName);
            System.out.println("end of program");

        }
        catch (Exception e){
            e.printStackTrace();
        }
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
                    tableRowsData.add(new org.bson.Document()
                            .append(coinIndexes.get(0), tdTagsNodes.child(0).text())
                            .append(coinIndexes.get(1), tdTagsNodes.child(1).text())
                            .append(coinIndexes.get(2), tdTagsNodes.child(2).text())
                            .append(coinIndexes.get(3), tdTagsNodes.child(3).text())
                            .append(coinIndexes.get(4), tdTagsNodes.child(4).text())
                            .append(coinIndexes.get(5), tdTagsNodes.child(5).text())
                            .append(coinIndexes.get(6), tdTagsNodes.child(6).text())
                            .append(coinIndexes.get(7), tdTagsNodes.child(7).text())
                            .append(coinIndexes.get(8), tdTagsNodes.child(8).text()));
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

        if(hasIndexes.isPresent()){//if found class return it
            return hasIndexes;
        }

        return Optional.empty();//return a null option
    }

    public static void setToUpdated(String collectionName){//setting the status table to updated
        updatingStatus.updateOne(Filters.eq("CollectionName", collectionName), new org.bson.Document("$set", new org.bson.Document("Status", "updated")));
        System.out.println("set to updated");
    }

    public static void setToUpdating(String collectionName){//setting the status table to updating
        updatingStatus.updateOne(Filters.eq("CollectionName", collectionName), new org.bson.Document("$set", new org.bson.Document("Status", "updated")));
        System.out.println("set to updating");
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



