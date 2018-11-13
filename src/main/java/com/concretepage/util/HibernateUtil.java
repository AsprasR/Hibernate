package com.concretepage.util;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.concretepage.persistence.Meteorology;
import org.hibernate.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.PersistenceException;

class HibernateUtil {

    static TreeSet<Meteorology> jsonGetRequest(String urlQueryString) {
        TreeSet<Meteorology> listsOfMeteo = new TreeSet<>();
        try {
            URL url = new URL(urlQueryString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            StringBuilder response= new StringBuilder();

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(response.toString());
            for( int i = 0; i < jsonArray.length(); i++ ) {
                JSONObject object = jsonArray.getJSONObject(i);
                int id_stacji = object.getInt("id_stacji");
                String stacja = object.getString("stacja");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
                Date data_pomiaru = sdf.parse(object.getString("data_pomiaru") + " " + object.getInt("godzina_pomiaru"));
                Float temperatura = object.isNull("temperatura") ? null : object.getFloat("temperatura");
                Integer predkosc_wiatru = object.isNull("predkosc_wiatru") ? null : object.getInt("predkosc_wiatru");
                Integer kierunek_wiatru = object.isNull("kierunek_wiatru") ? null : object.getInt("kierunek_wiatru");
                Float wilgotnosc_wzgledna = object.isNull("wilgotnosc_wzgledna") ? null : object.getFloat("wilgotnosc_wzgledna");
                Double suma_opadu = object.isNull("suma_opadu") ? null : object.getDouble("suma_opadu");
                Float cisnienie = object.isNull("cisnienie") ? null : object.getFloat("cisnienie") ;
                listsOfMeteo.add(new Meteorology(id_stacji, stacja, data_pomiaru, temperatura,
                        predkosc_wiatru, kierunek_wiatru, wilgotnosc_wzgledna,
                        suma_opadu, cisnienie));
            }
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }
        return listsOfMeteo;
    }

    static void addMeteorology( Session session, Meteorology meteorology ) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(meteorology);
            tx.commit();
        } catch( PropertyValueException e ) {
            if (tx != null)
                tx.rollback();
            throw e;
        } catch( HibernateException e ) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
        }
    }

    static int getNumberOfMeteo( Session session ) {
        Transaction tx = null;
        int size = 0;
        try {
            tx = session.beginTransaction();
            List meteo = session.createQuery("FROM Meteorology").list();
            size = meteo.size();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return size;
    }

    @SuppressWarnings("unchecked")
    static List<Meteorology> listOfMeteorologies(Session session ) {
        return session.createQuery("FROM Meteorology Order by id_stacji").list();
    }

    static void updateMeteorology(Session session, Meteorology meteorology ){
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(meteorology);
            tx.commit();
        } catch (HibernateException e) {
            if ( tx != null )
                tx.rollback();
            e.printStackTrace();
        } catch( PersistenceException e ) {
            if( tx != null )
                tx.rollback();
            throw e;
        }
    }

    static void deleteMeteorology(Session session, Integer id_stacji){
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Meteorology meteorology = session.get(Meteorology.class, id_stacji);
            session.delete(meteorology);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}
