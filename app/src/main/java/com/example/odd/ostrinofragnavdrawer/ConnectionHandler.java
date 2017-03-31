package com.example.odd.ostrinofragnavdrawer;
/**
 * Created by Odd on 25.02.2017.
 */

public class ConnectionHandler{


    /*private Connection con;
    private String url, username, password;
    private Connection myConn;
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    // constructor
    public ConnectionHandler() {
        url = "jdbc:mysql://sql11.freemysqlhosting.net:3306/sql11160769";
        username = " sql11160769";
        password = " Rdi4bykUhS";
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName(JDBC_DRIVER).newInstance();
        } catch (Exception ex) {
            System.out.println("Error loading driver");
        }

        //client = new OkHttpClient();
        //JSON = MediaType.parse("application/json; charset=utf-8");
    }

    public void startConnectiontoDatabaseAndUpdate(String sql) throws SQLException {
        myConn = DriverManager.getConnection(url, username, password);
        Statement myStat = myConn.createStatement();
        myStat.executeUpdate(sql);
    }

    public ResultSet startConnectiontoDatabaseAndQuery(String sql) throws SQLException{
        myConn = DriverManager.getConnection(url, username, password);
        Statement myStat = myConn.createStatement();
        ResultSet myRsi = myStat.executeQuery(sql);
        return myRsi;
    }

    public void insertOstTODB( Ost ost) throws SQLException {
        String title = ost.getTitle();
        String show = ost.getShow();
        String tags = ost.getTags();
        String url = ost.getUrl();

        String sql = " insert into OSTrino "
                + "(title, show, tags, url)"
                + "values( '" + title + "', '" + show + "', '" + tags + "', '" + url + "')";
        startConnectiontoDatabaseAndUpdate(sql);
    }*/
}
