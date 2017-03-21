/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebaleerjsondesdeurl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Juan Pablo
 */
public class MrssFeedCreator {
    
    public static String transform(String d){
        return d.substring(0,4)+"/"+d.substring(5,7)+"/"+d.substring(8,10)+"/";
    }
    
    public static void main(String[] args){
        //first execute the IgnDatabaseCreator so that this can work
        try{
            String urlDB = "jdbc:mysql://localhost:3306/ignmedia?zeroDateTimeBehavior=convertToNull";
            String username = "root";
            String password = "";//modify this 
 
            //The next lines connect the database (standard procedure)
            System.out.println("Connecting database...");
            Connection connection = null;
            Statement state = null;

            connection = DriverManager.getConnection(urlDB, username, password);
            System.out.println("Database connected!");
            System.out.println("Updating the tables...");
            state = connection.createStatement();
            String sql;
            ResultSet rs;
            
            PrintWriter writer = new PrintWriter("ignmrss.xml", "UTF-8");
            writer.println("<rss version=\"2.0\" xmlns:media=\"http://search.yahoo.com/mrss/\">");
            writer.println("<channel>");
            writer.println("<title>IGN Media</title>");
            writer.println("<link>http://www.ign.com</link>");
            writer.println("<description>IGN videos and articles</description>");
            
            sql = "SELECT COUNT(*) FROM ELEMENTS as n";
            rs = state.executeQuery(sql);
            rs.next(); 
            int numElements = rs.getInt("n");
            
            
            sql = "SELECT * FROM METADATAA";
            rs = state.executeQuery(sql);
            
            
            while(rs.next()){ //sorry for the lacking of info!! got out of time :( of course I could do better
                writer.println("<item>");

                writer.println("<title>"+rs.getString("headline")+"</title>");
                writer.println("<link>http://www.ign.com/articles/"+transform(rs.getString("publishDate"))+rs.getString("slug")+"</link>");
                writer.println("<description>"+rs.getString("subHeadline")+"</description>");
                writer.println("</item>");
            }
            
            sql = "SELECT * FROM METADATAV";
            rs = state.executeQuery(sql);
            while(rs.next()){ //sorry for the lacking of info!! got out of time :( of course I could do better
                writer.println("<item>");

                writer.println("<title>"+rs.getString("name")+"</title>");
                writer.println("<link>"+rs.getString("url")+"</link>");
                writer.println("<description>"+rs.getString("description")+"</description>");
                writer.println("</item>");
            }
            
            writer.println("<channel>");
            writer.println("</rss>");
            
            
            
            
            writer.close();
        } catch (IOException e) {
           // do something
        }catch (SQLException e) {
            throw new IllegalStateException("Something went wrong!", e);
        }
    }
}
