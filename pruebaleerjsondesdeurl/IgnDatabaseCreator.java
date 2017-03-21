/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebaleerjsondesdeurl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.*;

/**
 *
 * @author Juan Pablo
 */
public class IgnDatabaseCreator {
    //When I say "element", I am referring to either an article or a video
    
    Map<String,String> thumbnailsKeys[][][]; //3D array of HashMaps; each HashMap has ONE key and value, corresponding to ONE thumbnail of a specific element
    //thumbnailsKeys[i][j][k] would refer to the k-th key-value pair, of the j-th thumbnail, of the i-th element
    
    Map<String,String> metadata[][]; //2D array of HashMaps; each HashMap has ONE key and value, corresponding to a specific element
    //metadata[i][j] would refer to the j-th key-value pair of metadata, of the i-th element
    
    String tags[][]; //2D array of Strings; each String is ONE tag of a specific element
    //tags[i][j] would refer to the j-th tag of the i-th element
    
    String networks[][]; //2D array of Strings; each String is ONE network of a specific element
    //networks[i][j] would refer to the j-th network of the i-th element
    
  private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder(); 
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
    InputStream is = new URL(url).openStream(); // opens stream
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String jsonText = readAll(rd); // read all of the document
      JSONObject json = new JSONObject(jsonText); // transform it to json object
      return json;
    } 
    finally {
      is.close(); // closes stream
    }
  }
  
  /**
   * This is a json parser. It works specifically with the jsons returned by the
   * url http://ign-apis.herokuapp.com/endpoint?startIndex=x&count=y so it does
   * not work for general json objects.
   * @param json The json object to parse
   */
  public void obtainData(JSONObject json){
      //When I say "element", I am referring to either an article or a video
      int numberOfElements = json.getInt("count"); // the total number of elements
      JSONArray data = json.getJSONArray("data");  // the data of all of the numberOfElements elements
      JSONObject elements[] = new JSONObject[numberOfElements]; //array of JSONObjects; each JSONObject corresponds to an element
      JSONArray thumbnailsArray[] = new JSONArray[numberOfElements]; //array of JSONArrays; each JSONArray corresponds to ALL the thumbnails a specific element has
      JSONObject thumbnails[][] = new JSONObject[numberOfElements][];//2D array of JSONObjects; each JSONObject corresponds to ONE thumbnail of a specific element
      thumbnailsKeys = new HashMap[numberOfElements][][]; //3D array of HashMaps; each HashMap has ONE key and value, corresponding to ONE thumbnail of a specific element
      JSONObject metadataObject[] = new JSONObject[numberOfElements]; //array of JSONObjects; each JSONObject corresponds to ALL the metadata of a specific element
      metadata = new HashMap[numberOfElements][]; //2D array of HashMaps; each HashMap has ONE key and value, corresponding to a specific element
      JSONArray tagsArray[] = new JSONArray[numberOfElements]; //array of JSONArrays; each JSONArray corresponds to ALL the tags a specific element has
      tags = new String[numberOfElements][]; //2D array of Strings; each String is ONE tag of a specific element
      networks = new String[numberOfElements][]; //2D array of Strings; each String is ONE network of a specific element
      for(int i=0; i< numberOfElements; i++){
          System.out.println("ELEMENT " + (i+1) + " of current page: ");
          
          // the data of all elements (articles or videos)
          elements[i] = data.getJSONObject(i);
          
          // thumbnails
          thumbnailsArray[i] = elements[i].getJSONArray("thumbnails");
          thumbnails[i] = new JSONObject[thumbnailsArray[i].length()];
          thumbnailsKeys[i] = new HashMap[thumbnails[i].length][];
          System.out.println("Thumbnails:");
          for(int j=0; j<thumbnails[i].length; j++){
              thumbnails[i][j] = thumbnailsArray[i].getJSONObject(j);
              //System.out.println(thumbnails[i][j]);
              thumbnailsKeys[i][j]=new HashMap[thumbnails[i][j].length()];
              
              System.out.println("   Thumbnail " + (j+1));
              int k=0;
              Iterator<String> keys = thumbnails[i][j].keys(); //going to iterate
              String next;
              while(keys.hasNext()){
                  next = keys.next();
                  thumbnailsKeys[i][j][k]=new HashMap<String,String>();
                  if(thumbnails[i][j].get(next) instanceof String){ //String
                    thumbnailsKeys[i][j][k].put(next,thumbnails[i][j].getString(next));
                  }
                  else{// integer
                    thumbnailsKeys[i][j][k].put(next,thumbnails[i][j].getInt(next)+""); 
                  }
                  System.out.println("      "+thumbnailsKeys[i][j][k].toString());
                  k++;
              }
          }
          
          
          // metadata
          metadataObject[i] = elements[i].getJSONObject("metadata");
          metadata[i] = new HashMap[metadataObject[i].length()];
          
          System.out.println("Metadata: ");
          int m = 0;
          Iterator<String> keys = metadataObject[i].keys(); //going to iterate
          String nextM;
          while(keys.hasNext()){
              nextM = keys.next();
              metadata[i][m] = new HashMap<String,String>();
              
              if(metadataObject[i].get(nextM) instanceof String){ //String
                metadata[i][m].put(nextM,metadataObject[i].getString(nextM));
              }
              else if(metadataObject[i].get(nextM) instanceof Integer) {// Integer
                metadata[i][m].put(nextM,metadataObject[i].getInt(nextM)+"");  
              }
              else{ // the only other case is the array of the networks
                  JSONArray nets = metadataObject[i].getJSONArray(nextM);
                  metadata[i][m].put(nextM,nets.toString()); //I'll deal with this later
                  networks[i] = new String[nets.length()];
                  for(int n=0;n<networks[i].length;n++){
                      networks[i][n]=nets.getString(n);
                      System.out.println("   Network "+(n+1)+": "+networks[i][n]);
                  }
              }
              System.out.println("   "+metadata[i][m].toString());
              m++;
          }
          
          //System.out.println("Metadata: " + metadataObject[i]);
          
          //tags
          tagsArray[i] = elements[i].getJSONArray("tags");
          tags[i] = new String[tagsArray[i].length()];
          System.out.println("Tags: ");
          for(int j=0; j<tags[i].length; j++){
              tags[i][j] = tagsArray[i].getString(j);
              System.out.println("   "+tags[i][j]);
          }
          System.out.println("");
          
          
      }
  }
  
  public String[] createUrls(String url, ArrayList<String> endPts, ArrayList<String> suppParam, int[][] parameters, int n){
      // A somewhat general code of creation of urls. 
    String urls[] = new String[endPts.size()*n]; //  n urls per end point
    int index; // index of the url array
    for(int i=0;i<endPts.size();i++){
        for(int j=0;j<n;j++){
            index = i*n + j; // this effectively makes index increment by 1 each time
            urls[index]=url+endPts.get(i); //url with an endpoint
            if(suppParam.size()>0){ //adding parameters
                urls[index]=urls[index]+"?";
                for(int k=0;k<suppParam.size()-1;k++){
                    urls[index] = urls[index] + suppParam.get(k) + "=" + parameters[index][k] + "&";
                }
                urls[index] = urls[index] + suppParam.get(suppParam.size()-1) + "=" + parameters[index][suppParam.size()-1];
            }
        }
    }
    return urls;
  }
  
  public static boolean containsKey(Map<String,String>[] maps, String key) {
        boolean contains = false;
        for(Map<String,String> map : maps){
            if(map.get(key) != null){
                contains = true;
                break;
            }
        }
        
        return contains;
  }
  
  // This is a weird way of making an array of Maps work as a hash table... but, it works!
  public static String valueOfKey(Map<String,String>[] maps, String key) {
      String value = "";
      for(Map<String,String> map : maps){
            value = map.get(key);
            if(value != null)
                break;
        }
      return value;
  }
  
  public static String quoteProof(String s){ //escape ' and " characters correctly
      for(int i=0;i<s.length();i++){
          if(s.charAt(i) == '\'' || s.charAt(i) == '\"'){
              s = s.substring(0, i) + "\\" + s.substring(i, s.length());
              i++;
          }
      }
      return s;
  }
  
  public static String toDatetime(String dt){
      return dt.substring(0, 10)+" "+dt.substring(11,19);
  }

  public static void main(String[] args) throws IOException, JSONException, URISyntaxException {
    System.out.println("Welcoooome! This app will pull a list of articles and videos. ");
    System.out.println("It will also connect to a LOCAL EMPTY database called \"ignmedia\"" + 
                       " (jdbc:mysql://localhost:3306/ignmedia),");
    System.out.println("create the tables, and update the tables with all the info. \n\n");
    // if the tables are already created (from a previous run, for example), an exception will occur
    // solution to this: drop all the existing tables before running this code again
    
    String url = "http://ign-apis.herokuapp.com"; // this is the url
    JSONObject json = readJsonFromUrl(url); // get the JSON from the url
    IgnDatabaseCreator chief = new IgnDatabaseCreator(); // the chief will do many important things :)
    
    JSONArray endpoints = json.getJSONArray("endpoints"); //gets the array of the endpoints
    ArrayList<String> endPts = new ArrayList<String>();
    for (int i = 0; i < endpoints.length(); i++) {
        endPts.add(endpoints.get(i).toString()); //endPts will have all the endpoints
    }
    //System.out.println(endPts);
    
    JSONObject supportedParameters = json.getJSONObject("supportedParameters"); //parameters
    ArrayList<String> suppParam = new ArrayList<String>();
    Iterator<String> keys = supportedParameters.keys(); //going to iterate through the JSON
    while(keys.hasNext()){
        suppParam.add(keys.next()); //suppParam will have all the supported parameters
    }
    //System.out.println(suppParam);
    
    //String jsonString = json.toString(); //gets the whole JSON as a string
    //System.out.println(json.toString()+"\n"); //prints the whole JSON
    
    int number = 3; //number of urls (pages) to be generated per endpoint
    
    // Here you can define the starting index and the count for the endpoints.
    // Being back-end, I decided not to ask the user for these parameters.
    // Rather, if they need to be changed, they have to be changed from here.
    int parameters[][] = new int[endPts.size()*number][suppParam.size()];
    for(int i=0;i<endPts.size();i++){
        // The values are similar for all endpoints, but this can be modified
        // so that the values can be different for each endpoint.
        // Also, I did this knowing the supported parameters beforehand
        for(int j=0;j<number;j++){
            parameters[i*number + j][0]=j*20; // startIndex = 0 or 20 or 40, for each endpoint
            parameters[i*number + j][1]=20;// count = 20, for each endpoint
        }
    }
    
    
    //MySQL
    // I M P O R T A N T   N O T E : I created a local DataBase called "ignmedia"
    // ( jdbc:mysql://localhost:3306/ignmedia )
    // with help of the NetBeans IDE, so that the following lines of code are
    // only to CONNECT with it (and not to create it).
    
    System.out.println("Waiting...");
    long time=System.currentTimeMillis();
    while(System.currentTimeMillis()<time+3000);
    
    // The next lines load the driver (standard procedure)
    System.out.println("Loading driver...");
    try {
        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("com.mysql.jdbc.Driver is loaded");
    } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Driver not found", e);
    }
    
    chief.createTables(); //here we create all the tables (empty up to this moment)
    
    System.out.println("Waiting...");
    time=System.currentTimeMillis();
    while(System.currentTimeMillis()<time+1000);
    
    //back to getting the jsons:
    //first, get the urls
    
    String urls[] = chief.createUrls(url, endPts, suppParam, parameters, number);
    
    
    System.out.println("The following lines show the information obtained through the JSONs.\n");
    //second, reading the json; third, obtaining the data from them; finally, updating the database
    JSONObject jsons[] = new JSONObject[urls.length];
    for(int i=0;i<urls.length;i++){
        System.out.println("\nURL of page: " + urls[i]);
        jsons[i] = readJsonFromUrl(urls[i]);
        System.out.println("----------------------------------------------------");
        chief.obtainData(jsons[i]); //each time this line is executed, the attributes thumbnailsKeys, metadata, tags, and networks are overwritten
        chief.updateTables();       //but this is no problem, because immediately after obtaining the data, it updates the Database
                                    //(i.e. the Database always has all the data obtained up to any given moment)
       
    }
    
    System.out.println("Done!");
  }
  
  
  public void createTables(){
      
    //these three lines have to be changed depending on the database name, the username, and the password
    String urlDB = "jdbc:mysql://localhost:3306/ignmedia?zeroDateTimeBehavior=convertToNull";
    String username = "root";
    String password = ""; //modify this
    
    
    //The next lines connect the database (standard procedure)
    System.out.println("Connecting database...");
    Connection connection = null;
    Statement state = null;
    try{
        connection = DriverManager.getConnection(urlDB, username, password);
        System.out.println("Database connected!");
        System.out.println("Creating the tables...");
        state = connection.createStatement();
        String sql;
        
        //creation of tables
        sql = "CREATE TABLE ELEMENT " +
                "(idElement INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                "type VARCHAR(255) check (type in ('article','video')))";
        state.executeUpdate(sql);
        
        sql = "CREATE TABLE THUMBNAIL " +
               "(idThumbnail INTEGER AUTO_INCREMENT PRIMARY KEY, " + 
               // this can cause occasionally repeated identical thumbnails; I  
               // preferred this over creating a whole new table to represent a 
               // relationship between a thumbnail and an element
               
                " url VARCHAR(255), " +
               " size VARCHAR(255) check (size in ('compact','medium','large')), " + 
               " width INTEGER, " + 
               " height INTEGER, " + 
               " idElement INTEGER references ELEMENT)";
        state.executeUpdate(sql);
        
        sql="CREATE TABLE METADATAV " +
            "(url VARCHAR(255) primary key, " +
            "duration INTEGER, " +
            "name VARCHAR(255), " +
            "publishDate DATETIME, " +
            "description VARCHAR(1023), " +
            "state VARCHAR(255), " +
            "slug VARCHAR(255), " +
            "idElement INTEGER unique references ELEMENT)";
        state.executeUpdate(sql);
        
        sql="CREATE TABLE METADATAA " +
            "(keyA INTEGER AUTO_INCREMENT PRIMARY KEY, " +
            "articleType VARCHAR(255), " +
            "subHeadline VARCHAR(255), " +
            "publishDate DATETIME, " +
            "state VARCHAR(255), " +
            "headline VARCHAR(255), " +
            "slug VARCHAR(255), " +
            "idElement INTEGER unique references ELEMENT)";
        state.executeUpdate(sql);
        
        sql="CREATE TABLE TAG " +
            "(tg VARCHAR(255) primary key)";
        state.executeUpdate(sql);
        
        sql="CREATE TABLE NETWORK " +
            "(net VARCHAR(255) primary key)";
        state.executeUpdate(sql);
        
        //creating NETWORKOFARTICLE and NETWORKOFVIDEO tables wasn't the best
        //design possible for the database :/ but, it works
        sql="CREATE TABLE NETWORKOFARTICLE " +
            "(net VARCHAR(255) references NETWORK, " +
            "keyA INTEGER references METADATAA, " +
            "primary key(net, keyA))";
        state.executeUpdate(sql);
        
        sql="CREATE TABLE NETWORKOFVIDEO " +
            "(net VARCHAR(255) references NETWORK, " +
            "url VARCHAR(255) references METADATAV, " +
            "primary key(net, url))";
        state.executeUpdate(sql);
        
        sql="CREATE TABLE TAGOFELEMENT " +
            "(tg VARCHAR(255) references TAG, " +
            "idElement INTEGER references ELEMENT, " +
            "primary key(tg, idElement))";
        state.executeUpdate(sql);
        
        
        // now we have to fill the tables with the values we have 
        
        
        
        
        if(state!=null)
            state.close();
        if(connection!=null)
            connection.close();
        
    } catch (SQLException e) {
        throw new IllegalStateException("Something went wrong!", e);
    }
  }
  
  
  /**
   * Updates the tables with the most recent data extracted.
   */
  public void updateTables(){
      
    String urlDB = "jdbc:mysql://localhost:3306/ignmedia?zeroDateTimeBehavior=convertToNull";
    String username = "root";
    String password = "5Zopnin5";
    
    //The next lines connect the database (standard procedure)
    System.out.println("Connecting database...");
    Connection connection = null;
    Statement state = null;
    try{
        connection = DriverManager.getConnection(urlDB, username, password);
        System.out.println("Database connected!");
        System.out.println("Updating the tables...");
        state = connection.createStatement();
        String sql;
        ResultSet rs;
        int idElement;
        
        //if "url" is a key of metadata[0], we are dealing with videos (only videos have "url" as a metadata key)
        if(containsKey(metadata[0],"url")){ 
            for(int i=0; i<metadata.length;i++){ // for each element, we will insert a series of values to several tables
                sql = "INSERT INTO ELEMENT (type)" +
                      "VALUES('video')";
                state.executeUpdate(sql);
                
                rs = state.executeQuery("SELECT LAST_INSERT_ID() as id");
                rs.next();
                idElement = rs.getInt("id");//revisar esto
                
                for(int j=0; j<thumbnailsKeys[i].length;j++){
                    sql = "INSERT INTO THUMBNAIL " +
                          "VALUES(null, "
                               + "'" + quoteProof(valueOfKey(thumbnailsKeys[i][j],"url")) + "', " 
                               + "'" + quoteProof(valueOfKey(thumbnailsKeys[i][j],"size"))+"', "
                                     + valueOfKey(thumbnailsKeys[i][j],"width") + ", "
                                     + valueOfKey(thumbnailsKeys[i][j],"height") + ", "
                                     + idElement + ")";
                    state.executeUpdate(sql);
                }
                
                String urlMD = valueOfKey(metadata[i],"url");
                sql = "INSERT INTO METADATAV " +
                      "VALUES('" + quoteProof(urlMD) + "', " 
                                 + valueOfKey(metadata[i],"duration") + ", "
                           + "'" + quoteProof(valueOfKey(metadata[i],"name"))+"', "
                           + "'" + toDatetime(valueOfKey(metadata[i],"publishDate"))+"', "
                           + "'" + quoteProof(valueOfKey(metadata[i],"description"))+"', "
                           + "'" + quoteProof(valueOfKey(metadata[i],"state"))+"', "
                           + "'" + quoteProof(valueOfKey(metadata[i],"slug"))+"', "
                                 + idElement+")";
                state.executeUpdate(sql);
                
                for(int j=0;j<tags[i].length;j++){
                    sql = "INSERT INTO TAG " +
                      "VALUES('" + quoteProof(tags[i][j]) +"') " +
                      "ON DUPLICATE KEY UPDATE tg=tg";
                    state.executeUpdate(sql);
                    
                    sql = "INSERT INTO TAGOFELEMENT " +
                      "VALUES('" + quoteProof(tags[i][j]) + "', " +
                                 + idElement + ") " +
                            "ON DUPLICATE KEY UPDATE tg=tg";
                    state.executeUpdate(sql);
                }
                
                for(int j=0;j<networks[i].length;j++){
                    sql = "INSERT INTO NETWORK " +
                      "VALUES('" + quoteProof(networks[i][j]) +"') " +
                      "ON DUPLICATE KEY UPDATE net=net";
                    state.executeUpdate(sql);
                    
                    sql = "INSERT INTO NETWORKOFVIDEO " +
                      "VALUES('" + quoteProof(networks[i][j]) + "', " +
                             "'" + quoteProof(urlMD) + "') " + 
                             "ON DUPLICATE KEY UPDATE net=net";
                    state.executeUpdate(sql);
                }
                
            }
        }
        else{ // if we don't find "url" as a key of metadata[0], we are dealing with articles
            int keyA;
            for(int i=0; i<metadata.length;i++){ // for each element, we will insert a series of values to several tables
                sql = "INSERT INTO ELEMENT (type)" +
                      "VALUES('article')";
                state.executeUpdate(sql);
                
                rs = state.executeQuery("SELECT LAST_INSERT_ID() as id");
                rs.next();
                idElement = rs.getInt("id");//revisar esto
                
                for(int j=0; j<thumbnailsKeys[i].length;j++){
                    sql = "INSERT INTO THUMBNAIL " +
                            "VALUES(null, "
                               + "'" + quoteProof(valueOfKey(thumbnailsKeys[i][j],"url")) + "', " 
                               + "'" + quoteProof(valueOfKey(thumbnailsKeys[i][j],"size"))+"', "
                                     + valueOfKey(thumbnailsKeys[i][j],"width") + ", "
                                     + valueOfKey(thumbnailsKeys[i][j],"height") + ", "
                                     + idElement + ")";
                    state.executeUpdate(sql);
                }
                
                sql = "INSERT INTO METADATAA " +
                      "VALUES(null, " 
                           + "'" + quoteProof(valueOfKey(metadata[i],"articleType")) + "', "
                           + "'" + quoteProof(valueOfKey(metadata[i],"subHeadline"))+"', "
                           + "'" + toDatetime(valueOfKey(metadata[i],"publishDate"))+"', "
                           + "'" + quoteProof(valueOfKey(metadata[i],"state"))+"', "
                           + "'" + quoteProof(valueOfKey(metadata[i],"headline"))+"', "
                           + "'" + quoteProof(valueOfKey(metadata[i],"slug"))+"', "
                                 + idElement+")";
                
                state.executeUpdate(sql);
                rs = state.executeQuery("SELECT LAST_INSERT_ID() as id");
                rs.next();
                keyA = rs.getInt("id");
                
                for(int j=0;j<tags[i].length;j++){
                    sql = "INSERT INTO TAG " +
                      "VALUES('" + quoteProof(tags[i][j]) +"') " +
                      "ON DUPLICATE KEY UPDATE tg=tg";
                    state.executeUpdate(sql);
                    
                    sql = "INSERT INTO TAGOFELEMENT " +
                      "VALUES('" + quoteProof(tags[i][j]) + "', " +
                                 + idElement + ") " +
                              "ON DUPLICATE KEY UPDATE tg=tg";
                    state.executeUpdate(sql);
                }
                
                for(int j=0;j<networks[i].length;j++){
                    sql = "INSERT INTO NETWORK " +
                      "VALUES('" + quoteProof(networks[i][j]) +"') " +
                      "ON DUPLICATE KEY UPDATE net=net";
                    state.executeUpdate(sql);
                    
                    sql = "INSERT INTO NETWORKOFARTICLE " +
                      "VALUES('" + quoteProof(networks[i][j]) + "', " +
                                 + keyA + ") " +
                            "ON DUPLICATE KEY UPDATE net=net";
                    state.executeUpdate(sql);
                }
                
            }
        }
        
        
        if(state!=null)
            state.close();
        if(connection!=null)
            connection.close();
        
    } catch (SQLException e) {
        throw new IllegalStateException("Something went wrong!", e);
    }
  }
}
