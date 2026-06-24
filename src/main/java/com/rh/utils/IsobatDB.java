package com.rh.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class IsobatDB {
    private static IsobatDB instance ;
    private final String URL ="jdbc:mysql://127.0.0.1:3306/isobat";
    private final String USERNAME ="root";
    private final String PASSWORD ="";
    private Connection cnx ;

    private IsobatDB(){
        try {
            cnx = DriverManager.getConnection(URL,USERNAME,PASSWORD);

            System.out.println("Connected ...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static IsobatDB getInstance(){
        if (instance == null)
            instance = new IsobatDB();

        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}