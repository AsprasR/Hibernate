package com.concretepage.util;

import com.concretepage.persistence.Meteorology;
import org.hibernate.HibernateException;
import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.concretepage.util.HibernateUtil.*;
import static org.junit.Assert.*;

public class HibernateUtilTest {
    private static SessionFactory concreteSessionFactory;
    private static Session session;
    private TreeSet<Meteorology> lists;

    @Before
    public void setUp() throws HibernateException {
        try {
            Properties prop= new Properties();
            prop.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/testdb?useLegacyDatetimeCode=false&serverTimezone=UTC&createDatabaseIfNotExist=true&UseUnicode=true&characterEncoding=utf8");
            prop.setProperty("hibernate.connection.username", "postgres");
            prop.setProperty("hibernate.connection.password", "root");
            prop.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            prop.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            prop.setProperty("hibernate.hbm2ddl.auto", "update");
            prop.setProperty("hibernate.connection.CharSet", "utf8");
            prop.setProperty("hibernate.connection.characterEncoding", "utf8" );
            prop.setProperty("hibernate.connection.useUnicode", "true");
            concreteSessionFactory = new Configuration()
                    .addPackage("com.concretepage.persistence")
                    .addProperties(prop)
                    .addAnnotatedClass(Meteorology.class)
                    .buildSessionFactory();
            session = concreteSessionFactory.openSession();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
        doQuery(session, "delete from Meteorology");
        lists = jsonGetRequest("https://danepubliczne.imgw.pl/api/data/synop");
        for( Meteorology o : lists ) {
            addMeteorology(session, o);
        }
    }

    @After
    public void tearDown() {
        session.close();
        concreteSessionFactory.close();
    }

    @Test
    public void AddQueryTest() {
        int resultNumber = getNumberOfMeteo(session);
        assertEquals(lists.size(), resultNumber);
    }
    @Test
    public void SelectQueryTest() {
        List expectedResult = listOfMeteorologies(session);
        Iterator<Meteorology> expectedIterator = expectedResult.iterator();
        Iterator<Meteorology> actualIterator = lists.iterator();
        while (expectedIterator.hasNext() && actualIterator.hasNext())
        {
            Meteorology expect = expectedIterator.next();
            Meteorology actual = actualIterator.next();
            assertEquals(expect, actual);
        }
    }

    @Test
    public void DeleteQueryTest() {
        int deleteId = 12295;
        deleteMeteorology(session, deleteId);
        for (Iterator iterator = lists.iterator(); iterator.hasNext(); ) {
            Meteorology meteo = (Meteorology) iterator.next();
            if( meteo.getId_stacji() == deleteId ) {
                iterator.remove();
            }
        }
        int resultNumber = getNumberOfMeteo(session);
        assertEquals(lists.size(), resultNumber);
        List expectedResult = listOfMeteorologies(session);
        for (Iterator iterator = expectedResult.iterator(); iterator.hasNext(); ) {
            Meteorology meteorology = (Meteorology) iterator.next();
            assertNotEquals(meteorology.getId_stacji(), 12295);
        }
    }

    @Test( expected = PropertyValueException.class )
    public void CheckNullTest() {
        Meteorology actual = new Meteorology();
        actual.setId_stacji(5000);
        addMeteorology(session, actual);
    }

    @Test
    public void UpdateQueryTest() throws ParseException {
        int updateId = 12295;
        String stacja = "Łomża";
        String data_pomiaru = "2018-04-04";
        int godzina_pomiaru = 12;
        Float temperatura = (float) 25;
        Integer predkosc_wiatru = 100;
        Integer kierunek_wiatru = 240;
        Float wilgotnosc_wzgledna = (float) 19.9;
        Double suma_opadu = 0.001;
        Float cisnienie = (float) 1220.5;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Iterator iterator = lists.iterator(); iterator.hasNext(); ) {
            Meteorology meteo = (Meteorology) iterator.next();
            if( meteo.getId_stacji() ==  updateId ) {
                meteo.setStacja(stacja);
                meteo.setData_pomiaru(sdf.parse(data_pomiaru));
                meteo.setGodzina_pomiaru(godzina_pomiaru);
                meteo.setTemperatura(temperatura);
                meteo.setPredkosc_wiatru(predkosc_wiatru);
                meteo.setKierunek_wiatru(kierunek_wiatru);
                meteo.setWilgotnosc_wzgledna(wilgotnosc_wzgledna);
                meteo.setSuma_opadu(suma_opadu);
                meteo.setCisnienie(cisnienie);
                updateMeteorology(session, meteo);
            }
        }
        List expectedResult = listOfMeteorologies(session);
        for (Iterator iterator = expectedResult.iterator(); iterator.hasNext(); ) {
            Meteorology meteorology = (Meteorology) iterator.next();
            if(meteorology.getId_stacji() == updateId ) {
                assertEquals(stacja, meteorology.getStacja());
                assertEquals(sdf.parse(data_pomiaru), meteorology.getData_pomiaru());
                assertEquals(godzina_pomiaru, meteorology.getGodzina_pomiaru());
                assertEquals(temperatura, meteorology.getTemperatura());
                assertEquals(predkosc_wiatru, meteorology.getPredkosc_wiatru());
                assertEquals(kierunek_wiatru, meteorology.getKierunek_wiatru());
                assertEquals(wilgotnosc_wzgledna, meteorology.getWilgotnosc_wzgledna());
                assertEquals(suma_opadu, meteorology.getSuma_opadu());
                assertEquals(cisnienie, meteorology.getCisnienie());
            }
        }
    }

}