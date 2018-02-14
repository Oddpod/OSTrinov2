package com.odd.ostrinov2.tools;

import com.odd.ostrinov2.Ost;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

class ServerDBHandler{

        private String url = "jdbc:mysql://10.22.44.163:3306/ostrinodb";
        private String user = "root";
        private String password = "";

        private void startConnectiontoDatabaseAndUpdate(String sql) throws SQLException {
            Connection myConn = DriverManager.getConnection(url, user, password);
            Statement myStat = myConn.createStatement();
            myStat.executeUpdate(sql);
        }

        private ResultSet startConnectiontoDatabaseAndQuery(String sql) throws SQLException {
            Connection myConn = DriverManager.getConnection(url, user, password);
            Statement myStat = myConn.createStatement();
            return myStat.executeQuery(sql);
        }

        public void saveOsts(List<Ost> Osts) throws SQLException {
            for(Ost ost : Osts) {
                int id = ost.getId();
                String title = ost.getTitle();
                String show = ost.getShow();
                String tags = ost.getTags();
                String urlString = ost.getUrl();

                String sql = "insert ignore into osts "
                        + "values('" + id + "', '" + title + "', '" + show + "', '" + tags + "', '" + urlString + "')";
                startConnectiontoDatabaseAndUpdate(sql);
            }
        }

        public List<Ost> getOsts(List<Ost> ostList) throws SQLException{

            String sql = "select * from osts";
            ResultSet myRS = startConnectiontoDatabaseAndQuery(sql);

            while(myRS.next()){
                int id;
                String title, show, tags, url;
                id = myRS.getInt("Ostid");
                title = myRS.getString("Title");
                show = myRS.getString("Show");
                tags = myRS.getString("Tags");
                url = myRS.getString("Url");
                Ost ost = new Ost(title, show, tags, url);
                ost.setId(id);
                ostList.add(ost);
            }
            return ostList;
        }
    }
